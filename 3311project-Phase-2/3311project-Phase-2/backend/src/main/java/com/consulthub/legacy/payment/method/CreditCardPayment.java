package com.consulthub.legacy.payment.method;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CreditCardPayment extends PaymentMethod {

    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardHolderName;

    public CreditCardPayment(String paymentMethodId, String clientId, String label,
            String cardNumber, String expiryDate, String cvv,
            String cardHolderName) {
        super(paymentMethodId, clientId, MethodType.CREDIT_CARD, label);
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardHolderName = cardHolderName;
    }

    @Override
    public boolean validate() {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException("Invalid credit card number. Must be 16 digits.");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date is required.");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiry = YearMonth.parse(expiryDate, formatter);
            if (!expiry.isAfter(YearMonth.now())) {
                throw new IllegalArgumentException("Credit card has expired.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid expiry date format. Expected MM/yy.");
        }
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Invalid CVV. Must be 3 or 4 digits.");
        }
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cardholder name is required.");
        }
        return true;
    }

    @Override
    public String getMaskedDetails() {
        String masked = (cardNumber != null && cardNumber.length() >= 4)
                ? "**** **** **** " + cardNumber.substring(cardNumber.length() - 4)
                : "****";
        return "CreditCard[" + masked + ", exp=" + expiryDate + "]";
    }

    @Override
    public String processPayment(double amount) {
        System.out.println("Processing credit card payment of $" + String.format("%.2f", amount)
                + " on " + getMaskedDetails());
        simulateProcessingDelay();
        String transactionId = generateTransactionId("CC");
        System.out.println("Credit card payment successful. Transaction ID: " + transactionId);
        return transactionId;
    }

    @Override
    public boolean processRefund(String transactionId, double amount) {
        System.out.println("Processing credit card refund of $" + String.format("%.2f", amount)
                + " for " + transactionId);
        simulateProcessingDelay();
        System.out.println("Credit card refund successful.");
        return true;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }
}
