package com.consulthub.legacy.booking.state;

import com.consulthub.legacy.booking.Booking;


public class CancelledState implements BookingState {

    @Override
    public void handle(Booking booking) {
        System.out.println("Booking " + booking.getBookingId()
                + ": Booking was cancelled. No further action.");
    }

    @Override
    public String toString() {
        return "CANCELLED";
    }
}
