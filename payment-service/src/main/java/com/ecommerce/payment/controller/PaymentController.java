package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentProcessResponse;
import com.ecommerce.payment.dto.PaymentStatusResponse;
import com.ecommerce.payment.entity.PaymentTransaction;
import com.ecommerce.payment.service.PaymentService;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentProcessResponse> process(
            @RequestParam("orderId") Long orderId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "method", required = false, defaultValue = "MANUAL") String method) {
        PaymentTransaction transaction = paymentService.startPayment(orderId, amount, username, method);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            transaction = paymentService.markFailed(transaction.getTransactionRef(), "INVALID_AMOUNT", username);
            return ResponseEntity.badRequest().body(toProcessResponse(transaction));
        }
        if (!"VNPAY".equalsIgnoreCase(method)) {
            transaction = paymentService.markPaid(transaction.getTransactionRef(), "MOCK_TXN_" + transaction.getId(), "MOCK_NO_" + transaction.getId(), username);
        }
        return ResponseEntity.ok(toProcessResponse(transaction));
    }

    @GetMapping("/{transactionRef}")
    public ResponseEntity<PaymentStatusResponse> getTransaction(@PathVariable("transactionRef") String transactionRef) {
        return ResponseEntity.ok(toStatusResponse(paymentService.getByTransactionRef(transactionRef)));
    }

    private PaymentProcessResponse toProcessResponse(PaymentTransaction transaction) {
        PaymentProcessResponse response = new PaymentProcessResponse();
        response.setTransactionRef(transaction.getTransactionRef());
        response.setOrderId(transaction.getOrderId());
        response.setAmount(transaction.getAmount());
        response.setStatus(transaction.getStatus());
        response.setMethod(transaction.getMethod());
        response.setRedirectUrl("/payment-" + ("PAID".equalsIgnoreCase(transaction.getStatus()) ? "success" : "failed") + "?transactionRef=" + transaction.getTransactionRef());
        return response;
    }

    private PaymentStatusResponse toStatusResponse(PaymentTransaction transaction) {
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setTransactionRef(transaction.getTransactionRef());
        response.setOrderId(transaction.getOrderId());
        response.setAmount(transaction.getAmount());
        response.setStatus(transaction.getStatus());
        response.setMethod(transaction.getMethod());
        return response;
    }
}
