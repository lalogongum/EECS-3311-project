package com.consulthub.legacy.booking;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.consulthub.legacy.admin.CancellationPolicy;
import com.consulthub.legacy.booking.model.Client;
import com.consulthub.legacy.booking.model.Service;
import com.consulthub.legacy.booking.state.BookingState;
import com.consulthub.legacy.booking.state.CancelledState;
import com.consulthub.legacy.booking.state.RequestedState;
import com.consulthub.legacy.consultant.AvailabilitySlot;
import com.consulthub.legacy.consultant.Consultant;

/**
 * Booking class that represents a booking in the system.
 * It uses the State design pattern to manage its lifecycle states.
 */
public class Booking {

    private String bookingId;
    private double totalAmount;
    private BookingState currentState;
    private Client client;
    private Consultant consultant;
    private Service service;
    private AvailabilitySlot slot;
    private LocalDateTime createdAt;

    public Booking(String bookingId, double totalAmount, Client client,
            Consultant consultant, Service service, AvailabilitySlot slot) {
        this.bookingId = bookingId;
        this.totalAmount = totalAmount;
        this.currentState = new RequestedState();
        this.client = client;
        this.consultant = consultant;
        this.service = service;
        this.slot = slot;
        this.createdAt = LocalDateTime.now();
    }

    public void setState(BookingState state) {
        this.currentState = state;
    }

    public BookingState getState() {
        return currentState;
    }

    public void proceed() {
        currentState.handle(this);
    }

    /**
     * Cancel the booking if it is allowed by both lifecycle state and configured policy.
     */
    public void cancel() {
        String stateName = currentState.toString();
        ArrayList<BookingState> terminalStates = CancellationPolicy.getInstance().getCancellationPolicy();
        for (int i = 0; i < terminalStates.size(); i++) {
            if (stateName.equals(terminalStates.get(i).toString())) {
                System.out.println("Booking " + bookingId
                        + ": Cannot cancel — already in terminal state " + stateName + ".");
                return;
            }
        }

        if (!CancellationPolicy.getInstance().canCancel(this)) {
            System.out.println("Booking " + bookingId
                    + ": Cannot cancel — booking is inside the configured cancellation window.");
            return;
        }

        System.out.println("Booking " + bookingId + ": Cancelling from " + stateName + ".");
        setState(new CancelledState());
        releaseSlot();
    }

    public void releaseSlot() {
        if (slot != null) {
            slot.markAvailable();
        }
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Client getClient() {
        return client;
    }

    public Consultant getConsultant() {
        return consultant;
    }

    public Service getService() {
        return service;
    }

    public AvailabilitySlot getSlot() {
        return slot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Booking{id='" + bookingId + "', client=" + client.getName()
                + ", consultant=" + consultant.getName()
                + ", service=" + service.getName()
                + ", slot=" + slot.getSlotId()
                + ", amount=" + totalAmount
                + ", state=" + currentState + '}';
    }
}