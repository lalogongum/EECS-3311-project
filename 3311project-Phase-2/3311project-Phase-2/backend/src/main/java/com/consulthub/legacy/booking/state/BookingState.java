package com.consulthub.legacy.booking.state;

import com.consulthub.legacy.booking.Booking;

/**
 * State interface for the Booking State pattern.
 * Each concrete state defines how the booking behaves in that state.
 */
public interface BookingState {

    /**
     * Handle the current state logic and transition the booking
     * to the next appropriate state.
     *
     * @param booking the booking context
     */
    void handle(Booking booking);
}
