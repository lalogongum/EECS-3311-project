package com.consulthub.controller;

import com.consulthub.legacy.admin.*;
import com.consulthub.legacy.booking.state.*;
import com.consulthub.service.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DataStore store;

    // GET /api/admin/policies
    @GetMapping("/policies")
    public ResponseEntity<Map<String, Object>> getPolicies() {
        return ResponseEntity.ok(policiesToMap());
    }

    // PUT /api/admin/policies/cancellation
    @PutMapping("/policies/cancellation")
    public ResponseEntity<?> updateCancellation(@RequestBody Map<String, Object> body) {
        try {
            int hours = ((Number) body.get("hours")).intValue();
            Admin.getInstance().configurePolicy(
                new CancellationPolicyCommand(CancellationPolicy.getInstance(), hours));
            store.savePolicy("cancellationHours", String.valueOf(hours));   // persist to DB
            return ResponseEntity.ok(policiesToMap());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/admin/policies/pricing
    @PutMapping("/policies/pricing")
    public ResponseEntity<?> updatePricing(@RequestBody Map<String, Object> body) {
        try {
            double min = ((Number) body.get("min")).doubleValue();
            double max = ((Number) body.get("max")).doubleValue();
            Admin.getInstance().configurePolicy(
                new PricingPolicyCommand(PricingPolicy.getInstance(), min, max));
            store.savePolicy("priceMin", String.valueOf(min));   // persist to DB
            store.savePolicy("priceMax", String.valueOf(max));
            return ResponseEntity.ok(policiesToMap());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/admin/policies/notifications
    @PutMapping("/policies/notifications")
    public ResponseEntity<?> updateNotifications(@RequestBody Map<String, Object> body) {
        try {
            boolean enabled = (Boolean) body.get("enabled");
            Admin.getInstance().configurePolicy(
                new NotificationPolicyCommand(NotificationPolicy.getInstance(), enabled));
            store.savePolicy("notificationsEnabled", String.valueOf(enabled));   // persist to DB
            return ResponseEntity.ok(policiesToMap());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/admin/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long total    = 0, pending = 0, approved = 0;
        for (var c : store.consultants.values()) {
            total++;
            if ("PENDING".equals(c.getStatus()))  pending++;
            if ("APPROVED".equals(c.getStatus())) approved++;
        }
        long bookingCount = 0;
        for (var ignored : store.bookingService.getBookingHistory()) bookingCount++;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalConsultants",    total);
        m.put("pendingConsultants",  pending);
        m.put("approvedConsultants", approved);
        m.put("totalBookings",       bookingCount);
        m.put("totalClients",        store.clients.size());
        m.put("totalServices",       store.services.size());
        return ResponseEntity.ok(m);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> policiesToMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("cancellationHours",    CancellationPolicy.getInstance().getMinimumHoursBeforeStart());
        m.put("priceMin",             PricingPolicy.getInstance().getMinPrice());
        m.put("priceMax",             PricingPolicy.getInstance().getMaxPrice());
        m.put("notificationsEnabled", NotificationPolicy.getInstance().getNotificationPolicy());
        m.put("refundPolicy",         RefundPolicy.getInstance().getRefundPolicy());
        return m;
    }
}
