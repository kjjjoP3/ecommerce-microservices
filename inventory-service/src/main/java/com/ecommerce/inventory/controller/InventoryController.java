package com.ecommerce.inventory.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/inventory")
public class InventoryController {
    @PostMapping("/reserve/{productId}")
    public ResponseEntity<String> reserve(@PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity) {
        return ResponseEntity.ok("RESERVED");
    }
    @PostMapping("/release/{productId}")
    public ResponseEntity<String> release(@PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity) {
        return ResponseEntity.ok("RELEASED");
    }
}
