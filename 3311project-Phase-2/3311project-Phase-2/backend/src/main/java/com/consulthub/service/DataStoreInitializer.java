package com.consulthub.service;

import com.consulthub.entity.*;
import com.consulthub.legacy.booking.Booking;
import com.consulthub.legacy.booking.model.Client;
import com.consulthub.legacy.booking.model.Service;
import com.consulthub.legacy.consultant.AvailabilitySlot;
import com.consulthub.legacy.consultant.Consultant;
import com.consulthub.legacy.admin.*;
import com.consulthub.legacy.payment.method.BankAccountPayment;
import com.consulthub.legacy.payment.method.CreditCardPayment;
import com.consulthub.legacy.payment.method.DebitCardPayment;
import com.consulthub.legacy.payment.method.PayPalPayment;
import com.consulthub.legacy.payment.method.PaymentMethod;
import com.consulthub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Initializes the database with seed data and loads it into the in-memory
 * DataStore maps. Uses ApplicationRunner (not @PostConstruct) so that
 * @Transactional works correctly via Spring's proxy mechanism.
 */
@Component
public class DataStoreInitializer implements ApplicationRunner {

    @Autowired private DataStore store;
    @Autowired private ClientRepository         clientRepo;
    @Autowired private ServiceRepository        serviceRepo;
    @Autowired private ConsultantRepository     consultantRepo;
    @Autowired private SlotRepository           slotRepo;
    @Autowired private BookingRepository        bookingRepo;
    @Autowired private PaymentMethodRepository  paymentMethodRepo;
    @Autowired private SystemPolicyRepository   policyRepo;

    @Override
    public void run(ApplicationArguments args) {
        if (clientRepo.count() == 0) {
            seedDatabase();
        }
        loadFromDatabase();
        loadPaymentMethodsFromDatabase();
        loadPoliciesFromDatabase();
        seedPaymentMethod();
    }

    @Transactional
    public void seedDatabase() {
        clientRepo.save(new ClientEntity("cl001", "Alex Chen",     "alex@company.com"));
        clientRepo.save(new ClientEntity("cl002", "Jordan Rivera", "jordan@startup.io"));

        serviceRepo.save(new ServiceEntity("svc001", "Strategy Session",   60,  250.0));
        serviceRepo.save(new ServiceEntity("svc002", "Technical Review",   90,  400.0));
        serviceRepo.save(new ServiceEntity("svc003", "Quick Consultation", 30,  120.0));
        serviceRepo.save(new ServiceEntity("svc004", "Deep Dive Workshop", 180, 800.0));

        ConsultantEntity con1 = new ConsultantEntity("c001", "Dr. Sarah Mitchell", "sarah@consultpro.com", "APPROVED");
        consultantRepo.save(con1);
        LocalDateTime b3 = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);
        LocalDateTime b4 = LocalDateTime.now().plusDays(4).withSecond(0).withNano(0);
        slotRepo.save(new AvailabilitySlotEntity("s001", con1, b3.withHour(9),  b3.withHour(10)));
        slotRepo.save(new AvailabilitySlotEntity("s002", con1, b3.withHour(14), b3.withHour(15)));
        slotRepo.save(new AvailabilitySlotEntity("s003", con1, b4.withHour(10), b4.withHour(11)));

        ConsultantEntity con2 = new ConsultantEntity("c002", "James Okonkwo", "james@consultpro.com", "APPROVED");
        consultantRepo.save(con2);
        slotRepo.save(new AvailabilitySlotEntity("s004", con2, b3.withHour(11), b3.withHour(12)));
        slotRepo.save(new AvailabilitySlotEntity("s005", con2, b4.withHour(15), b4.withHour(16)));

        consultantRepo.save(new ConsultantEntity("c003", "Priya Nair", "priya@example.com", "PENDING"));
    }

    public void loadFromDatabase() {
        for (ClientEntity e : clientRepo.findAll()) {
            store.clients.put(e.getClientId(),
                new Client(e.getClientId(), e.getName(), e.getEmail()));
        }

        for (ServiceEntity e : serviceRepo.findAll()) {
            store.services.put(e.getServiceId(),
                new Service(e.getServiceId(), e.getName(), e.getDurationMinutes(), e.getPrice()));
        }

        Map<String, List<AvailabilitySlotEntity>> slotsByConsultant = new HashMap<>();
        for (AvailabilitySlotEntity s : slotRepo.findAll()) {
            slotsByConsultant
                .computeIfAbsent(s.getConsultant().getConsultantId(), k -> new ArrayList<>())
                .add(s);
        }
        for (ConsultantEntity e : consultantRepo.findAll()) {
            Consultant c = new Consultant(e.getConsultantId(), e.getName(), e.getEmail());
            c.setStatus(e.getStatus());
            for (AvailabilitySlotEntity s : slotsByConsultant.getOrDefault(e.getConsultantId(), List.of())) {
                AvailabilitySlot slot = new AvailabilitySlot(
                    s.getSlotId(), s.getStartDateTime(), s.getEndDateTime());
                if (!s.isAvailable()) slot.markUnavailable();
                c.addAvailabilitySlot(slot);
            }
            store.consultants.put(e.getConsultantId(), c);
        }

        for (BookingEntity e : bookingRepo.findAll()) {
            Client     client     = store.clients.get(e.getClientId());
            Consultant consultant = store.consultants.get(e.getConsultantId());
            Service    service    = store.services.get(e.getServiceId());
            if (client == null || consultant == null || service == null) continue;

            AvailabilitySlot slot = consultant.getAvailabilitySlots().stream()
                .filter(s -> s.getSlotId().equals(e.getSlotId()))
                .findFirst()
                .orElseGet(() -> {
                    AvailabilitySlot ph = new AvailabilitySlot(
                        e.getSlotId() != null ? e.getSlotId() : "unknown",
                        LocalDateTime.now(), LocalDateTime.now());
                    ph.markUnavailable();
                    return ph;
                });

            Booking booking = new Booking(
                e.getBookingId(), e.getTotalAmount(), client, consultant, service, slot);
            booking.setState(store.stateFromString(e.getState()));
            store.bookingService.restoreBooking(booking);
        }
    }

    public void loadPaymentMethodsFromDatabase() {
        for (PaymentMethodEntity e : paymentMethodRepo.findAll()) {
            try {
                PaymentMethod pm = switch (e.getMethodType()) {
                    case "CREDIT_CARD"  -> new CreditCardPayment(
                        e.getPaymentMethodId(), e.getClientId(), e.getLabel(),
                        e.getCardNumber(), e.getExpiryDate(), e.getCvv(), e.getCardHolderName());
                    case "DEBIT_CARD"   -> new DebitCardPayment(
                        e.getPaymentMethodId(), e.getClientId(), e.getLabel(),
                        e.getCardNumber(), e.getExpiryDate(), e.getCvv(), e.getCardHolderName());
                    case "PAYPAL"       -> new PayPalPayment(
                        e.getPaymentMethodId(), e.getClientId(), e.getLabel(), e.getPaypalEmail());
                    case "BANK_ACCOUNT" -> new BankAccountPayment(
                        e.getPaymentMethodId(), e.getClientId(), e.getLabel(),
                        e.getAccountNumber(), e.getRoutingNumber(), e.getBankName());
                    default -> null;
                };
                if (pm != null) PaymentMethod.addPaymentMethod(pm);
            } catch (Exception ignored) {}
        }
    }

    private void seedPaymentMethod() {
        try {
            if (paymentMethodRepo.count() == 0 && PaymentMethod.getPaymentMethods("cl001").isEmpty()) {
                CreditCardPayment seedCard = new CreditCardPayment(
                    "PM-seed01", "cl001", "My Visa",
                    "4111111111111111", "12/27", "123", "Alex Chen");
                PaymentMethod.addPaymentMethod(seedCard);
                store.savePaymentMethod(seedCard);
            }
        } catch (Exception ignored) {}
    }

    public void loadPoliciesFromDatabase() {
        policyRepo.findAll().forEach(e -> {
            try {
                switch (e.getPolicyKey()) {
                    case "cancellationHours" ->
                        CancellationPolicy.getInstance().applyTimeWindowChange(Integer.parseInt(e.getPolicyValue()));
                    case "priceMin" -> {
                        double min = Double.parseDouble(e.getPolicyValue());
                        PricingPolicy.getInstance().applyChange(min, PricingPolicy.getInstance().getMaxPrice());
                    }
                    case "priceMax" -> {
                        double max = Double.parseDouble(e.getPolicyValue());
                        PricingPolicy.getInstance().applyChange(PricingPolicy.getInstance().getMinPrice(), max);
                    }
                    case "notificationsEnabled" ->
                        NotificationPolicy.getInstance().applyChange(Boolean.parseBoolean(e.getPolicyValue()));
                    case "refundPolicy" ->
                        RefundPolicy.getInstance().applyChange(e.getPolicyValue());
                }
            } catch (Exception ignored) {}
        });
    }

}
