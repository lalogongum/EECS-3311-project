package com.consulthub.legacy.payment.state;

import com.consulthub.legacy.payment.Payment;

/**
 * Terminal state. Payment failed, no transitions allowed.
 */
public class PaymentFailedState implements PaymentState {

    @Override
    public void handle(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Failed.");
    }

    @Override
    public void paymentSuccess(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Cannot succeed a failed payment.");
    }

    @Override
    public void paymentFail(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Already failed.");
    }

    @Override
    public void refund(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Cannot refund a failed payment.");
    }

    @Override
    public String getStateName() {
        return "FAILED";
    }

    @Override
    public String toString() {
        return getStateName();
    }
}
