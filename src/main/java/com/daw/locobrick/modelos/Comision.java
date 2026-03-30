package com.daw.locobrick.modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "Comisiones")
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long commissionID;

    @Column(nullable = false, length = 50)
    private String type;

    // --- CAMBIO A ENTEROS (200 = 2.00%) ---
    @Column(nullable = false)
    private Long rate;

    @Column(length = 255)
    private String description;

    public Comision() {
    }

    public Comision(String type, Long rate, String description) {
        this.type = type;
        this.rate = rate;
        this.description = description;
    }

    public long getCommissionID() { return commissionID; }
    public void setCommissionID(long commissionID) { this.commissionID = commissionID; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getRate() { return rate; }
    public void setRate(Long rate) { this.rate = rate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}