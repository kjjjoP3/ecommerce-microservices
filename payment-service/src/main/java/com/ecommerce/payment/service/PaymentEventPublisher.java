package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.PaymentTransaction;
import com.ecommerce.payment.event.OrderStatusEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublisher {
    private final KafkaTemplate<String, OrderStatusEvent> kafkaTemplate;
    private final String paymentStatusTopic;

    public PaymentEventPublisher(
            KafkaTemplate<String, OrderStatusEvent> kafkaTemplate,
            @Value("${app.kafka.topics.payment-status}") String paymentStatusTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentStatusTopic = paymentStatusTopic;
    }

    public void publishPaymentStatus(PaymentTransaction transaction, String status, String username) {
        OrderStatusEvent event = new OrderStatusEvent();
        event.setOrderId(transaction.getOrderId());
        event.setProductId(null);
        event.setQuantity(null);
        event.setAmount(transaction.getAmount());
        event.setStatus(status);
        event.setTransactionRef(transaction.getTransactionRef());
        event.setUsername(username);
        kafkaTemplate.send(paymentStatusTopic, String.valueOf(transaction.getOrderId()), event);
    }
}
