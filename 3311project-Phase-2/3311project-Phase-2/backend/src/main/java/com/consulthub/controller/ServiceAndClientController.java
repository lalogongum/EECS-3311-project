package com.consulthub.controller;

import com.consulthub.legacy.booking.model.Client;
import com.consulthub.legacy.booking.model.Service;
import com.consulthub.service.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ServiceAndClientController {

    @Autowired
    private DataStore store;

    // ── Services ──────────────────────────────────────────────────────────────

    @GetMapping("/api/services")
    public ResponseEntity<List<Map<String, Object>>> getServices() {
        return ResponseEntity.ok(store.services.values().stream()
                .map(this::serviceToMap).collect(Collectors.toList()));
    }

    @GetMapping("/api/services/{id}")
    public ResponseEntity<?> getService(@PathVariable String id) {
        Service s = store.services.get(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(serviceToMap(s));
    }

    // ── Clients ───────────────────────────────────────────────────────────────

    @GetMapping("/api/clients")
    public ResponseEntity<List<Map<String, Object>>> getClients() {
        return ResponseEntity.ok(store.clients.values().stream()
                .map(this::clientToMap).collect(Collectors.toList()));
    }

    @GetMapping("/api/clients/{id}")
    public ResponseEntity<?> getClient(@PathVariable String id) {
        Client c = store.clients.get(id);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(clientToMap(c));
    }

    @PostMapping("/api/clients/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name  = body.get("name");
        String email = body.get("email");
        if (name == null || email == null)
            return ResponseEntity.badRequest().body(Map.of("error", "name and email required"));

        // Use UUID-based ID to avoid collisions with seeded IDs (cl001, cl002, etc.)
        String id = "cl-" + UUID.randomUUID().toString().substring(0, 8);
        Client c  = new Client(id, name, email);
        store.clients.put(id, c);
        store.saveClient(c);                 // persist to DB
        return ResponseEntity.ok(clientToMap(c));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> serviceToMap(Service s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",       s.getServiceId());
        m.put("name",     s.getName());
        m.put("duration", s.getDurationMinutes());
        m.put("price",    s.getPrice());
        return m;
    }

    private Map<String, Object> clientToMap(Client c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",    c.getClientId());
        m.put("name",  c.getName());
        m.put("email", c.getEmail());
        return m;
    }
}
