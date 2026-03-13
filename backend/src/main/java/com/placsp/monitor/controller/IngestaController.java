package com.placsp.monitor.controller;

import com.placsp.monitor.model.Licitacion;
import com.placsp.monitor.repository.LicitacionRepository;
import com.placsp.monitor.scheduler.IngestaScheduler;
import com.placsp.monitor.service.FeedIngestaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingesta")
@CrossOrigin(origins = "*")
@Tag(name = "Ingesta", description = "Gestión de la ingesta del feed ATOM de PLACSP")
public class IngestaController {

    private final FeedIngestaService feedIngestaService;
    private final LicitacionRepository licitacionRepository;
    private final IngestaScheduler ingestaScheduler;

    public IngestaController(FeedIngestaService feedIngestaService, LicitacionRepository licitacionRepository,
                             IngestaScheduler ingestaScheduler) {
        this.feedIngestaService = feedIngestaService;
        this.licitacionRepository = licitacionRepository;
        this.ingestaScheduler = ingestaScheduler;
    }

    @GetMapping("/estado")
    @Operation(summary = "Estado de la ingesta", description = "Devuelve si hay una ingesta en curso y el número de licitaciones en BBDD.")
    public ResponseEntity<Map<String, Object>> estado() {
        return ResponseEntity.ok(Map.of(
                "ingesting", ingestaScheduler.isIngesting(),
                "totalLicitaciones", licitacionRepository.count()
        ));
    }

    @PostMapping("/ejecutar")
    @Operation(summary = "Ejecutar ingesta manual", description = "Descarga las páginas del feed ATOM. Cada página contiene 500 entries. Por defecto 1 página. Pasar fromUrl para continuar desde una página concreta.")
    public ResponseEntity<FeedIngestaService.IngestaResumen> ejecutar(
            @RequestParam(defaultValue = "5") int pages,
            @RequestParam(required = false) String fromUrl) {
        var resumen = feedIngestaService.ejecutar(Math.max(1, Math.min(pages, 200)), fromUrl);
        return ResponseEntity.ok(resumen);
    }

    @PostMapping("/importar")
    @Operation(summary = "Importar licitaciones en bulk", description = "Recibe una lista de licitaciones en JSON y las guarda (upsert por entryId).")
    public ResponseEntity<Map<String, Object>> importar(@RequestBody List<Licitacion> licitaciones) {
        var saved = licitacionRepository.saveAll(licitaciones);
        return ResponseEntity.ok(Map.of("importadas", saved.size()));
    }
}
