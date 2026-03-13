package com.placsp.monitor.controller;

import com.placsp.monitor.dto.LicitacionDto;
import com.placsp.monitor.service.LicitacionService;
import com.placsp.monitor.service.PliegoAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/licitaciones")
@CrossOrigin(origins = "*")
@Tag(name = "Licitaciones", description = "Búsqueda, estadísticas y exportación de licitaciones públicas")
public class LicitacionController {

    private final LicitacionService licitacionService;
    private final PliegoAnalysisService pliegoAnalysisService;

    public LicitacionController(LicitacionService licitacionService,
                                PliegoAnalysisService pliegoAnalysisService) {
        this.licitacionService = licitacionService;
        this.pliegoAnalysisService = pliegoAnalysisService;
    }

    @GetMapping
    @Operation(summary = "Buscar licitaciones", description = "Devuelve licitaciones paginadas aplicando filtros dinámicos. Por defecto: estado=PUB, tipoContrato=2 (Servicios), cpv=72 (IT)")
    public Page<LicitacionDto> buscar(
            @RequestParam(defaultValue = "PUB") String estado,
            @RequestParam(defaultValue = "2") String tipoContrato,
            @RequestParam(defaultValue = "72") String cpv,
            @RequestParam(defaultValue = "false") boolean cpvExclusivo,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) BigDecimal importeMin,
            @RequestParam(required = false) BigDecimal importeMax,
            @RequestParam(required = false) String nutsCode,
            @RequestParam(required = false) String procedimiento,
            @RequestParam(required = false) Integer diasPlazo,
            @PageableDefault(size = 20, sort = "fechaActualizacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return licitacionService.buscar(estado, tipoContrato, cpv, cpvExclusivo, texto,
                importeMin, importeMax, nutsCode, procedimiento, diasPlazo, pageable);
    }

    @GetMapping("/cantabria")
    @Operation(summary = "Licitaciones en Cantabria", description = "Atajo que aplica nutsCode=ES13 (Cantabria) junto con los filtros por defecto")
    public Page<LicitacionDto> cantabria(
            @RequestParam(defaultValue = "PUB") String estado,
            @RequestParam(defaultValue = "2") String tipoContrato,
            @RequestParam(defaultValue = "72") String cpv,
            @RequestParam(defaultValue = "false") boolean cpvExclusivo,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) BigDecimal importeMin,
            @RequestParam(required = false) BigDecimal importeMax,
            @RequestParam(required = false) String procedimiento,
            @RequestParam(required = false) Integer diasPlazo,
            @PageableDefault(size = 20, sort = "fechaActualizacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return licitacionService.buscar(estado, tipoContrato, cpv, cpvExclusivo, texto,
                importeMin, importeMax, "ES13", procedimiento, diasPlazo, pageable);
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar licitaciones a CSV", description = "Descarga un fichero CSV con todas las licitaciones que cumplan los filtros (máximo 5000)")
    public void exportCsv(
            @RequestParam(defaultValue = "PUB") String estado,
            @RequestParam(defaultValue = "2") String tipoContrato,
            @RequestParam(defaultValue = "72") String cpv,
            @RequestParam(defaultValue = "false") boolean cpvExclusivo,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) BigDecimal importeMin,
            @RequestParam(required = false) BigDecimal importeMax,
            @RequestParam(required = false) String nutsCode,
            @RequestParam(required = false) String procedimiento,
            @RequestParam(required = false) Integer diasPlazo,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"licitaciones.csv\"");
        licitacionService.exportCsv(estado, tipoContrato, cpv, cpvExclusivo, texto,
                importeMin, importeMax, nutsCode, procedimiento, diasPlazo,
                response.getWriter());
    }

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas de licitaciones", description = "Devuelve el número total de licitaciones y la suma de importes para los filtros aplicados")
    public LicitacionService.ResumenDto stats(
            @RequestParam(defaultValue = "PUB") String estado,
            @RequestParam(defaultValue = "2") String tipoContrato,
            @RequestParam(defaultValue = "72") String cpv,
            @RequestParam(defaultValue = "false") boolean cpvExclusivo,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) BigDecimal importeMin,
            @RequestParam(required = false) BigDecimal importeMax,
            @RequestParam(required = false) String nutsCode,
            @RequestParam(required = false) String procedimiento,
            @RequestParam(required = false) Integer diasPlazo) {
        return licitacionService.stats(estado, tipoContrato, cpv, cpvExclusivo, texto,
                importeMin, importeMax, nutsCode, procedimiento, diasPlazo);
    }

    @PostMapping("/analizar-pliegos")
    @Operation(summary = "Analizar pliegos", description = "Descarga los pliegos PCAP y PPT, extrae el texto y genera un resumen con IA")
    public PliegoAnalysisService.AnalisisResult analizarPliegos(@RequestParam String entryId) {
        return pliegoAnalysisService.analizar(entryId);
    }
}
