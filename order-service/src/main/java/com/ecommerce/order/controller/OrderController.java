package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.PaymentProcessResponse;
import com.ecommerce.order.event.OrderStatusEvent;
import com.ecommerce.order.service.OrderEventPublisher;
import com.ecommerce.order.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderEventPublisher orderEventPublisher;
    private final OrderService orderService;

    public OrderController(OrderEventPublisher orderEventPublisher, OrderService orderService) {
        this.orderEventPublisher = orderEventPublisher;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody OrderRequest request) {
        Long orderId = System.currentTimeMillis();
        List<OrderItemRequest> items = request.getItems();
        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("orderStatus", "FAILED", "message", "Cart items are required"));
        }

        for (OrderItemRequest item : items) {
            restTemplate.postForEntity("http://localhost:8084/api/v1/inventory/reserve/" + item.getProductId() + "?quantity=" + item.getQuantity(), null, String.class);
        }

        BigDecimal totalAmount = request.totalAmount();
        ResponseEntity<PaymentProcessResponse> paymentResponse = restTemplate.postForEntity(
                "http://localhost:8086/api/v1/payments/process?orderId=" + orderId +
                        "&amount=" + totalAmount +
                        "&username=" + (request.getUsername() == null ? "" : request.getUsername()) +
                        "&method=" + (request.getMethod() == null ? "MANUAL" : request.getMethod()),
                null,
                PaymentProcessResponse.class);

        PaymentProcessResponse payment = paymentResponse.getBody();
        if (payment == null || !"PAID".equalsIgnoreCase(payment.getStatus())) {
            for (OrderItemRequest item : items) {
                restTemplate.postForEntity("http://localhost:8084/api/v1/inventory/release/" + item.getProductId() + "?quantity=" + item.getQuantity(), null, String.class);
            }
            orderEventPublisher.publishOrderStatus(new OrderStatusEvent(orderId, null, null, totalAmount, "FAILED"));
            return ResponseEntity.badRequest().body(Map.of(
                    "orderStatus", "FAILED",
                    "paymentStatus", payment == null ? "FAILED" : payment.getStatus(),
                    "transactionRef", payment == null ? null : payment.getTransactionRef()));
        }

        orderService.savePaidOrder(request);
        orderEventPublisher.publishOrderStatus(new OrderStatusEvent(orderId, null, null, totalAmount, "PAID"));
        return ResponseEntity.ok(Map.of(
                "orderStatus", "PAID",
                "paymentStatus", payment.getStatus(),
                "transactionRef", payment.getTransactionRef(),
                "redirectUrl", payment.getRedirectUrl()));
    }
}
