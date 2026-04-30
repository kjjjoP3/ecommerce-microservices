package com.ecommerce.order.controller;
import com.ecommerce.order.event.OrderStatusEvent;
import com.ecommerce.order.service.OrderEventPublisher;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
@RestController @RequestMapping("/api/v1/orders")
public class OrderController {
    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderEventPublisher orderEventPublisher;

    public OrderController(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    @PostMapping
    public Map<String, String> create(@RequestParam Long productId, @RequestParam Integer quantity, @RequestParam BigDecimal amount) {
        Long orderId = System.currentTimeMillis();
        restTemplate.postForEntity("http://localhost:8084/api/v1/inventory/reserve/" + productId + "?quantity=" + quantity, null, String.class);
        String payment = restTemplate.postForObject("http://localhost:8086/api/v1/payments/process?orderId=" + orderId + "&amount=" + amount, null, String.class);
        if (!"PAID".equals(payment)) {
            restTemplate.postForEntity("http://localhost:8084/api/v1/inventory/release/" + productId + "?quantity=" + quantity, null, String.class);
            orderEventPublisher.publishOrderStatus(new OrderStatusEvent(orderId, productId, quantity, amount, "FAILED"));
            return Map.of("orderStatus", "FAILED");
        }
        orderEventPublisher.publishOrderStatus(new OrderStatusEvent(orderId, productId, quantity, amount, "PAID"));
        return Map.of("orderStatus", "PAID");
    }
}
