package com.consulthub.legacy.payment.method;

public class BankAccountPayment extends PaymentMethod {

    private String accountNumber;
    private String routingNumber;
    private String bankName;

    public BankAccountPayment(String paymentMethodId, String clientId, String label,
            String accountNumber, String routingNumber, String bankName) {
        super(paymentMethodId, clientId, MethodType.BANK_ACCOUNT, label);
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
        this.bankName = bankName;
    }

    @Override
    public boolean validate() {
        if (accountNumber == null || !accountNumber.matches("\\d{8,17}")) {
            throw new IllegalArgumentException("Invalid account number. Must be 8-17 digits.");
        }
        if (routingNumber == null || !routingNumber.matches("\\d{9}")) {
            throw new IllegalArgumentException("Invalid routing number. Must be 9 digits.");
        }
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank name is required.");
        }
        return true;
    }

    @Override
    public String getMaskedDetails() {
        String maskedAcct = (accountNumber != null && accountNumber.length() >= 4)
                ? "****" + accountNumber.substring(accountNumber.length() - 4)
                : "****";
        return "BankAccount[acct=" + maskedAcct + ", bank=" + bankName + "]";
    }

    @Override
    public String processPayment(double amount) {
        System.out.println("Processing bank account payment of $" + String.format("%.2f", amount)
                + " via " + getMaskedDetails());
        simulateProcessingDelay();
        String transactionId = generateTransactionId("BA");
        System.out.println("Bank account payment successful. Transaction ID: " + transactionId);
        return transactionId;
    }

    @Override
    public boolean processRefund(String transactionId, double amount) {
        System.out.println("Processing bank account refund of $" + String.format("%.2f", amount)
                + " for " + transactionId);
        simulateProcessingDelay();
        System.out.println("Bank account refund successful.");
        return true;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public String getBankName() {
        return bankName;
    }
}
