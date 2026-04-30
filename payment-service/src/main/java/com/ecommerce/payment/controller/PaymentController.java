package com.ecommerce.payment.controller;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/payments")
public class PaymentController {
    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestParam Long orderId, @RequestParam BigDecimal amount) {
        return amount.compareTo(new BigDecimal("0")) > 0 ? ResponseEntity.ok("PAID") : ResponseEntity.badRequest().body("FAILED");
    }
}
