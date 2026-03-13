package com.placsp.monitor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "analisis_pliegos")
public class AnalisisPliego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String entryId;

    @Column(columnDefinition = "TEXT")
    private String resumenPcap;

    @Column(columnDefinition = "TEXT")
    private String resumenPpt;

    private LocalDateTime fechaAnalisis;

    public AnalisisPliego() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getResumenPcap() {
        return resumenPcap;
    }

    public void setResumenPcap(String resumenPcap) {
        this.resumenPcap = resumenPcap;
    }

    public String getResumenPpt() {
        return resumenPpt;
    }

    public void setResumenPpt(String resumenPpt) {
        this.resumenPpt = resumenPpt;
    }

    public LocalDateTime getFechaAnalisis() {
        return fechaAnalisis;
    }

    public void setFechaAnalisis(LocalDateTime fechaAnalisis) {
        this.fechaAnalisis = fechaAnalisis;
    }
}
