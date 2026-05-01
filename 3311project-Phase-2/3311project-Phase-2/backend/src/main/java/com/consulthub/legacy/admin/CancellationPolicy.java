package com.consulthub.legacy.admin;

import java.util.ArrayList;

import com.consulthub.legacy.booking.Booking;
import com.consulthub.legacy.booking.state.BookingState;
import com.consulthub.legacy.booking.state.CancelledState;
import com.consulthub.legacy.booking.state.CompletedState;
import com.consulthub.legacy.booking.state.RejectedState;

public class CancellationPolicy {
	private ArrayList<BookingState> finalStates;
	private int minimumHoursBeforeStart;
	
	private static final CancellationPolicy instance = new CancellationPolicy();
	
	private CancellationPolicy() {
		finalStates = new ArrayList<>();
		finalStates.add(new CompletedState());
		finalStates.add(new CancelledState());
		finalStates.add(new RejectedState());
		minimumHoursBeforeStart = 24;
	}
	
	public static CancellationPolicy getInstance() {
		return instance;
	}
	
	/**
	 * Return the policy's states that don't allow cancellation.
	 * @return the ArrayList<BookingState> representing the states that the policy prohibits cancellations in.
	 */
	public ArrayList<BookingState> getCancellationPolicy() {
		return this.finalStates;
	}

	public int getMinimumHoursBeforeStart() {
        return minimumHoursBeforeStart;
    }

	public boolean canCancel(Booking booking) {
        if (booking == null) {
            return false;
        }
        if (booking.getSlot() == null || booking.getSlot().getStartDateTime() == null) {
            return true;
        }
        long hoursUntilStart = java.time.Duration.between(java.time.LocalDateTime.now(),
                booking.getSlot().getStartDateTime()).toHours();
        return hoursUntilStart >= minimumHoursBeforeStart;
    }
	
	/**
	 * Change the value of this policy with the passed parameter
	 * @param newStates 	The ArrayList<BookingState> value that the policy's variable will be set to
	 */
	private void change(ArrayList<BookingState> newStates) {
		this.finalStates.clear();
		for(int i = 0; i < newStates.size(); i++) {
			this.finalStates.add(newStates.get(i));
		}
	}
	
	/**
	 * Invoke the method to change this policy's variable with the given parameter
	 * @param newStates 	The ArrayList<BookingState> value that the policy's variable will be set to
	 */
	public void applyChange(ArrayList<BookingState> newStates) {
		change(newStates);
	}

	/**
	 * Apply the cancellation window time
	 * @param minimumHoursBeforeStart 	The int value that the variable will be set to
	 */
	public void applyTimeWindowChange(int minimumHoursBeforeStart) {
        if (minimumHoursBeforeStart < 0) {
            throw new IllegalArgumentException("Minimum hours before start must be non-negative.");
        }
        this.minimumHoursBeforeStart = minimumHoursBeforeStart;
        System.out.println("Cancellation time window changed to " + minimumHoursBeforeStart + " hours before start.");
    }
}





