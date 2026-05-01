package com.consulthub.legacy.booking.state;

import com.consulthub.legacy.booking.Booking;

/**
 * The booking has been confirmed by the consultant.
 * Transitions to PendingPaymentState when handled.
 */
public class ConfirmedState implements BookingState {

    @Override
    public void handle(Booking booking) {
        System.out.println("Booking " + booking.getBookingId()
                + ": Confirmed. Awaiting payment.");
        booking.setState(new PendingPaymentState());
    }

    @Override
    public String toString() {
        return "CONFIRMED";
    }
}
