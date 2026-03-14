package com.placsp.monitor.service;

import com.placsp.monitor.config.AppConfig;
import com.placsp.monitor.model.Licitacion;
import com.placsp.monitor.repository.LicitacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    @Transactional
    public IngestaResumen ejecutar() {
        return ejecutar(appConfig.getMaxPages(), null);
    }

    @Transactional
    public IngestaResumen ejecutar(int maxPages, String fromUrl) {
        String startUrl = (fromUrl != null && !fromUrl.isBlank()) ? fromUrl : appConfig.getUrl();
        log.info("Iniciando ingesta del feed PLACSP ({} páginas máx.) desde {}", maxPages, startUrl);

        List<Licitacion> allParsed = new ArrayList<>();
        List<String> allDeletedRefs = new ArrayList<>();
        int paginasProcesadas = 0;

        String url = startUrl;
        progreso.set(new IngestaProgreso(0, maxPages, 0, 0, "Descargando feed..."));

        try {
            for (int page = 0; page < maxPages && url != null; page++) {
                progreso.set(new IngestaProgreso(page + 1, maxPages, allParsed.size(), 0,
                        "Descargando página " + (page + 1) + " de " + maxPages + "..."));

                byte[] body = downloadPage(url);
                if (body == null) break;

                AtomParser.ParseResult result = atomParser.parse(new ByteArrayInputStream(body));
                allParsed.addAll(result.licitaciones());
                allDeletedRefs.addAll(result.deletedEntryRefs());
                paginasProcesadas++;

                progreso.set(new IngestaProgreso(page + 1, maxPages, allParsed.size(), 0,
                        "Parseadas " + allParsed.size() + " entries de " + (page + 1) + " páginas"));

                String nextUrl = FeedClient.extractNextLink(new ByteArrayInputStream(body)).orElse(null);
                if (nextUrl != null) {
                    log.debug("Siguiente página ({}/{}): {}", page + 2, maxPages, nextUrl);
                }
                url = nextUrl;
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error durante la descarga del feed: {}", e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        progreso.set(new IngestaProgreso(paginasProcesadas, maxPages, allParsed.size(), 0,
                "Filtrando y guardando en base de datos..."));

        int entriesLeidas = allParsed.size();

        // Deduplicar: quedarse con la de mayor fechaActualizacion por entryId
        Map<String, Licitacion> deduped = new LinkedHashMap<>();
        for (Licitacion lic : allParsed) {
            if (lic.getEntryId() == null) continue;
            deduped.merge(lic.getEntryId(), lic, (existing, incoming) -> {
                if (existing.getFechaActualizacion() == null) return incoming;
                if (incoming.getFechaActualizacion() == null) return existing;
                return incoming.getFechaActualizacion().isAfter(existing.getFechaActualizacion())
                        ? incoming : existing;
            });
        }

        // Determinar nuevas vs actualizadas
        List<String> ids = new ArrayList<>(deduped.keySet());
        List<Licitacion> existentes = repository.findAllById(ids);
        Map<String, Licitacion> existentesMap = new LinkedHashMap<>();
        for (Licitacion e : existentes) {
            existentesMap.put(e.getEntryId(), e);
        }

        int nuevas = 0;
        int actualizadas = 0;
        int yaExistentes = 0;
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

        boolean solapaConBbdd = yaExistentes > 0 || actualizadas > 0;

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
            log.info("Guardadas {} licitaciones ({} nuevas, {} actualizadas)", toSave.size(), nuevas, actualizadas);
        }

        // Eliminar deleted-entries
        int eliminadas = 0;
        if (!allDeletedRefs.isEmpty()) {
            List<Licitacion> aEliminar = repository.findAllById(allDeletedRefs);
            if (!aEliminar.isEmpty()) {
                repository.deleteAll(aEliminar);
                eliminadas = aEliminar.size();
                log.info("Eliminadas {} licitaciones marcadas como deleted-entry", eliminadas);
            }
        }

        progreso.set(null);

        IngestaResumen resumen = new IngestaResumen(paginasProcesadas, entriesLeidas, nuevas, actualizadas, eliminadas, yaExistentes, solapaConBbdd, url);
        log.info("Ingesta completada: {} (solapa con BBDD: {})", resumen, solapaConBbdd);
        return resumen;
    }

    private byte[] downloadPage(String url) throws IOException, InterruptedException {
        try (var is = feedClient.download(url)) {
            return is.readAllBytes();
        }
    }
}
