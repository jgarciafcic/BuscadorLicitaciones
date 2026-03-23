package com.placsp.monitor.service;

import com.placsp.monitor.config.AppConfig;
import com.placsp.monitor.model.Licitacion;
import com.placsp.monitor.repository.LicitacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FeedIngestaService {

    private static final Logger log = LoggerFactory.getLogger(FeedIngestaService.class);

    private final FeedClient feedClient;
    private final AtomParser atomParser;
    private final LicitacionRepository repository;
    private final AppConfig appConfig;

    public record IngestaProgreso(int paginaDescargada, int totalPaginas,
                                   int entriesLeidas, int nuevasHastaAhora,
                                   String fase) {}

    private final AtomicReference<IngestaProgreso> progreso = new AtomicReference<>(null);

    public IngestaProgreso getProgreso() {
        return progreso.get();
    }

    public FeedIngestaService(FeedClient feedClient, AtomParser atomParser,
                              LicitacionRepository repository, AppConfig appConfig) {
        this.feedClient = feedClient;
        this.atomParser = atomParser;
        this.repository = repository;
        this.appConfig = appConfig;
    }

    public record IngestaResumen(int paginasProcesadas, int entriesLeidas,
                                 int nuevas, int actualizadas, int eliminadas,
                                 int yaExistentes, boolean solapaConBbdd,
                                 String nextPageUrl) {}

    private record PageStats(int nuevas, int actualizadas, int eliminadas, int yaExistentes) {}

    public IngestaResumen ejecutar() {
        return ejecutar(appConfig.getMaxPages(), null);
    }

    public IngestaResumen ejecutar(int maxPages, String fromUrl) {
        String nextUrl = (fromUrl != null && !fromUrl.isBlank()) ? fromUrl : appConfig.getUrl();
        log.info("Iniciando ingesta del feed PLACSP ({} páginas máx.) desde {}", maxPages, nextUrl);

        int paginasProcesadas = 0;
        int totalEntriesLeidas = 0;
        int totalNuevas = 0;
        int totalActualizadas = 0;
        int totalEliminadas = 0;
        int totalYaExistentes = 0;
        boolean solapaConBbdd = false;

        progreso.set(new IngestaProgreso(0, maxPages, 0, 0, "Iniciando ingesta..."));

        for (int page = 0; page < maxPages && nextUrl != null; page++) {
            progreso.set(new IngestaProgreso(page + 1, maxPages, totalEntriesLeidas, totalNuevas,
                    "Descargando página " + (page + 1) + " de " + maxPages + "..."));

            try (InputStream stream = feedClient.download(nextUrl)) {
                AtomParser.ParseResult result = atomParser.parse(stream);

                progreso.set(new IngestaProgreso(page + 1, maxPages, totalEntriesLeidas + result.licitaciones().size(), totalNuevas,
                        "Guardando página " + (page + 1) + "..."));

                PageStats stats = procesarYGuardar(result.licitaciones(), result.deletedEntryRefs());

                totalEntriesLeidas += result.licitaciones().size();
                totalNuevas += stats.nuevas();
                totalActualizadas += stats.actualizadas();
                totalEliminadas += stats.eliminadas();
                totalYaExistentes += stats.yaExistentes();
                if (stats.yaExistentes() > 0 || stats.actualizadas() > 0) solapaConBbdd = true;
                paginasProcesadas++;

                nextUrl = result.nextLink();

                progreso.set(new IngestaProgreso(page + 1, maxPages, totalEntriesLeidas, totalNuevas,
                        "Página " + (page + 1) + " guardada (" + stats.nuevas() + " nuevas)"));

            } catch (IOException | InterruptedException e) {
                log.error("Error durante la ingesta en página {}: {}", page + 1, e.getMessage(), e);
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                break;
            }
        }

        progreso.set(null);

        IngestaResumen resumen = new IngestaResumen(paginasProcesadas, totalEntriesLeidas,
                totalNuevas, totalActualizadas, totalEliminadas, totalYaExistentes, solapaConBbdd, nextUrl);
        log.info("Ingesta completada: {} (solapa con BBDD: {})", resumen, solapaConBbdd);
        return resumen;
    }

    @Transactional
    protected PageStats procesarYGuardar(List<Licitacion> licitaciones, List<String> deletedRefs) {
        // Deduplicar: quedarse con la de mayor fechaActualizacion por entryId
        Map<String, Licitacion> deduped = new LinkedHashMap<>();
        for (Licitacion lic : licitaciones) {
            if (lic.getEntryId() == null) continue;
            deduped.merge(lic.getEntryId(), lic, (existing, incoming) -> {
                if (existing.getFechaActualizacion() == null) return incoming;
                if (incoming.getFechaActualizacion() == null) return existing;
                return incoming.getFechaActualizacion().isAfter(existing.getFechaActualizacion())
                        ? incoming : existing;
            });
        }

        List<String> ids = new ArrayList<>(deduped.keySet());
        List<Licitacion> existentes = repository.findAllById(ids);
        Map<String, Licitacion> existentesMap = new LinkedHashMap<>();
        for (Licitacion e : existentes) existentesMap.put(e.getEntryId(), e);

        int nuevas = 0, actualizadas = 0, yaExistentes = 0;
        List<Licitacion> toSave = new ArrayList<>();

        for (Licitacion lic : deduped.values()) {
            Licitacion existente = existentesMap.get(lic.getEntryId());
            if (existente == null) {
                nuevas++;
                toSave.add(lic);
            } else {
                boolean isNewer = existente.getFechaActualizacion() == null
                        || (lic.getFechaActualizacion() != null
                            && lic.getFechaActualizacion().isAfter(existente.getFechaActualizacion()));
                boolean missingNewFields = existente.getCriteriosAdjudicacion() == null
                        && lic.getCriteriosAdjudicacion() != null;
                if (isNewer || missingNewFields) {
                    actualizadas++;
                    toSave.add(lic);
                } else {
                    yaExistentes++;
                }
            }
        }

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
            log.info("Guardadas {} licitaciones ({} nuevas, {} actualizadas)", toSave.size(), nuevas, actualizadas);
        }

        int eliminadas = 0;
        if (!deletedRefs.isEmpty()) {
            List<Licitacion> aEliminar = repository.findAllById(deletedRefs);
            if (!aEliminar.isEmpty()) {
                repository.deleteAll(aEliminar);
                eliminadas = aEliminar.size();
                log.info("Eliminadas {} licitaciones marcadas como deleted-entry", eliminadas);
            }
        }

        return new PageStats(nuevas, actualizadas, eliminadas, yaExistentes);
    }
}
