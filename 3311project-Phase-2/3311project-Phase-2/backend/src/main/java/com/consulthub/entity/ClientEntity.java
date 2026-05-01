package com.consulthub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class ClientEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    public ClientEntity() {}

    public ClientEntity(String clientId, String name, String email) {
        this.clientId = clientId;
        this.name     = name;
        this.email    = email;
    }

    public String getClientId() { return clientId; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
}
