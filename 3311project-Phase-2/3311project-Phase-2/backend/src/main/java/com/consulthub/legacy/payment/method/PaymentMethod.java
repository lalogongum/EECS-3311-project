package com.consulthub.legacy.payment.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class PaymentMethod {

    public enum MethodType {
        CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_ACCOUNT
    }

    private static final Map<String, List<PaymentMethod>> clientPaymentMethods = new HashMap<>();

    public static void addPaymentMethod(PaymentMethod paymentMethod) {
        paymentMethod.validate();
        String clientId = paymentMethod.getClientId();
        List<PaymentMethod> methods = clientPaymentMethods.get(clientId);
        if (methods == null) {
            methods = new ArrayList<>();
            clientPaymentMethods.put(clientId, methods);
        }
        methods.add(paymentMethod);
        System.out.println("[PaymentMethod] Added: " + paymentMethod.getMaskedDetails()
                + " for client " + clientId);
    }

    public static List<PaymentMethod> getPaymentMethods(String clientId) {
        List<PaymentMethod> methods = clientPaymentMethods.get(clientId);
        if (methods == null) {
            return new ArrayList<>();
        }
        return methods;
    }

    public static PaymentMethod getPaymentMethod(String clientId, String paymentMethodId) {
        List<PaymentMethod> methods = getPaymentMethods(clientId);
        for (PaymentMethod method : methods) {
            if (method.getPaymentMethodId().equals(paymentMethodId)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Payment method not found: " + paymentMethodId);
    }

    public static void updatePaymentMethod(PaymentMethod updatedMethod) {
        updatedMethod.validate();
        String clientId = updatedMethod.getClientId();
        List<PaymentMethod> methods = clientPaymentMethods.get(clientId);
        if (methods == null) {
            throw new IllegalArgumentException("No payment methods found for client: " + clientId);
        }
        for (int i = 0; i < methods.size(); i++) {
            if (methods.get(i).getPaymentMethodId().equals(updatedMethod.getPaymentMethodId())) {
                methods.set(i, updatedMethod);
                System.out.println("[PaymentMethod] Updated: " + updatedMethod.getMaskedDetails());
                return;
            }
        }
        throw new IllegalArgumentException("Payment method not found: " + updatedMethod.getPaymentMethodId());
    }

    public static void removePaymentMethod(String clientId, String paymentMethodId) {
        List<PaymentMethod> methods = clientPaymentMethods.get(clientId);
        if (methods == null) {
            throw new IllegalArgumentException("No payment methods found for client: " + clientId);
        }
        for (int i = 0; i < methods.size(); i++) {
            if (methods.get(i).getPaymentMethodId().equals(paymentMethodId)) {
                methods.remove(i);
                System.out.println("[PaymentMethod] Removed " + paymentMethodId + " for client " + clientId);
                return;
            }
        }
        throw new IllegalArgumentException("Payment method not found: " + paymentMethodId);
    }

    public static String generatePaymentMethodId() {
        return "PM-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static void clearAllPaymentMethods() {
        clientPaymentMethods.clear();
    }

    private String paymentMethodId;
    private String clientId;
    private MethodType type;
    private String label;

    public PaymentMethod(String paymentMethodId, String clientId,
            MethodType type, String label) {
        this.paymentMethodId = paymentMethodId;
        this.clientId = clientId;
        this.type = type;
        this.label = label;
    }

    public abstract boolean validate();

    public abstract String getMaskedDetails();

    public abstract String processPayment(double amount);

    public abstract boolean processRefund(String transactionId, double amount);

    protected void simulateProcessingDelay() {
        try {
            long delay = 2000 + (long) (Math.random() * 1000);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected String generateTransactionId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getClientId() {
        return clientId;
    }

    public MethodType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "PaymentMethod{id='" + paymentMethodId + "', type=" + type
                + ", label='" + label + "', details=" + getMaskedDetails() + "}";
    }
}
