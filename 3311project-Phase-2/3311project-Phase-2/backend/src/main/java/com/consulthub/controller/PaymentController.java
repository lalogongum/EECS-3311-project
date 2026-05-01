package com.consulthub.controller;

import com.consulthub.entity.PaymentEntity;
import com.consulthub.legacy.booking.Booking;
import com.consulthub.legacy.payment.Payment;
import com.consulthub.legacy.payment.method.*;
import com.consulthub.legacy.payment.state.PaymentSuccessfulState;
import com.consulthub.legacy.payment.state.PaymentRefundedState;
import com.consulthub.service.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private DataStore store;

    /**
     * GET /api/payments/client/{clientId}
     * Returns merged history from DB (authoritative, survives restarts) plus any
     * in-memory-only payments made this session that haven't been flushed yet.
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Map<String, Object>>> getHistory(@PathVariable String clientId) {
        // DB is the authoritative source — always query it
        List<Map<String, Object>> dbHistory = store.getPaymentHistoryFromDb(clientId);

        // Also include any in-memory payments not yet in DB (edge case safety net)
        Set<String> dbIds = dbHistory.stream()
                .map(m -> (String) m.get("transactionId"))
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>(dbHistory);
        for (Payment p : Payment.getPaymentHistory(clientId)) {
            if (!dbIds.contains(p.getTransactionId())) {
                result.add(paymentToMap(p));
            }
        }
        return ResponseEntity.ok(result);
    }

    // POST /api/payments/process
    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody Map<String, String> body) {
        String bookingId       = body.get("bookingId");
        String paymentMethodId = body.get("paymentMethodId");
        String clientId        = body.get("clientId");

        try {
            Booking booking = store.bookingService.getBooking(bookingId);
            PaymentMethod method = PaymentMethod.getPaymentMethod(clientId, paymentMethodId);
            Payment payment = Payment.processPayment(booking, method);
            store.saveBooking(booking);      // booking state -> PAID, persist to DB
            store.savePayment(payment);      // persist payment record to DB
            return ResponseEntity.ok(paymentToMap(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/payments/refund
     * Looks up the payment in memory first, then falls back to the DB so that
     * refunds work correctly after a server restart.
     */
    @PostMapping("/refund")
    public ResponseEntity<?> refund(@RequestBody Map<String, String> body) {
        String txnId           = body.get("transactionId");
        String paymentMethodId = body.get("paymentMethodId");
        String clientId        = body.get("clientId");

        try {
            PaymentMethod method = PaymentMethod.getPaymentMethod(clientId, paymentMethodId);

            // Restore payment into in-memory list from DB if not already there
            ensurePaymentInMemory(txnId, clientId, method);

            Payment payment = Payment.processRefund(txnId, method);
            store.savePayment(payment);      // persist refund state to DB
            return ResponseEntity.ok(paymentToMap(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/payments/methods/{clientId}
    @GetMapping("/methods/{clientId}")
    public ResponseEntity<List<Map<String, Object>>> getMethods(@PathVariable String clientId) {
        List<Map<String, Object>> result = PaymentMethod.getPaymentMethods(clientId)
                .stream().map(this::methodToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // POST /api/payments/methods
    @PostMapping("/methods")
    public ResponseEntity<?> addMethod(@RequestBody Map<String, String> body) {
        String clientId = body.get("clientId");
        String type     = body.get("type");
        String label    = body.get("label");
        String pmId     = PaymentMethod.generatePaymentMethodId();

        try {
            PaymentMethod method = buildMethod(pmId, clientId, type, label, body);
            PaymentMethod.addPaymentMethod(method);
            store.savePaymentMethod(method);   // persist to DB
            return ResponseEntity.ok(methodToMap(method));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/payments/methods/{clientId}/{pmId}
    @DeleteMapping("/methods/{clientId}/{pmId}")
    public ResponseEntity<?> removeMethod(@PathVariable String clientId, @PathVariable String pmId) {
        try {
            PaymentMethod.removePaymentMethod(clientId, pmId);
            store.deletePaymentMethod(pmId);   // remove from DB
            return ResponseEntity.ok(Map.of("message", "Removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * If the payment with the given transactionId is not in the in-memory list,
     * load it from the DB and restore it so processRefund() can find it.
     */
    private void ensurePaymentInMemory(String txnId, String clientId, PaymentMethod method) {
        try {
            Payment.getPaymentByTransactionId(txnId); // already in memory
        } catch (IllegalArgumentException notFound) {
            // Restore from DB
            PaymentEntity entity = store.paymentRepo.findById(txnId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + txnId));

            Payment restored = new Payment(
                    entity.getTransactionId(),
                    entity.getBookingId(),
                    entity.getClientId(),
                    entity.getAmount(),
                    method);
            restored.setDescription(entity.getDescription() != null ? entity.getDescription() : "");

            // Restore state
            switch (entity.getState()) {
                case "SUCCESSFUL" -> restored.setState(new PaymentSuccessfulState());
                case "REFUNDED"   -> restored.setState(new PaymentRefundedState());
                // PENDING and FAILED left as-is (constructed as PENDING)
            }

            Payment.restorePayment(restored);
        }
    }

    private PaymentMethod buildMethod(String pmId, String clientId, String type, String label, Map<String, String> b) {
        return switch (type.toUpperCase()) {
            case "CREDIT_CARD"  -> new CreditCardPayment(pmId, clientId, label,
                    b.get("cardNumber"), b.get("expiryDate"), b.get("cvv"), b.get("cardHolderName"));
            case "DEBIT_CARD"   -> new DebitCardPayment(pmId, clientId, label,
                    b.get("cardNumber"), b.get("expiryDate"), b.get("cvv"), b.get("cardHolderName"));
            case "PAYPAL"       -> new PayPalPayment(pmId, clientId, label, b.get("paypalEmail"));
            case "BANK_ACCOUNT" -> new BankAccountPayment(pmId, clientId, label,
                    b.get("accountNumber"), b.get("routingNumber"), b.get("bankName"));
            default -> throw new IllegalArgumentException("Unknown payment method type: " + type);
        };
    }

    private Map<String, Object> paymentToMap(Payment p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("transactionId", p.getTransactionId());
        m.put("bookingId",     p.getBookingId());
        m.put("clientId",      p.getClientId());
        m.put("amount",        p.getAmount());
        m.put("state",         p.getStateName());
        m.put("method",        p.getPaymentMethod().getType().toString());
        m.put("methodMasked",  p.getPaymentMethod().getMaskedDetails());
        m.put("description",   p.getDescription());
        m.put("createdAt",     p.getCreatedAt().toString());
        m.put("updatedAt",     p.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> methodToMap(PaymentMethod pm) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",       pm.getPaymentMethodId());
        m.put("type",     pm.getType().toString());
        m.put("label",    pm.getLabel());
        m.put("masked",   pm.getMaskedDetails());
        m.put("clientId", pm.getClientId());
        return m;
    }
}
