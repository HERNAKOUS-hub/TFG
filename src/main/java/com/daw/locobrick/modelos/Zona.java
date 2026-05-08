package com.daw.locobrick.modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "Zonas")
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long zoneID;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String district;

    public Zona() {
    }

    public Zona(String city, String district) {
        this.city = city;
        this.district = district;
    }

    public long getZoneID() { return zoneID; }
    public void setZoneID(long zoneID) { this.zoneID = zoneID; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
}