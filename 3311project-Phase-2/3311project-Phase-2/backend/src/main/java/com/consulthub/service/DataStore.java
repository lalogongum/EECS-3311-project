package com.consulthub.service;

import com.consulthub.entity.*;
import com.consulthub.legacy.booking.Booking;
import com.consulthub.legacy.booking.BookingService;
import com.consulthub.legacy.booking.model.Client;
import com.consulthub.legacy.booking.model.Service;
import com.consulthub.legacy.booking.state.*;
import com.consulthub.legacy.consultant.AvailabilitySlot;
import com.consulthub.legacy.consultant.Consultant;
import com.consulthub.legacy.admin.*;
import com.consulthub.legacy.payment.method.*;
import com.consulthub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central store bridging Phase 1 legacy domain objects (kept for design patterns)
 * with a real PostgreSQL/H2 database via JPA repositories.
 *
 * The in-memory maps are static so they are initialized at class-load time,
 * before Spring creates any CGLIB proxy — guaranteeing they are never null.
 */
@Component
public class DataStore {

    // ── JPA Repositories (injected by Spring) ─────────────────────────────────
    @Autowired public ConsultantRepository      consultantRepo;
    @Autowired public SlotRepository            slotRepo;
    @Autowired public ClientRepository          clientRepo;
    @Autowired public ServiceRepository         serviceRepo;
    @Autowired public BookingRepository         bookingRepo;
    @Autowired public PaymentRepository         paymentRepo;
    @Autowired public PaymentMethodRepository   paymentMethodRepo;
    @Autowired public SystemPolicyRepository    policyRepo;

    // ── Legacy in-memory maps — STATIC so they survive CGLIB proxying ──────────
    public static final BookingService bookingService = new BookingService();
    public static final Map<String, Client>     clients     = new ConcurrentHashMap<>();
    public static final Map<String, Consultant> consultants = new ConcurrentHashMap<>();
    public static final Map<String, Service>    services    = new ConcurrentHashMap<>();

    // ── Persist helpers ───────────────────────────────────────────────────────

    @Transactional
    public void saveConsultant(Consultant c) {
        ConsultantEntity entity = consultantRepo.findById(c.getConsultantId())
            .orElse(new ConsultantEntity(c.getConsultantId(), c.getName(), c.getEmail(), c.getStatus()));
        entity.setStatus(c.getStatus());
        consultantRepo.save(entity);
    }

    @Transactional
    public void saveNewConsultant(Consultant c) {
        consultantRepo.save(new ConsultantEntity(c.getConsultantId(), c.getName(), c.getEmail(), c.getStatus()));
    }

    @Transactional
    public void saveSlot(String consultantId, AvailabilitySlot slot) {
        ConsultantEntity con = consultantRepo.findById(consultantId)
            .orElseThrow(() -> new IllegalArgumentException("Consultant not found: " + consultantId));
        AvailabilitySlotEntity entity = slotRepo.findById(slot.getSlotId())
            .orElse(new AvailabilitySlotEntity(slot.getSlotId(), con,
                slot.getStartDateTime(), slot.getEndDateTime()));
        entity.setAvailable(slot.isAvailable());
        slotRepo.save(entity);
    }

    @Transactional
    public void deleteSlot(String slotId) {
        slotRepo.deleteById(slotId);
    }

    @Transactional
    public void saveBooking(Booking b) {
        String slotId = b.getSlot() != null ? b.getSlot().getSlotId() : null;
        BookingEntity entity = bookingRepo.findById(b.getBookingId())
            .orElse(new BookingEntity(b.getBookingId(),
                b.getClient().getClientId(),
                b.getConsultant().getConsultantId(),
                b.getService().getServiceId(),
                slotId, b.getTotalAmount(),
                b.getState().toString(),
                b.getCreatedAt()));
        entity.setState(b.getState().toString());
        bookingRepo.save(entity);
    }

    @Transactional
    public void saveClient(Client c) {
        clientRepo.save(new ClientEntity(c.getClientId(), c.getName(), c.getEmail()));
    }

    @Transactional
    public void savePayment(com.consulthub.legacy.payment.Payment p) {
        PaymentEntity entity = paymentRepo.findById(p.getTransactionId())
            .orElse(new PaymentEntity(
                p.getTransactionId(), p.getBookingId(), p.getClientId(),
                p.getAmount(), p.getStateName(),
                p.getPaymentMethod().getType().toString(),
                p.getPaymentMethod().getMaskedDetails(),
                p.getDescription(), p.getCreatedAt(), p.getUpdatedAt()));
        entity.setState(p.getStateName());
        entity.setDescription(p.getDescription());
        entity.setUpdatedAt(p.getUpdatedAt());
        paymentRepo.save(entity);
    }

    // ── Payment method persistence ────────────────────────────────────────────

    @Transactional
    public void savePaymentMethod(PaymentMethod pm) {
        String cardNumber     = null, expiryDate   = null, cvv           = null, cardHolderName = null;
        String paypalEmail    = null;
        String accountNumber  = null, routingNumber = null, bankName      = null;

        if (pm instanceof CreditCardPayment c) {
            cardNumber = c.getCardNumber(); expiryDate = c.getExpiryDate();
            cvv = c.getCvv(); cardHolderName = c.getCardHolderName();
        } else if (pm instanceof DebitCardPayment d) {
            cardNumber = d.getCardNumber(); expiryDate = d.getExpiryDate();
            cvv = d.getCvv(); cardHolderName = d.getCardHolderName();
        } else if (pm instanceof PayPalPayment pp) {
            paypalEmail = pp.getPaypalEmail();
        } else if (pm instanceof BankAccountPayment b) {
            accountNumber = b.getAccountNumber(); routingNumber = b.getRoutingNumber(); bankName = b.getBankName();
        }

        PaymentMethodEntity entity = new PaymentMethodEntity(
            pm.getPaymentMethodId(), pm.getClientId(), pm.getType().toString(), pm.getLabel(),
            cardNumber, expiryDate, cvv, cardHolderName,
            paypalEmail,
            accountNumber, routingNumber, bankName);
        paymentMethodRepo.save(entity);
    }

    @Transactional
    public void deletePaymentMethod(String paymentMethodId) {
        paymentMethodRepo.deleteById(paymentMethodId);
    }


    // ── System policy persistence ─────────────────────────────────────────────

    @Transactional
    public void savePolicy(String key, String value) {
        SystemPolicyEntity entity = policyRepo.findById(key)
            .orElse(new SystemPolicyEntity(key, value));
        entity.setPolicyValue(value);
        policyRepo.save(entity);
    }

    @Transactional
    public void saveAllPolicies() {
        savePolicy("cancellationHours", String.valueOf(CancellationPolicy.getInstance().getMinimumHoursBeforeStart()));
        savePolicy("priceMin",          String.valueOf(PricingPolicy.getInstance().getMinPrice()));
        savePolicy("priceMax",          String.valueOf(PricingPolicy.getInstance().getMaxPrice()));
        savePolicy("notificationsEnabled", String.valueOf(NotificationPolicy.getInstance().getNotificationPolicy()));
        savePolicy("refundPolicy",      RefundPolicy.getInstance().getRefundPolicy());
    }

    // ── Payment history from DB ───────────────────────────────────────────────

    public List<Map<String, Object>> getPaymentHistoryFromDb(String clientId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PaymentEntity e : paymentRepo.findByClientId(clientId)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("transactionId", e.getTransactionId());
            m.put("bookingId",     e.getBookingId());
            m.put("clientId",      e.getClientId());
            m.put("amount",        e.getAmount());
            m.put("state",         e.getState());
            m.put("method",        e.getMethodType());
            m.put("methodMasked",  e.getMethodMasked());
            m.put("description",   e.getDescription());
            m.put("createdAt",     e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
            m.put("updatedAt",     e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null);
            result.add(m);
        }
        return result;
    }

    // ── State helper ──────────────────────────────────────────────────────────

    public BookingState stateFromString(String s) {
        return switch (s) {
            case "CONFIRMED"       -> new ConfirmedState();
            case "PENDING_PAYMENT" -> new PendingPaymentState();
            case "PAID"            -> new PaidState();
            case "REJECTED"        -> new RejectedState();
            case "CANCELLED"       -> new CancelledState();
            case "COMPLETED"       -> new CompletedState();
            default                -> new RequestedState();
        };
    }
}
