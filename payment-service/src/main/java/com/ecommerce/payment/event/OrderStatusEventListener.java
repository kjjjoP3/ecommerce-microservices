package com.ecommerce.payment.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusEventListener.class);

    @KafkaListener(topics = "${app.kafka.topics.order-status}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderStatus(OrderStatusEvent event) {
        LOGGER.info("Payment service received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());
    }
}
