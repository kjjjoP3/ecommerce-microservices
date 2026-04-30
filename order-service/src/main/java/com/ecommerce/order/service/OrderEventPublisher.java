package com.ecommerce.order.service;

import com.ecommerce.order.event.OrderStatusEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {
    private final KafkaTemplate<String, OrderStatusEvent> kafkaTemplate;
    private final String orderStatusTopic;

    public OrderEventPublisher(
            KafkaTemplate<String, OrderStatusEvent> kafkaTemplate,
            @Value("${app.kafka.topics.order-status}") String orderStatusTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderStatusTopic = orderStatusTopic;
    }

    public void publishOrderStatus(OrderStatusEvent event) {
        kafkaTemplate.send(orderStatusTopic, String.valueOf(event.getOrderId()), event);
    }
}
