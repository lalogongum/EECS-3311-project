package com.consulthub.controller;

import com.consulthub.legacy.admin.Admin;
import com.consulthub.legacy.consultant.AvailabilitySlot;
import com.consulthub.legacy.consultant.Consultant;
import com.consulthub.service.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consultants")
public class ConsultantController {

    @Autowired
    private DataStore store;

    // GET /api/consultants
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllConsultants() {
        List<Map<String, Object>> result = store.consultants.values().stream()
                .map(this::consultantToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET /api/consultants/approved
    @GetMapping("/approved")
    public ResponseEntity<List<Map<String, Object>>> getApproved() {
        List<Map<String, Object>> result = store.consultants.values().stream()
                .filter(c -> "APPROVED".equals(c.getStatus()))
                .map(this::consultantToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET /api/consultants/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getConsultant(@PathVariable String id) {
        Consultant c = store.consultants.get(id);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(consultantToMap(c));
    }

    // POST /api/consultants
    @PostMapping
    public ResponseEntity<?> registerConsultant(@RequestBody Map<String, String> body) {
        String name  = body.get("name");
        String email = body.get("email");
        if (name == null || email == null)
            return ResponseEntity.badRequest().body(Map.of("error", "name and email required"));

        // Use UUID-based ID to avoid collisions with seeded IDs (c001, c002, c003)
        String id = "c-" + UUID.randomUUID().toString().substring(0, 8);
        Consultant c = new Consultant(id, name, email);
        store.consultants.put(id, c);
        store.saveNewConsultant(c);          // persist to DB
        return ResponseEntity.ok(consultantToMap(c));
    }

    // PUT /api/consultants/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id) {
        Consultant c = store.consultants.get(id);
        if (c == null) return ResponseEntity.notFound().build();
        Admin.getInstance().approveConsultantRegistration(c);
        store.saveConsultant(c);             // persist status change to DB
        return ResponseEntity.ok(consultantToMap(c));
    }

    // PUT /api/consultants/{id}/reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id) {
        Consultant c = store.consultants.get(id);
        if (c == null) return ResponseEntity.notFound().build();
        Admin.getInstance().rejectConsultantRegistration(c);
        store.saveConsultant(c);             // persist status change to DB
        return ResponseEntity.ok(consultantToMap(c));
    }

    // GET /api/consultants/{id}/slots
    @GetMapping("/{id}/slots")
    public ResponseEntity<?> getSlots(@PathVariable String id) {
        Consultant c = store.consultants.get(id);
        if (c == null) return ResponseEntity.notFound().build();
        List<Map<String, Object>> slots = c.getAvailabilitySlots().stream()
                .map(this::slotToMap).collect(Collectors.toList());
        return ResponseEntity.ok(slots);
    }

    // POST /api/consultants/{id}/slots
    @PostMapping("/{id}/slots")
    public ResponseEntity<?> addSlot(@PathVariable String id, @RequestBody Map<String, String> body) {
        Consultant c = store.consultants.get(id);
        if (c == null) return ResponseEntity.notFound().build();
        try {
            LocalDateTime start  = LocalDateTime.parse(body.get("start"));
            LocalDateTime end    = LocalDateTime.parse(body.get("end"));
            String slotId        = "s-" + System.currentTimeMillis();
            AvailabilitySlot slot = new AvailabilitySlot(slotId, start, end);
            c.addAvailabilitySlot(slot);
            store.saveSlot(id, slot);        // persist new slot to DB
            return ResponseEntity.ok(slotToMap(slot));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/consultants/{id}/slots/{slotId}
    @DeleteMapping("/{id}/slots/{slotId}")
    public ResponseEntity<?> removeSlot(@PathVariable String id, @PathVariable String slotId) {
        Consultant c = store.consultants.get(id);
        if (c == null) return ResponseEntity.notFound().build();
        boolean removed = c.getAvailabilitySlots().removeIf(s -> s.getSlotId().equals(slotId) && s.isAvailable());
        if (!removed) return ResponseEntity.badRequest().body(Map.of("error", "Slot not found or already booked"));
        store.deleteSlot(slotId);            // delete from DB
        return ResponseEntity.ok(Map.of("message", "Slot removed"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> consultantToMap(Consultant c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",     c.getConsultantId());
        m.put("name",   c.getName());
        m.put("email",  c.getEmail());
        m.put("status", c.getStatus());
        m.put("availableSlots", c.getAvailabilitySlots().stream()
                .filter(AvailabilitySlot::isAvailable).count());
        m.put("slots",  c.getAvailabilitySlots().stream()
                .map(this::slotToMap).collect(Collectors.toList()));
        return m;
    }

    private Map<String, Object> slotToMap(AvailabilitySlot s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",        s.getSlotId());
        m.put("start",     s.getStartDateTime() != null ? s.getStartDateTime().toString() : null);
        m.put("end",       s.getEndDateTime()   != null ? s.getEndDateTime().toString()   : null);
        m.put("available", s.isAvailable());
        return m;
    }
}
