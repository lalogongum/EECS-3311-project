package com.consulthub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_methods")
public class PaymentMethodEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String paymentMethodId;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String methodType;   // CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_ACCOUNT

    @Column(nullable = false)
    private String label;

    // Card fields (CREDIT_CARD / DEBIT_CARD)
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardHolderName;

    // PayPal
    private String paypalEmail;

    // Bank account
    private String accountNumber;
    private String routingNumber;
    private String bankName;

    public PaymentMethodEntity() {}

    public PaymentMethodEntity(String paymentMethodId, String clientId, String methodType,
                               String label,
                               String cardNumber, String expiryDate, String cvv, String cardHolderName,
                               String paypalEmail,
                               String accountNumber, String routingNumber, String bankName) {
        this.paymentMethodId = paymentMethodId;
        this.clientId        = clientId;
        this.methodType      = methodType;
        this.label           = label;
        this.cardNumber      = cardNumber;
        this.expiryDate      = expiryDate;
        this.cvv             = cvv;
        this.cardHolderName  = cardHolderName;
        this.paypalEmail     = paypalEmail;
        this.accountNumber   = accountNumber;
        this.routingNumber   = routingNumber;
        this.bankName        = bankName;
    }

    public String getPaymentMethodId() { return paymentMethodId; }
    public String getClientId()        { return clientId; }
    public String getMethodType()      { return methodType; }
    public String getLabel()           { return label; }
    public String getCardNumber()      { return cardNumber; }
    public String getExpiryDate()      { return expiryDate; }
    public String getCvv()             { return cvv; }
    public String getCardHolderName()  { return cardHolderName; }
    public String getPaypalEmail()     { return paypalEmail; }
    public String getAccountNumber()   { return accountNumber; }
    public String getRoutingNumber()   { return routingNumber; }
    public String getBankName()        { return bankName; }
}
