package com.placsp.monitor.service;

import com.placsp.monitor.dto.LicitacionDto;
import com.placsp.monitor.model.Licitacion;
import com.placsp.monitor.repository.LicitacionRepository;
import com.placsp.monitor.repository.LicitacionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LicitacionService {

    private final LicitacionRepository repository;

    public LicitacionService(LicitacionRepository repository) {
        this.repository = repository;
    }

    public record ResumenDto(long totalLicitaciones, BigDecimal sumaImportes) {}

    public Page<LicitacionDto> buscar(String estado, String tipoContrato, String cpv,
                                      boolean cpvExclusivo, String texto,
                                      BigDecimal importeMin, BigDecimal importeMax,
                                      String nutsCode, String procedimiento, Integer diasPlazo,
                                      Pageable pageable) {
        Specification<Licitacion> spec = buildSpec(estado, tipoContrato, cpv, cpvExclusivo, texto,
                importeMin, importeMax, nutsCode, procedimiento, diasPlazo);
        return repository.findAll(spec, pageable).map(LicitacionDto::fromEntity);
    }

    public ResumenDto stats(String estado, String tipoContrato, String cpv,
                            boolean cpvExclusivo, String texto,
                            BigDecimal importeMin, BigDecimal importeMax,
                            String nutsCode, String procedimiento, Integer diasPlazo) {
        Specification<Licitacion> spec = buildSpec(estado, tipoContrato, cpv, cpvExclusivo, texto,
                importeMin, importeMax, nutsCode, procedimiento, diasPlazo);
        List<Licitacion> results = repository.findAll(spec);
        BigDecimal suma = results.stream()
                .map(Licitacion::getImporteSinImpuestos)
                .filter(i -> i != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenDto(results.size(), suma);
    }

    private static final int MAX_EXPORT = 5000;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void exportCsv(String estado, String tipoContrato, String cpv,
                          boolean cpvExclusivo, String texto,
                          BigDecimal importeMin, BigDecimal importeMax,
                          String nutsCode, String procedimiento, Integer diasPlazo,
                          Writer writer) throws IOException {
        Specification<Licitacion> spec = buildSpec(estado, tipoContrato, cpv, cpvExclusivo, texto,
                importeMin, importeMax, nutsCode, procedimiento, diasPlazo);
        Pageable limit = PageRequest.of(0, MAX_EXPORT, Sort.by(Sort.Direction.DESC, "fechaActualizacion"));
        List<Licitacion> results = repository.findAll(spec, limit).getContent();

        writer.write('\ufeff'); // BOM for Excel UTF-8
        writer.write("Expediente;Estado;Tipo Contrato;Objeto;Órgano Contratación;CPV;" +
                "Importe sin impuestos;Importe con impuestos;Fecha límite ofertas;Hora límite;" +
                "Procedimiento;Lugar ejecución;NUTS;Fecha actualización;Enlace PLACSP\n");

        for (Licitacion lic : results) {
            writer.write(csvField(lic.getExpediente()));
            writer.write(';');
            writer.write(csvField(lic.getEstado()));
            writer.write(';');
            writer.write(csvField(lic.getTipoContrato()));
            writer.write(';');
            writer.write(csvField(lic.getObjeto()));
            writer.write(';');
            writer.write(csvField(lic.getOrganoContratacion()));
            writer.write(';');
            writer.write(csvField(lic.getCpvCodes()));
            writer.write(';');
            writer.write(lic.getImporteSinImpuestos() != null ? lic.getImporteSinImpuestos().toPlainString() : "");
            writer.write(';');
            writer.write(lic.getImporteConImpuestos() != null ? lic.getImporteConImpuestos().toPlainString() : "");
            writer.write(';');
            writer.write(lic.getFechaLimiteOfertas() != null ? lic.getFechaLimiteOfertas().format(DATE_FMT) : "");
            writer.write(';');
            writer.write(lic.getHoraLimiteOfertas() != null ? lic.getHoraLimiteOfertas().format(TIME_FMT) : "");
            writer.write(';');
            writer.write(csvField(lic.getTipoProcedimiento()));
            writer.write(';');
            writer.write(csvField(lic.getLugarEjecucion()));
            writer.write(';');
            writer.write(csvField(lic.getNutsCode()));
            writer.write(';');
            writer.write(lic.getFechaActualizacion() != null ? lic.getFechaActualizacion().format(DATETIME_FMT) : "");
            writer.write(';');
            writer.write(csvField(lic.getEnlacePlacsp()));
            writer.write('\n');
        }

        writer.flush();
    }

    private static String csvField(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private Specification<Licitacion> buildSpec(String estado, String tipoContrato, String cpv,
                                                boolean cpvExclusivo, String texto,
                                                BigDecimal importeMin, BigDecimal importeMax,
                                                String nutsCode, String procedimiento, Integer diasPlazo) {
        return Specification.where(LicitacionSpecification.byEstado(estado))
                .and(LicitacionSpecification.byTipoContrato(tipoContrato))
                .and(cpvExclusivo
                        ? LicitacionSpecification.byCpvExclusive(cpv)
                        : LicitacionSpecification.byCpvPrefix(cpv))
                .and(LicitacionSpecification.byTexto(texto))
                .and(LicitacionSpecification.byImporteMin(importeMin))
                .and(LicitacionSpecification.byImporteMax(importeMax))
                .and(LicitacionSpecification.byNutsCode(nutsCode))
                .and(LicitacionSpecification.byProcedimiento(procedimiento))
                .and(LicitacionSpecification.byPlazoActivo(diasPlazo));
    }
}
