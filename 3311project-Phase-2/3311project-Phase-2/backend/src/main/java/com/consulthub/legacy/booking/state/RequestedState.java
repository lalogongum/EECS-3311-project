package com.consulthub.legacy.booking.state;

import com.consulthub.legacy.booking.Booking;

/**
 * Initial state when a client submits a booking request.
 * Transitions to ConfirmedState when handled (consultant accepts).
 */
public class RequestedState implements BookingState {

    @Override
    public void handle(Booking booking) {
        System.out.println("Booking " + booking.getBookingId()
                + ": Request received. Moving to Confirmed.");
        booking.setState(new ConfirmedState());
    }

    @Override
    public String toString() {
        return "REQUESTED";
    }
}
