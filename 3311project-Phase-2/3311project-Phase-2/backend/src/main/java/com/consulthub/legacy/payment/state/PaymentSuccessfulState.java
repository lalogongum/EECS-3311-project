package com.consulthub.legacy.payment.state;

import com.consulthub.legacy.payment.Payment;

/**
 * Payment succeeded. Can transition to Refunded.
 */
public class PaymentSuccessfulState implements PaymentState {

    @Override
    public void handle(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Successful.");
    }

    @Override
    public void paymentSuccess(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Already successful.");
    }

    @Override
    public void paymentFail(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Cannot fail a successful payment.");
    }

    @Override
    public void refund(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] SUCCESSFUL -> REFUNDED.");
        payment.setState(new PaymentRefundedState());
    }

    @Override
    public String getStateName() {
        return "SUCCESSFUL";
    }

    @Override
    public String toString() {
        return getStateName();
    }
}
