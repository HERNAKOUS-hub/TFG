package com.daw.locobrick.modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "Propiedades")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long propertyID;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- CAMBIO A CÉNTIMOS ---
    private Long value;

    private Long valorRestante;

    private String status;

    private Long rentaMensual; // Renta que genera al mes (en céntimos)

    @ManyToOne
    @JoinColumn(name = "zoneID")
    private Zona zona;

    public Propiedad() {
    }

    public Propiedad(String name, Zona zona, Long value, String description, String status) {
        this.name = name;
        this.zona = zona;
        this.value = value;
        this.valorRestante = value;
        this.description = description;
        this.status = status;
    }

    public Long getPropertyID() {
        return propertyID;
    }

    public void setPropertyID(Long propertyID) {
        this.propertyID = propertyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getValorRestante() {
        return valorRestante;
    }

    public void setValorRestante(Long valorRestante) {
        this.valorRestante = valorRestante;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public Long getRentaMensual() {
        return rentaMensual;
    }

    public void setRentaMensual(Long rentaMensual) {
        this.rentaMensual = rentaMensual;
    }
}