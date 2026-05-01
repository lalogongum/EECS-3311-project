package com.consulthub.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultants")
public class ConsultantEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String consultantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "consultant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AvailabilitySlotEntity> slots = new ArrayList<>();

    public ConsultantEntity() {}

    public ConsultantEntity(String consultantId, String name, String email, String status) {
        this.consultantId = consultantId;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public String getConsultantId() { return consultantId; }
    public String getName()         { return name; }
    public String getEmail()        { return email; }
    public String getStatus()       { return status; }
    public void setStatus(String s) { this.status = s; }
    public List<AvailabilitySlotEntity> getSlots() { return slots; }
}
