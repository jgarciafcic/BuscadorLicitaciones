package com.placsp.monitor.dto;

import com.placsp.monitor.model.Licitacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record LicitacionDto(
        String entryId,
        String expediente,
        String objeto,
        String estado,
        String tipoContrato,
        BigDecimal importeSinImpuestos,
        BigDecimal importeConImpuestos,
        String organoContratacion,
        String organoId,
        String cpvCodes,
        LocalDate fechaLimiteOfertas,
        LocalTime horaLimiteOfertas,
        String tipoProcedimiento,
        String lugarEjecucion,
        String nutsCode,
        String enlacePlacsp,
        String urlPcap,
        String urlPpt,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaIngesta
) {

    public static LicitacionDto fromEntity(Licitacion entity) {
        return new LicitacionDto(
                entity.getEntryId(),
                entity.getExpediente(),
                entity.getObjeto(),
                entity.getEstado(),
                entity.getTipoContrato(),
                entity.getImporteSinImpuestos(),
                entity.getImporteConImpuestos(),
                entity.getOrganoContratacion(),
                entity.getOrganoId(),
                entity.getCpvCodes(),
                entity.getFechaLimiteOfertas(),
                entity.getHoraLimiteOfertas(),
                entity.getTipoProcedimiento(),
                entity.getLugarEjecucion(),
                entity.getNutsCode(),
                entity.getEnlacePlacsp(),
                entity.getUrlPcap(),
                entity.getUrlPpt(),
                entity.getFechaActualizacion(),
                entity.getFechaIngesta()
        );
    }
}
