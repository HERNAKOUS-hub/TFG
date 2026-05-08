package com.daw.locobrick.modelos;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Participaciones")
public class Participacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long participationID;

    @ManyToOne
    @JoinColumn(name = "investorID")
    private Inversor inversor;

    @ManyToOne
    @JoinColumn(name = "propertyID")
    private Propiedad propiedad;

    // --- CAMBIO A CÉNTIMOS/BASE 10000 ---
    @Column(nullable = false)
    private Long percentage; // 10000 = 100.00%

    @Column(nullable = false)
    private Long investmentAmount; // 15000 = 150.00 €

    @Column(nullable = false)
    private LocalDate purchaseDate;

    public Participacion() {
    }

    public Participacion(Inversor inversor, Propiedad propiedad, Long percentage, Long amount, LocalDate date) {
        this.inversor = inversor;
        this.propiedad = propiedad;
        this.percentage = percentage;
        this.investmentAmount = amount;
        this.purchaseDate = date;
    }

    public long getParticipationID() { return participationID; }
    public void setParticipationID(long participationID) { this.participationID = participationID; }

    public Inversor getInversor() { return inversor; }
    public void setInversor(Inversor inversor) { this.inversor = inversor; }

    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }

    public Long getPercentage() { return percentage; }
    public void setPercentage(Long percentage) { this.percentage = percentage; }

    public Long getInvestmentAmount() { return investmentAmount; }
    public void setInvestmentAmount(Long investmentAmount) { this.investmentAmount = investmentAmount; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
}