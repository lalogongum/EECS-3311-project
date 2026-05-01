package com.consulthub.legacy.payment.state;

import com.consulthub.legacy.payment.Payment;

/**
 * State interface for Payment lifecycle. Pending -> Successful / Failed,
 * Successful -> Refunded.
 */
public interface PaymentState {

    void handle(Payment payment);

    void paymentSuccess(Payment payment);

    void paymentFail(Payment payment);

    void refund(Payment payment);

    String getStateName();
}
