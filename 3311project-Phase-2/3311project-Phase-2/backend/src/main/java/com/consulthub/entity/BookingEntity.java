package com.consulthub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class BookingEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String bookingId;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String consultantId;

    @Column(nullable = false)
    private String serviceId;

    private String slotId;

    private double totalAmount;

    @Column(nullable = false)
    private String state;

    private LocalDateTime createdAt;

    public BookingEntity() {}

    public BookingEntity(String bookingId, String clientId, String consultantId,
                         String serviceId, String slotId, double totalAmount,
                         String state, LocalDateTime createdAt) {
        this.bookingId    = bookingId;
        this.clientId     = clientId;
        this.consultantId = consultantId;
        this.serviceId    = serviceId;
        this.slotId       = slotId;
        this.totalAmount  = totalAmount;
        this.state        = state;
        this.createdAt    = createdAt;
    }

    public String        getBookingId()    { return bookingId; }
    public String        getClientId()     { return clientId; }
    public String        getConsultantId() { return consultantId; }
    public String        getServiceId()    { return serviceId; }
    public String        getSlotId()       { return slotId; }
    public double        getTotalAmount()  { return totalAmount; }
    public String        getState()        { return state; }
    public void          setState(String s){ this.state = s; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
}
