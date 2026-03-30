package com.daw.locobrick.modelos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TokenSeguridad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private String tipo; 
    private LocalDateTime fechaExpiracion;

    @ManyToOne
    @JoinColumn(name = "inversor_id")
    private Inversor inversor;

    public TokenSeguridad() {}

    public TokenSeguridad(String token, String tipo, Inversor inversor, int minutosValidez) {
        this.token = token;
        this.tipo = tipo;
        this.inversor = inversor;
        this.fechaExpiracion = LocalDateTime.now().plusMinutes(minutosValidez);
    }

    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public Inversor getInversor() { return inversor; }
    public void setInversor(Inversor inversor) { this.inversor = inversor; }
}
