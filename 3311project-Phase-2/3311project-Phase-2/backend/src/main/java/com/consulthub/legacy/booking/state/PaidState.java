package com.consulthub.legacy.booking.state;

import com.consulthub.legacy.booking.Booking;


public class PaidState implements BookingState {

    @Override
    public void handle(Booking booking) {
        System.out.println("Booking " + booking.getBookingId()
                + ": Service delivered. Booking completed.");
        booking.setState(new CompletedState());
    }

    @Override
    public String toString() {
        return "PAID";
    }
}
