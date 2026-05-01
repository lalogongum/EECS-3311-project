package com.consulthub.legacy.payment.state;

import com.consulthub.legacy.payment.Payment;

/**
 * Terminal state. Payment refunded, no transitions allowed.
 */
public class PaymentRefundedState implements PaymentState {

    @Override
    public void handle(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Refunded.");
    }

    @Override
    public void paymentSuccess(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Cannot succeed a refunded payment.");
    }

    @Override
    public void paymentFail(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Cannot fail a refunded payment.");
    }

    @Override
    public void refund(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Already refunded.");
    }

    @Override
    public String getStateName() {
        return "REFUNDED";
    }

    @Override
    public String toString() {
        return getStateName();
    }
}
