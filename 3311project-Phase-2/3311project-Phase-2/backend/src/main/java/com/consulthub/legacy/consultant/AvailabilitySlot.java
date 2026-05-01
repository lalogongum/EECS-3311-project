package com.consulthub.legacy.consultant;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AvailabilitySlot {

    private String slotId;
    private Date date;
    private String startTime;
    private String endTime;
    private boolean isAvailable;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public AvailabilitySlot(String slotId, Date date, String startTime, String endTime) {
        this.slotId = slotId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = true;
        this.startDateTime = date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        this.endDateTime = this.startDateTime;
    }

    public AvailabilitySlot(String slotId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.slotId = slotId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.date = startDateTime == null ? null : Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        this.startTime = startDateTime == null ? null : startDateTime.toLocalTime().toString();
        this.endTime = endDateTime == null ? null : endDateTime.toLocalTime().toString();
        this.isAvailable = true;
    }

    public void markAvailable() {
        this.isAvailable = true;
    }

    public void markUnavailable() {
        this.isAvailable = false;
    }

    public String getSlotId() {
        return slotId;
    }

    public Date getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    @Override
    public String toString() {
        if (startDateTime != null) {
            return "AvailabilitySlot{id='" + slotId + "', start=" + startDateTime + ", end=" + endDateTime + ", available=" + isAvailable + '}';
        }
        return "AvailabilitySlot{id='" + slotId + "', date=" + date + ", start='" + startTime + "', end='" + endTime + "', available=" + isAvailable + '}';
    }
}
