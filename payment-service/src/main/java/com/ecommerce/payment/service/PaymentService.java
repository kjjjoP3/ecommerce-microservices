package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.PaymentTransaction;
import com.ecommerce.payment.repository.PaymentTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final RestTemplate restTemplate;
    private final String cartServiceBaseUrl;

    public PaymentService(
            PaymentTransactionRepository paymentTransactionRepository,
            PaymentEventPublisher paymentEventPublisher,
            RestTemplate restTemplate,
            @Value("${app.services.cart-base-url}") String cartServiceBaseUrl) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentEventPublisher = paymentEventPublisher;
        this.restTemplate = restTemplate;
        this.cartServiceBaseUrl = cartServiceBaseUrl;
    }

    public PaymentTransaction startPayment(Long orderId, BigDecimal amount, String username, String method) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setMethod(method == null || method.isBlank() ? "MANUAL" : method);
        transaction.setStatus("PENDING");
        transaction.setTransactionRef(UUID.randomUUID().toString());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        return paymentTransactionRepository.save(transaction);
    }

    public PaymentTransaction markPaid(String transactionRef, String vnpTxnRef, String vnpTransactionNo, String username) {
        PaymentTransaction transaction = getRequiredTransaction(transactionRef);
        clearCart(username);
        transaction.setStatus("PAID");
        transaction.setVnpTxnRef(vnpTxnRef);
        transaction.setVnpTransactionNo(vnpTransactionNo);
        transaction.setUpdatedAt(LocalDateTime.now());
        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        paymentEventPublisher.publishPaymentStatus(saved, "PAID", username);
        return saved;
    }

    public PaymentTransaction markFailed(String transactionRef, String reason, String username) {
        PaymentTransaction transaction = getRequiredTransaction(transactionRef);
        transaction.setStatus("FAILED");
        transaction.setUpdatedAt(LocalDateTime.now());
        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        paymentEventPublisher.publishPaymentStatus(saved, "FAILED", username);
        return saved;
    }

    public Map<String, Object> buildCheckoutResponse(PaymentTransaction transaction) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionRef", transaction.getTransactionRef());
        response.put("orderId", transaction.getOrderId());
        response.put("amount", transaction.getAmount());
        response.put("status", transaction.getStatus());
        response.put("redirectUrl", "/payment-" + ("PAID".equalsIgnoreCase(transaction.getStatus()) ? "success" : "failed") + "?transactionRef=" + transaction.getTransactionRef());
        return response;
    }

    public PaymentTransaction getByTransactionRef(String transactionRef) {
        return getRequiredTransaction(transactionRef);
    }

    private PaymentTransaction getRequiredTransaction(String transactionRef) {
        return paymentTransactionRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + transactionRef));
    }

    private void clearCart(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Name", username);
        restTemplate.exchange(cartServiceBaseUrl + "/api/v1/cart", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }
}
