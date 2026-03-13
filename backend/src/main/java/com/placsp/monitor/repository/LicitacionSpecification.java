package com.placsp.monitor.repository;

import com.placsp.monitor.model.Licitacion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public final class LicitacionSpecification {

    private LicitacionSpecification() {
    }

    public static Specification<Licitacion> byEstado(String estado) {
        if (estado == null || estado.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Licitacion> byTipoContrato(String tipoContrato) {
        if (tipoContrato == null || tipoContrato.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("tipoContrato"), tipoContrato);
    }

    /**
     * Filtra licitaciones donde AL MENOS UN código CPV empieza por el prefijo.
     * Como cpvCodes es un string separado por comas, buscamos al inicio o tras coma.
     */
    public static Specification<Licitacion> byCpvPrefix(String cpv) {
        if (cpv == null || cpv.isBlank()) return null;
        return (root, query, cb) -> cb.or(
                cb.like(root.get("cpvCodes"), cpv + "%"),
                cb.like(root.get("cpvCodes"), "%," + cpv + "%")
        );
    }

    public static Specification<Licitacion> byTexto(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String pattern = "%" + texto.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("objeto")), pattern),
                cb.like(cb.lower(root.get("organoContratacion")), pattern),
                cb.like(cb.lower(root.get("expediente")), pattern)
        );
    }

    public static Specification<Licitacion> byImporteMin(BigDecimal importeMin) {
        if (importeMin == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("importeSinImpuestos"), importeMin);
    }

    public static Specification<Licitacion> byImporteMax(BigDecimal importeMax) {
        if (importeMax == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("importeSinImpuestos"), importeMax);
    }

    public static Specification<Licitacion> byNutsCode(String nutsCode) {
        if (nutsCode == null || nutsCode.isBlank()) return null;
        return (root, query, cb) -> cb.like(root.get("nutsCode"), nutsCode + "%");
    }

    public static Specification<Licitacion> byProcedimiento(String procedimiento) {
        if (procedimiento == null || procedimiento.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("tipoProcedimiento"), procedimiento);
    }

    /**
     * Filtra licitaciones donde TODOS los códigos CPV empiezan por el prefijo dado.
     * Genera condiciones NOT LIKE para excluir códigos tras coma que no coincidan.
     */
    public static Specification<Licitacion> byCpvExclusive(String cpv) {
        if (cpv == null || cpv.isBlank()) return null;
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            // El primer código debe empezar por el prefijo
            predicates.add(cb.like(root.get("cpvCodes"), cpv + "%"));
            // Excluir cualquier código tras coma que no empiece por el prefijo
            for (int pos = 0; pos < cpv.length(); pos++) {
                char prefixChar = cpv.charAt(pos);
                String prefixSoFar = cpv.substring(0, pos);
                for (char c = '0'; c <= '9'; c++) {
                    if (c != prefixChar) {
                        predicates.add(cb.notLike(root.get("cpvCodes"), "%," + prefixSoFar + c + "%"));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Licitacion> byPlazoActivo(Integer dias) {
        if (dias == null) return null;
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        return (root, query, cb) -> cb.between(root.get("fechaLimiteOfertas"), hoy, limite);
    }
}
