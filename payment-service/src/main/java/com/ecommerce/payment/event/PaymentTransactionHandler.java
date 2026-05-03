package com.ecommerce.payment.event;

import com.ecommerce.payment.service.PaymentService;

public final class PaymentTransactionHandler {
    private PaymentTransactionHandler() {
    }

    public static void handle(PaymentService paymentService, OrderStatusEvent event) {
        if ("FAILED".equalsIgnoreCase(event.getStatus())) {
            paymentService.markFailed(String.valueOf(event.getOrderId()), "ORDER_FAILED", event.getUsername());
        }
    }
}
