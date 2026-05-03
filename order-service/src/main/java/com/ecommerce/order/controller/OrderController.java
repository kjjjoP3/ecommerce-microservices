package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderItemSummaryResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderSummaryResponse;
import com.ecommerce.order.dto.PaymentProcessResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.event.OrderStatusEvent;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OrderViewRepository;
import com.ecommerce.order.service.OrderEventPublisher;
import com.ecommerce.order.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderEventPublisher orderEventPublisher;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderViewRepository orderViewRepository;

    public OrderController(OrderEventPublisher orderEventPublisher, OrderService orderService, OrderRepository orderRepository, OrderViewRepository orderViewRepository) {
        this.orderEventPublisher = orderEventPublisher;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderViewRepository = orderViewRepository;
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

        Order savedOrder = orderService.savePaidOrder(request);
        orderEventPublisher.publishOrderStatus(new OrderStatusEvent(orderId, null, null, totalAmount, "PAID"));
        return ResponseEntity.ok(Map.of(
                "orderStatus", "PAID",
                "paymentStatus", payment.getStatus(),
                "transactionRef", payment.getTransactionRef(),
                "redirectUrl", payment.getRedirectUrl(),
                "orderId", savedOrder.getId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderSummaryResponse>> myOrders(@RequestHeader(value = "X-User-Name", required = false) String username) {

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        // Resolve username -> APP_USER.ID via auth-service
        Long customerId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = restTemplate.getForObject(
                    "http://localhost:8081/api/v1/auth/users/" + username + "/id",
                    Map.class);
            if (userInfo == null || !userInfo.containsKey("id")) {
                return ResponseEntity.notFound().build();
            }
            customerId = ((Number) userInfo.get("id")).longValue();
        } catch (HttpClientErrorException.NotFound e) {
            // auth-service trả 404 — username không tồn tại
            return ResponseEntity.notFound().build();
        } catch (ResourceAccessException e) {
            // auth-service không kết nối được
            log.error("Cannot reach auth-service: {}", e.getMessage());
            return ResponseEntity.status(503).build();
        } catch (Exception e) {
            log.error("Unexpected error resolving username '{}': {}", username, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }

        List<OrderSummaryResponse> response = orderViewRepository.findByCustomerIdOrderByIdDesc(customerId).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private OrderSummaryResponse toSummary(Order order) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setItems(order.getItems().stream().map(this::toItemSummary).collect(Collectors.toList()));
        return response;
    }

    private OrderItemSummaryResponse toItemSummary(OrderItem item) {
        OrderItemSummaryResponse response = new OrderItemSummaryResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        return response;
    }
}
