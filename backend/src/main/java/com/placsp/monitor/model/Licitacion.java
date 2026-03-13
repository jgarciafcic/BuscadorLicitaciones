package com.placsp.monitor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "licitaciones")
public class Licitacion {

    @Id
    @Column(length = 500)
    private String entryId;

    @Column(length = 500)
    private String expediente;

    @Column(length = 2000)
    private String objeto;

    private String estado;

    private String tipoContrato;

    private BigDecimal importeSinImpuestos;

    private BigDecimal importeConImpuestos;

    @Column(length = 500)
    private String organoContratacion;

    private String organoId;

    @Column(length = 5000)
    private String cpvCodes;

    private LocalDate fechaLimiteOfertas;

    private LocalTime horaLimiteOfertas;

    private String tipoProcedimiento;

    private String lugarEjecucion;

    private String nutsCode;

    @Column(length = 1000)
    private String enlacePlacsp;

    @Column(length = 1000)
    private String urlPcap;

    @Column(length = 1000)
    private String urlPpt;

    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaIngesta;

    public Licitacion() {
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getExpediente() {
        return expediente;
    }

    public void setExpediente(String expediente) {
        this.expediente = expediente;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public BigDecimal getImporteSinImpuestos() {
        return importeSinImpuestos;
    }

    public void setImporteSinImpuestos(BigDecimal importeSinImpuestos) {
        this.importeSinImpuestos = importeSinImpuestos;
    }

    public BigDecimal getImporteConImpuestos() {
        return importeConImpuestos;
    }

    public void setImporteConImpuestos(BigDecimal importeConImpuestos) {
        this.importeConImpuestos = importeConImpuestos;
    }

    public String getOrganoContratacion() {
        return organoContratacion;
    }

    public void setOrganoContratacion(String organoContratacion) {
        this.organoContratacion = organoContratacion;
    }

    public String getOrganoId() {
        return organoId;
    }

    public void setOrganoId(String organoId) {
        this.organoId = organoId;
    }

    public String getCpvCodes() {
        return cpvCodes;
    }

    public void setCpvCodes(String cpvCodes) {
        this.cpvCodes = cpvCodes;
    }

    public LocalDate getFechaLimiteOfertas() {
        return fechaLimiteOfertas;
    }

    public void setFechaLimiteOfertas(LocalDate fechaLimiteOfertas) {
        this.fechaLimiteOfertas = fechaLimiteOfertas;
    }

    public LocalTime getHoraLimiteOfertas() {
        return horaLimiteOfertas;
    }

    public void setHoraLimiteOfertas(LocalTime horaLimiteOfertas) {
        this.horaLimiteOfertas = horaLimiteOfertas;
    }

    public String getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public void setTipoProcedimiento(String tipoProcedimiento) {
        this.tipoProcedimiento = tipoProcedimiento;
    }

    public String getLugarEjecucion() {
        return lugarEjecucion;
    }

    public void setLugarEjecucion(String lugarEjecucion) {
        this.lugarEjecucion = lugarEjecucion;
    }

    public String getNutsCode() {
        return nutsCode;
    }

    public void setNutsCode(String nutsCode) {
        this.nutsCode = nutsCode;
    }

    public String getEnlacePlacsp() {
        return enlacePlacsp;
    }

    public void setEnlacePlacsp(String enlacePlacsp) {
        this.enlacePlacsp = enlacePlacsp;
    }

    public String getUrlPcap() {
        return urlPcap;
    }

    public void setUrlPcap(String urlPcap) {
        this.urlPcap = urlPcap;
    }

    public String getUrlPpt() {
        return urlPpt;
    }

    public void setUrlPpt(String urlPpt) {
        this.urlPpt = urlPpt;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDateTime getFechaIngesta() {
        return fechaIngesta;
    }

    public void setFechaIngesta(LocalDateTime fechaIngesta) {
        this.fechaIngesta = fechaIngesta;
    }

    @Override
    public String toString() {
        return "Licitacion{" +
                "entryId='" + entryId + '\'' +
                ", expediente='" + expediente + '\'' +
                ", objeto='" + objeto + '\'' +
                ", estado='" + estado + '\'' +
                ", tipoContrato='" + tipoContrato + '\'' +
                ", importeSinImpuestos=" + importeSinImpuestos +
                ", importeConImpuestos=" + importeConImpuestos +
                ", organoContratacion='" + organoContratacion + '\'' +
                ", organoId='" + organoId + '\'' +
                ", cpvCodes='" + cpvCodes + '\'' +
                ", fechaLimiteOfertas=" + fechaLimiteOfertas +
                ", horaLimiteOfertas=" + horaLimiteOfertas +
                ", tipoProcedimiento='" + tipoProcedimiento + '\'' +
                ", lugarEjecucion='" + lugarEjecucion + '\'' +
                ", nutsCode='" + nutsCode + '\'' +
                ", enlacePlacsp='" + enlacePlacsp + '\'' +
                ", fechaActualizacion=" + fechaActualizacion +
                ", fechaIngesta=" + fechaIngesta +
                '}';
    }
}
