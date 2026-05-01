package com.consulthub.legacy.payment.method;

public class PayPalPayment extends PaymentMethod {

    private String paypalEmail;

    public PayPalPayment(String paymentMethodId, String clientId, String label,
            String paypalEmail) {
        super(paymentMethodId, clientId, MethodType.PAYPAL, label);
        this.paypalEmail = paypalEmail;
    }

    @Override
    public boolean validate() {
        String emailPattern = "^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (paypalEmail == null || !paypalEmail.matches(emailPattern)) {
            throw new IllegalArgumentException("Invalid PayPal email address format.");
        }
        return true;
    }

    @Override
    public String getMaskedDetails() {
        if (paypalEmail == null || !paypalEmail.contains("@")) {
            return "PayPal[****]";
        }
        String[] parts = paypalEmail.split("@");
        String localPart = parts[0];
        String masked = localPart.length() > 2
                ? localPart.substring(0, 2) + "****"
                : localPart + "****";
        return "PayPal[" + masked + "@" + parts[1] + "]";
    }

    @Override
    public String processPayment(double amount) {
        System.out.println("Processing PayPal payment of $" + String.format("%.2f", amount)
                + " via " + getMaskedDetails());
        simulateProcessingDelay();
        String transactionId = generateTransactionId("PP");
        System.out.println("PayPal payment successful. Transaction ID: " + transactionId);
        return transactionId;
    }

    @Override
    public boolean processRefund(String transactionId, double amount) {
        System.out.println("Processing PayPal refund of $" + String.format("%.2f", amount)
                + " for " + transactionId);
        simulateProcessingDelay();
        System.out.println("PayPal refund successful.");
        return true;
    }

    public String getPaypalEmail() {
        return paypalEmail;
    }
}
