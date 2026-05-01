package com.consulthub.controller;

import com.consulthub.legacy.admin.CancellationPolicy;
import com.consulthub.legacy.admin.NotificationPolicy;
import com.consulthub.legacy.admin.PricingPolicy;
import com.consulthub.legacy.booking.model.Service;
import com.consulthub.service.DataStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private DataStore store;

    @Value("${ai.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
        String userMessage = body.get("message");
        if (userMessage == null || userMessage.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "message is required"));

        String systemContext = buildSystemContext();

        try {
            String reply;
            if (apiKey == null || apiKey.isBlank()) {
                reply = fallbackResponse(userMessage);
            } else {
                reply = callGroqAPI(userMessage, systemContext);
            }
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            System.out.println("❌ GROQ API ERROR:");
            e.printStackTrace();   // 🔥 THIS IS THE KEY
            return ResponseEntity.ok(Map.of("reply",
                    "I'm having trouble connecting right now. Please try again shortly. " +
                    "You can also browse the platform directly to find what you need."));
        }
    }

    private String callGroqAPI(String userMessage, String systemContext) throws Exception {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", systemContext),
            Map.of("role", "user",   "content", userMessage)
        ));
        requestBody.put("max_tokens", 512);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private String buildSystemContext() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful customer assistant for ConsultHub, a professional consulting booking platform. ");
        sb.append("You help clients understand how the platform works, available services, booking process, and policies. ");
        sb.append("You are friendly, concise, and professional. Do not access or reveal any personal user data.\n\n");
        sb.append("=== PLATFORM INFORMATION ===\n");
        sb.append("Available Services:\n");
        for (Service s : store.services.values()) {
            sb.append(String.format("- %s: %d minutes, $%.2f\n", s.getName(), s.getDurationMinutes(), s.getPrice()));
        }
        sb.append("\nCurrent Policies:\n");
        sb.append("- Cancellation: Must cancel at least ")
          .append(CancellationPolicy.getInstance().getMinimumHoursBeforeStart())
          .append(" hours before session start\n");
        sb.append(String.format("- Pricing range: $%.2f - $%.2f\n",
                PricingPolicy.getInstance().getMinPrice(),
                PricingPolicy.getInstance().getMaxPrice()));
        sb.append("- Notifications are ")
          .append(NotificationPolicy.getInstance().getNotificationPolicy() ? "enabled" : "disabled")
          .append("\n");
        sb.append("\nPayment Methods Accepted: Credit Card, Debit Card, PayPal, Bank Transfer\n");
        sb.append("Booking States: Requested -> Confirmed -> Pending Payment -> Paid -> Completed\n");
        sb.append("\nApproved Consultants available: ")
          .append(store.consultants.values().stream().filter(c -> "APPROVED".equals(c.getStatus())).count())
          .append("\n");
        sb.append("\nIMPORTANT: Never reveal personal user information, payment details, or booking specifics. ");
        sb.append("Only answer questions about how the platform works in general.");
        return sb.toString();
    }

    private String fallbackResponse(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("book") || lower.contains("session"))
            return "To book a session: go to 'Book a Session', choose an approved consultant, pick a service and available time slot, then submit your request.";
        if (lower.contains("pay") || lower.contains("payment"))
            return "We accept Credit Card, Debit Card, PayPal, and Bank Transfer. Once confirmed, go to 'My Bookings' and click Pay.";
        if (lower.contains("cancel"))
            return "You can cancel from 'My Bookings' at least " +
                   CancellationPolicy.getInstance().getMinimumHoursBeforeStart() +
                   " hours before your session.";
        if (lower.contains("service") || lower.contains("offer"))
            return "We offer: Strategy Session (60 min, $250), Technical Review (90 min, $400), Quick Consultation (30 min, $120), and Deep Dive Workshop (180 min, $800).";
        if (lower.contains("refund"))
            return "Refunds can be requested from 'Payments' for successfully paid bookings.";
        if (lower.contains("consultant"))
            return "Browse approved consultants in the 'Browse Consultants' section and book directly from there.";
        return "Hi! I'm the ConsultHub assistant. I can help with bookings, payments, cancellations, and more. What would you like to know?";
    }
}
