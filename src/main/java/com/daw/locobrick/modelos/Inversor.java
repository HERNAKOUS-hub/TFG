package com.daw.locobrick.modelos;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Inversores")
public class Inversor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long investorID;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String password;

    // --- CAMBIO A CÉNTIMOS ---
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long saldo = 0L; // 10000 = 100,00 €

    private LocalDate registrationDate;

    @Column(nullable = false)
    private String rol = "USER";

    @Column(name = "ultimos_digitos_tarjeta", length = 4)
    private String ultimosDigitosTarjeta;

    // --- NUEVO CAMPO: VERIFICACIÓN DE CUENTA ---
    @Column(columnDefinition = "boolean default false")
    private boolean cuentaVerificada = false;

    // --- NUEVOS CAMPOS: KYC (Conoce a tu cliente) ---
    @Column(unique = true, nullable = false, length = 20)
    private String dni;

    // ¡AQUÍ ESTÁ EL CAMBIO! Ahora todos nacen siendo de España por defecto
    @Column(name = "residencia_fiscal", nullable = false)
    private String residenciaFiscal = "España";

    @Column(nullable = false)
    private String direccion;

    public Inversor() {
    }

    public Inversor(String firstName, String lastName, String email, String password, LocalDate registrationDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
        this.saldo = 0L;
    }

    public Long getInvestorID() {
        return investorID;
    }

    public void setInvestorID(Long investorID) {
        this.investorID = investorID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getSaldo() {
        return saldo;
    }

    public void setSaldo(Long saldo) {
        this.saldo = saldo;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getUltimosDigitosTarjeta() {
        return ultimosDigitosTarjeta;
    }

    public void setUltimosDigitosTarjeta(String ultimosDigitosTarjeta) {
        this.ultimosDigitosTarjeta = ultimosDigitosTarjeta;
    }

    public boolean isCuentaVerificada() {
        return cuentaVerificada;
    }

    public void setCuentaVerificada(boolean cuentaVerificada) {
        this.cuentaVerificada = cuentaVerificada;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getResidenciaFiscal() {
        return residenciaFiscal;
    }

    public void setResidenciaFiscal(String residenciaFiscal) {
        this.residenciaFiscal = residenciaFiscal;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}