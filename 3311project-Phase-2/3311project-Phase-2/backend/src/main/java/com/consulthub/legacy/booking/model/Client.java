package com.consulthub.legacy.booking.model;

/**
 * Represents a client who requests a booking.
 */
public class Client {

    private String clientId;
    private String name;
    private String email;

    public Client(String clientId, String name, String email) {
        this.clientId = clientId;
        this.name = name;
        this.email = email;
    }

    public String getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Client{id='" + clientId + "', name='" + name + "'}";
    }
}
