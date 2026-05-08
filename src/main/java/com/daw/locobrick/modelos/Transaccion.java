package com.daw.locobrick.modelos;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long transactionID;

    // NUEVO: Enlace directo al inversor para saber de quién es el ingreso/retiro
    @ManyToOne
    @JoinColumn(name = "investorID", nullable = false)
    private Inversor inversor;

    // AHORA ES NULLABLE (Para ingresos/retiros no hay participación inmobiliaria)
    @ManyToOne
    @JoinColumn(name = "participationID", nullable = true)
    private Participacion participacion;

    @ManyToOne
    @JoinColumn(name = "commissionID", nullable = true)
    private Comision comision;

    @Column(nullable = false, length = 50)
    private String type; // "Compra", "Venta", "Ingreso", "Retirada"

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private LocalDate date;

    public Transaccion() {
    }

    // Constructor completo para Compras y Ventas de propiedades
    public Transaccion(Inversor inversor, Participacion participacion, Comision comision, String type, Long amount, LocalDate date) {
        this.inversor = inversor;
        this.participacion = participacion;
        this.comision = comision;
        this.type = type;
        this.amount = amount;
        this.date = date;
    }

    // Constructor simplificado para Ingresos y Retiradas de dinero
    public Transaccion(Inversor inversor, String type, Long amount, LocalDate date) {
        this.inversor = inversor;
        this.type = type;
        this.amount = amount;
        this.date = date;
    }

    public long getTransactionID() { return transactionID; }
    public void setTransactionID(long transactionID) { this.transactionID = transactionID; }

    public Inversor getInversor() { return inversor; }
    public void setInversor(Inversor inversor) { this.inversor = inversor; }

    public Participacion getParticipacion() { return participacion; }
    public void setParticipacion(Participacion participacion) { this.participacion = participacion; }

    public Comision getComision() { return comision; }
    public void setComision(Comision comision) { this.comision = comision; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}