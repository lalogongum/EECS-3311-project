package com.consulthub.legacy.booking.state;

import com.consulthub.legacy.booking.Booking;


public class CompletedState implements BookingState {

    @Override
    public void handle(Booking booking) {
        System.out.println("Booking " + booking.getBookingId()
                + ": Already completed. No further action.");
    }

    @Override
    public String toString() {
        return "COMPLETED";
    }
}
