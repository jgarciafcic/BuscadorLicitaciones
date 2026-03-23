package com.placsp.monitor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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

    @Column(length = 1000)
    private String urlAnuncio;

    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaIngesta;

    // Duración del contrato
    private String duracionMedida;       // ej. "4"
    private String duracionUnidad;       // DAY, MON, ANN
    private LocalDate duracionInicio;
    private LocalDate duracionFin;

    // Urgencia
    private String urgencia;             // 1=Ordinaria, 2=Urgente

    // Prórroga
    @Lob
    private String prorroga;

    // Lotes
    @Lob
    private String lotes;                // JSON array con info de lotes

    // Criterios de adjudicación
    @Lob
    private String criteriosAdjudicacion; // JSON array

    // Solvencia
    @Lob
    private String solvenciaTecnica;

    @Lob
    private String solvenciaEconomica;

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

    public String getUrlAnuncio() {
        return urlAnuncio;
    }

    public void setUrlAnuncio(String urlAnuncio) {
        this.urlAnuncio = urlAnuncio;
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

    public String getDuracionMedida() { return duracionMedida; }
    public void setDuracionMedida(String duracionMedida) { this.duracionMedida = duracionMedida; }

    public String getDuracionUnidad() { return duracionUnidad; }
    public void setDuracionUnidad(String duracionUnidad) { this.duracionUnidad = duracionUnidad; }

    public LocalDate getDuracionInicio() { return duracionInicio; }
    public void setDuracionInicio(LocalDate duracionInicio) { this.duracionInicio = duracionInicio; }

    public LocalDate getDuracionFin() { return duracionFin; }
    public void setDuracionFin(LocalDate duracionFin) { this.duracionFin = duracionFin; }

    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }

    public String getProrroga() { return prorroga; }
    public void setProrroga(String prorroga) { this.prorroga = prorroga; }

    public String getLotes() { return lotes; }
    public void setLotes(String lotes) { this.lotes = lotes; }

    public String getCriteriosAdjudicacion() { return criteriosAdjudicacion; }
    public void setCriteriosAdjudicacion(String criteriosAdjudicacion) { this.criteriosAdjudicacion = criteriosAdjudicacion; }

    public String getSolvenciaTecnica() { return solvenciaTecnica; }
    public void setSolvenciaTecnica(String solvenciaTecnica) { this.solvenciaTecnica = solvenciaTecnica; }

    public String getSolvenciaEconomica() { return solvenciaEconomica; }
    public void setSolvenciaEconomica(String solvenciaEconomica) { this.solvenciaEconomica = solvenciaEconomica; }

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
