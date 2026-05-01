package com.consulthub.legacy.booking.state;

import com.consulthub.legacy.booking.Booking;


public class PendingPaymentState implements BookingState {

    @Override
    public void handle(Booking booking) {
        System.out.println("Booking " + booking.getBookingId()
                + ": Payment received. Marked as Paid.");
        booking.setState(new PaidState());
    }

    @Override
    public String toString() {
        return "PENDING_PAYMENT";
    }
}
