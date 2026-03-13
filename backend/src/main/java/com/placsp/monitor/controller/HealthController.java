package com.placsp.monitor.controller;

import com.placsp.monitor.dto.LicitacionDto;
import com.placsp.monitor.repository.LicitacionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Sistema", description = "Endpoints de estado y diagnóstico")
public class HealthController {

    private final LicitacionRepository repository;

    public HealthController(LicitacionRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/health")
    @Operation(summary = "Estado de la aplicación", description = "Devuelve el estado del servicio y el número de licitaciones en base de datos")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "licitaciones", repository.count());
    }

    @GetMapping("/api/licitaciones/sample")
    @Operation(summary = "Muestra de licitaciones", description = "Devuelve las 5 licitaciones con mayor importe para verificación rápida")
    public List<LicitacionDto> sample() {
        return repository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "importeSinImpuestos"))
        ).map(LicitacionDto::fromEntity).getContent();
    }
}
