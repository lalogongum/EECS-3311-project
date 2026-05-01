package com.consulthub.legacy.booking.state;

import com.consulthub.legacy.booking.Booking;


public class RejectedState implements BookingState {

    @Override
    public void handle(Booking booking) {
        System.out.println("Booking " + booking.getBookingId()
                + ": Booking was rejected. No further action.");
    }

    @Override
    public String toString() {
        return "REJECTED";
    }
}
