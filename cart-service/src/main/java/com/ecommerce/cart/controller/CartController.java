package com.ecommerce.cart.controller;

import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.service.CartService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItem> getCart(@RequestHeader(value = "X-User-Name", required = false) String username) {
        return cartService.getCart(resolveUser(username));
    }

    @PostMapping("/items")
    public List<CartItem> addItem(@RequestHeader(value = "X-User-Name", required = false) String username, @RequestBody CartItem item) {
        return cartService.addItem(resolveUser(username), item);
    }

    @PutMapping("/items/{productId}")
    public List<CartItem> updateQty(@RequestHeader(value = "X-User-Name", required = false) String username, @PathVariable Long productId, @RequestParam Integer quantity) {
        return cartService.updateQty(resolveUser(username), productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    public List<CartItem> removeItem(@RequestHeader(value = "X-User-Name", required = false) String username, @PathVariable Long productId) {
        return cartService.removeItem(resolveUser(username), productId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader(value = "X-User-Name", required = false) String username) {
        cartService.clearCart(resolveUser(username));
        return ResponseEntity.noContent().build();
    }

    private String resolveUser(String username) {
        return (username == null || username.isBlank()) ? "guest" : username;
    }
}
