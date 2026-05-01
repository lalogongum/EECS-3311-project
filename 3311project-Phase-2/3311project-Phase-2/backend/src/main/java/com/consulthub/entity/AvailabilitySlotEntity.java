package com.consulthub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "availability_slots")
public class AvailabilitySlotEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id", nullable = false)
    private ConsultantEntity consultant;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private boolean available = true;

    public AvailabilitySlotEntity() {}

    public AvailabilitySlotEntity(String slotId, ConsultantEntity consultant,
                                  LocalDateTime start, LocalDateTime end) {
        this.slotId = slotId;
        this.consultant = consultant;
        this.startDateTime = start;
        this.endDateTime = end;
        this.available = true;
    }

    public String getSlotId()               { return slotId; }
    public ConsultantEntity getConsultant() { return consultant; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public LocalDateTime getEndDateTime()   { return endDateTime; }
    public boolean isAvailable()            { return available; }
    public void setAvailable(boolean v)     { this.available = v; }
}
