package com.consulthub.controller;

import com.consulthub.legacy.booking.Booking;
import com.consulthub.legacy.booking.model.Client;
import com.consulthub.legacy.booking.model.Service;
import com.consulthub.legacy.consultant.AvailabilitySlot;
import com.consulthub.legacy.consultant.Consultant;
import com.consulthub.service.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private DataStore store;

    // GET /api/bookings
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        store.bookingService.getBookingHistory().forEach(b -> result.add(bookingToMap(b)));
        return ResponseEntity.ok(result);
    }

    // GET /api/bookings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable String id) {
        try {
            return ResponseEntity.ok(bookingToMap(store.bookingService.getBooking(id)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/bookings/client/{clientId}
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Map<String, Object>>> getForClient(@PathVariable String clientId) {
        List<Map<String, Object>> result = store.bookingService.getBookingsForClient(clientId)
                .stream().map(this::bookingToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET /api/bookings/consultant/{consultantId}
    @GetMapping("/consultant/{consultantId}")
    public ResponseEntity<List<Map<String, Object>>> getForConsultant(@PathVariable String consultantId) {
        List<Map<String, Object>> result = store.bookingService.getBookingsForConsultant(consultantId)
                .stream().map(this::bookingToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // POST /api/bookings
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String clientId     = body.get("clientId");
        String consultantId = body.get("consultantId");
        String serviceId    = body.get("serviceId");
        String slotId       = body.get("slotId");

        Client     client     = store.clients.get(clientId);
        Consultant consultant = store.consultants.get(consultantId);
        Service    service    = store.services.get(serviceId);

        if (client == null)     return ResponseEntity.badRequest().body(Map.of("error", "Client not found"));
        if (consultant == null) return ResponseEntity.badRequest().body(Map.of("error", "Consultant not found"));
        if (service == null)    return ResponseEntity.badRequest().body(Map.of("error", "Service not found"));

        AvailabilitySlot slot = consultant.findAvailableSlot(slotId);
        if (slot == null) return ResponseEntity.badRequest().body(Map.of("error", "Slot not available"));

        try {
            Booking booking = store.bookingService.createBooking(client, consultant, service, slot);
            store.saveBooking(booking);          // persist to DB
            store.saveSlot(consultantId, slot);  // persist slot availability change to DB
            return ResponseEntity.ok(bookingToMap(booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/bookings/{id}/accept
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable String id) {
        try {
            Booking b = store.bookingService.getBooking(id);
            Consultant consultant = store.consultants.get(b.getConsultant().getConsultantId());
            if (consultant == null) return ResponseEntity.badRequest().body(Map.of("error", "Consultant not found"));
            consultant.acceptBooking(b);
            store.saveBooking(b);                // persist state change to DB
            return ResponseEntity.ok(bookingToMap(b));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/bookings/{id}/reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id) {
        try {
            Booking b = store.bookingService.getBooking(id);
            Consultant consultant = store.consultants.get(b.getConsultant().getConsultantId());
            if (consultant == null) return ResponseEntity.badRequest().body(Map.of("error", "Consultant not found"));
            consultant.rejectBooking(b);
            store.saveBooking(b);                // persist state change to DB
            if (b.getSlot() != null) {
                store.saveSlot(b.getConsultant().getConsultantId(), b.getSlot()); // slot released
            }
            return ResponseEntity.ok(bookingToMap(b));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/bookings/{id}/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id) {
        try {
            store.bookingService.cancelBooking(id);
            Booking b = store.bookingService.getBooking(id);
            store.saveBooking(b);                // persist state change to DB
            if (b.getSlot() != null) {
                store.saveSlot(b.getConsultant().getConsultantId(), b.getSlot()); // slot may be released
            }
            return ResponseEntity.ok(bookingToMap(b));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/bookings/{id}/complete
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable String id) {
        try {
            Booking b = store.bookingService.getBooking(id);
            Consultant consultant = store.consultants.get(b.getConsultant().getConsultantId());
            if (consultant == null) return ResponseEntity.badRequest().body(Map.of("error", "Consultant not found"));
            consultant.completeBooking(b);
            store.saveBooking(b);                // persist state change to DB
            return ResponseEntity.ok(bookingToMap(b));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    Map<String, Object> bookingToMap(Booking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",             b.getBookingId());
        m.put("clientId",       b.getClient().getClientId());
        m.put("clientName",     b.getClient().getName());
        m.put("consultantId",   b.getConsultant().getConsultantId());
        m.put("consultantName", b.getConsultant().getName());
        m.put("serviceId",      b.getService().getServiceId());
        m.put("serviceName",    b.getService().getName());
        m.put("slotId",         b.getSlot() != null ? b.getSlot().getSlotId() : null);
        m.put("slotStart",      b.getSlot() != null && b.getSlot().getStartDateTime() != null
                ? b.getSlot().getStartDateTime().toString() : null);
        m.put("amount",         b.getTotalAmount());
        m.put("state",          b.getState().toString());
        m.put("createdAt",      b.getCreatedAt().toString());
        return m;
    }
}
