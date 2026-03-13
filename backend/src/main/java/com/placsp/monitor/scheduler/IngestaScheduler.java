package com.placsp.monitor.scheduler;

import com.placsp.monitor.repository.LicitacionRepository;
import com.placsp.monitor.service.FeedIngestaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IngestaScheduler {

    private static final Logger log = LoggerFactory.getLogger(IngestaScheduler.class);
    private static final int INITIAL_PAGES = 3;

    private final FeedIngestaService feedIngestaService;
    private final LicitacionRepository repository;

    public IngestaScheduler(FeedIngestaService feedIngestaService, LicitacionRepository repository) {
        this.feedIngestaService = feedIngestaService;
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void ingestaInicial() {
        if (repository.count() > 0) {
            log.info("Base de datos con {} licitaciones, se omite ingesta inicial", repository.count());
            return;
        }
        log.info("Base de datos vacía, lanzando ingesta inicial de {} páginas", INITIAL_PAGES);
        try {
            var resumen = feedIngestaService.ejecutar(INITIAL_PAGES, null);
            log.info("Ingesta inicial completada: {} nuevas, {} actualizadas, {} eliminadas",
                    resumen.nuevas(), resumen.actualizadas(), resumen.eliminadas());
        } catch (Exception e) {
            log.error("Error en ingesta inicial: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${placsp.feed.cron}")
    public void ejecutarIngesta() {
        log.info("Ingesta programada iniciada");
        try {
            var resumen = feedIngestaService.ejecutar();
            log.info("Ingesta programada completada: {} páginas, {} nuevas, {} actualizadas, {} eliminadas",
                    resumen.paginasProcesadas(), resumen.nuevas(), resumen.actualizadas(), resumen.eliminadas());
        } catch (Exception e) {
            log.error("Error en ingesta programada: {}", e.getMessage(), e);
        }
    }
}
