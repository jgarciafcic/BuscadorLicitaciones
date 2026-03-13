package com.placsp.monitor.controller;

import com.placsp.monitor.service.FeedIngestaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingesta")
@CrossOrigin(origins = "*")
@Tag(name = "Ingesta", description = "Gestión de la ingesta del feed ATOM de PLACSP")
public class IngestaController {

    private final FeedIngestaService feedIngestaService;

    public IngestaController(FeedIngestaService feedIngestaService) {
        this.feedIngestaService = feedIngestaService;
    }

    @PostMapping("/ejecutar")
    @Operation(summary = "Ejecutar ingesta manual", description = "Descarga las páginas del feed ATOM. Cada página contiene 500 entries. Por defecto 1 página. Pasar fromUrl para continuar desde una página concreta.")
    public ResponseEntity<FeedIngestaService.IngestaResumen> ejecutar(
            @RequestParam(defaultValue = "1") int pages,
            @RequestParam(required = false) String fromUrl) {
        var resumen = feedIngestaService.ejecutar(Math.max(1, Math.min(pages, 200)), fromUrl);
        return ResponseEntity.ok(resumen);
    }
}
