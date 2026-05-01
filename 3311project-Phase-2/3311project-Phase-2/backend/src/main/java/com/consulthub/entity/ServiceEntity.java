package com.consulthub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String serviceId;

    @Column(nullable = false)
    private String name;

    private int durationMinutes;
    private double price;

    public ServiceEntity() {}

    public ServiceEntity(String serviceId, String name, int durationMinutes, double price) {
        this.serviceId       = serviceId;
        this.name            = name;
        this.durationMinutes = durationMinutes;
        this.price           = price;
    }

    public String getServiceId()      { return serviceId; }
    public String getName()           { return name; }
    public int    getDurationMinutes(){ return durationMinutes; }
    public double getPrice()          { return price; }
}
