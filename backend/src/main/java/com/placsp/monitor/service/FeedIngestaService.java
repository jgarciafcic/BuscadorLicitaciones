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

@Service
public class FeedIngestaService {

    private static final Logger log = LoggerFactory.getLogger(FeedIngestaService.class);

    private final FeedClient feedClient;
    private final AtomParser atomParser;
    private final LicitacionRepository repository;
    private final AppConfig appConfig;

    public FeedIngestaService(FeedClient feedClient, AtomParser atomParser,
                              LicitacionRepository repository, AppConfig appConfig) {
        this.feedClient = feedClient;
        this.atomParser = atomParser;
        this.repository = repository;
        this.appConfig = appConfig;
    }

    public record IngestaResumen(int paginasProcesadas, int entriesLeidas,
                                 int nuevas, int actualizadas, int eliminadas,
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

        try {
            for (int page = 0; page < maxPages && url != null; page++) {
                byte[] body = downloadPage(url);
                if (body == null) break;

                AtomParser.ParseResult result = atomParser.parse(new ByteArrayInputStream(body));
                allParsed.addAll(result.licitaciones());
                allDeletedRefs.addAll(result.deletedEntryRefs());
                paginasProcesadas++;

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
        List<Licitacion> toSave = new ArrayList<>();

        for (Licitacion lic : deduped.values()) {
            Licitacion existente = existentesMap.get(lic.getEntryId());
            if (existente == null) {
                nuevas++;
                toSave.add(lic);
            } else {
                if (existente.getFechaActualizacion() == null
                        || (lic.getFechaActualizacion() != null
                            && lic.getFechaActualizacion().isAfter(existente.getFechaActualizacion()))) {
                    actualizadas++;
                    toSave.add(lic);
                }
            }
        }

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

        IngestaResumen resumen = new IngestaResumen(paginasProcesadas, entriesLeidas, nuevas, actualizadas, eliminadas, url);
        log.info("Ingesta completada: {}", resumen);
        return resumen;
    }

    private byte[] downloadPage(String url) throws IOException, InterruptedException {
        try (var is = feedClient.download(url)) {
            return is.readAllBytes();
        }
    }
}
