package com.consulthub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private String clientId;

    private double amount;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String methodType;

    private String methodMasked;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentEntity() {}

    public PaymentEntity(String transactionId, String bookingId, String clientId,
                         double amount, String state, String methodType,
                         String methodMasked, String description,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.transactionId = transactionId;
        this.bookingId     = bookingId;
        this.clientId      = clientId;
        this.amount        = amount;
        this.state         = state;
        this.methodType    = methodType;
        this.methodMasked  = methodMasked;
        this.description   = description;
        this.createdAt     = createdAt;
        this.updatedAt     = updatedAt;
    }

    public String        getTransactionId() { return transactionId; }
    public String        getBookingId()     { return bookingId; }
    public String        getClientId()      { return clientId; }
    public double        getAmount()        { return amount; }
    public String        getState()         { return state; }
    public void          setState(String s) { this.state = s; }
    public String        getMethodType()    { return methodType; }
    public String        getMethodMasked()  { return methodMasked; }
    public String        getDescription()   { return description; }
    public void          setDescription(String d) { this.description = d; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
}
