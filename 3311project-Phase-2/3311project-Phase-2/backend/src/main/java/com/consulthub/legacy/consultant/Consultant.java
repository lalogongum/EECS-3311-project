package com.consulthub.legacy.consultant;

import java.util.ArrayList;
import java.util.List;

import com.consulthub.legacy.admin.NotificationPolicy;
import com.consulthub.legacy.booking.Booking;
import com.consulthub.legacy.booking.state.CompletedState;
import com.consulthub.legacy.booking.state.RejectedState;

public class Consultant {

    private String consultantId;
    private String name;
    private String email;
    private String status;

    private List<AvailabilitySlot> availabilitySlots;

    public Consultant(String consultantId, String name, String email) {
        this.consultantId = consultantId;
        this.name = name;
        this.email = email;
        this.status = "PENDING";
        this.availabilitySlots = new ArrayList<>();
    }

    public void manageAvailability(List<AvailabilitySlot> slots) {
        this.availabilitySlots = slots;
    }

    public void addAvailabilitySlot(AvailabilitySlot slot) {
        this.availabilitySlots.add(slot);
    }

    public AvailabilitySlot findAvailableSlot(String slotId) {
        for (AvailabilitySlot slot : availabilitySlots) {
            if (slot.getSlotId().equals(slotId) && slot.isAvailable()) {
                return slot;
            }
        }
        return null;
    }

    public void acceptBooking(Booking booking) {
        ensureApproved("accept");
        if (booking == null) {
            return;
        }
        if (!"REQUESTED".equals(booking.getState().toString())) {
            throw new IllegalStateException("Only REQUESTED bookings can be accepted.");
        }
        booking.proceed();
        booking.proceed();
        if (NotificationPolicy.getInstance().getNotificationPolicy()) {
            System.out.println("[Notification] Booking " + booking.getBookingId() + " confirmed and awaiting payment.");
        }
    }

    public void rejectBooking(Booking booking) {
        ensureApproved("reject");
        if (booking == null) {
            return;
        }
        if (!"REQUESTED".equals(booking.getState().toString())) {
            throw new IllegalStateException("Only REQUESTED bookings can be rejected.");
        }
        System.out.println("Consultant " + this.name + " has rejected the booking: " + booking.getBookingId());
        booking.setState(new RejectedState());
        booking.releaseSlot();
        if (NotificationPolicy.getInstance().getNotificationPolicy()) {
            System.out.println("[Notification] Client notified that booking " + booking.getBookingId() + " was rejected.");
        }
    }

    public void completeBooking(Booking booking) {
        ensureApproved("complete");
        if (booking == null) {
            return;
        }
        if (!"PAID".equals(booking.getState().toString())) {
            throw new IllegalStateException("A booking can only be completed after payment is processed.");
        }
        booking.setState(new CompletedState());
        if (NotificationPolicy.getInstance().getNotificationPolicy()) {
            System.out.println("[Notification] Booking " + booking.getBookingId() + " marked as completed.");
        }
    }

    private void ensureApproved(String action) {
        if (!status.equals("APPROVED")) {
            throw new IllegalStateException(
                    "Consultant " + name + " is not approved and cannot " + action + " bookings.");
        }
    }

    public String getConsultantId() {
        return consultantId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AvailabilitySlot> getAvailabilitySlots() {
        return availabilitySlots;
    }
}
