package com.consulthub.legacy.payment.state;

import com.consulthub.legacy.payment.Payment;

/**
 * Initial state. Can transition to Successful or Failed.
 */
public class PaymentPendingState implements PaymentState {

    @Override
    public void handle(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Pending.");
    }

    @Override
    public void paymentSuccess(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] PENDING -> SUCCESSFUL.");
        payment.setState(new PaymentSuccessfulState());
    }

    @Override
    public void paymentFail(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] PENDING -> FAILED.");
        payment.setState(new PaymentFailedState());
    }

    @Override
    public void refund(Payment payment) {
        System.out.println("[Payment " + payment.getTransactionId() + "] Cannot refund a pending payment.");
    }

    @Override
    public String getStateName() {
        return "PENDING";
    }

    @Override
    public String toString() {
        return getStateName();
    }
}
