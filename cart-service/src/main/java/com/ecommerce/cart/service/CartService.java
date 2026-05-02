package com.ecommerce.cart.service;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItemEntity;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<CartItem> getCart(String username) {
        return getOrCreateCart(username).getItems().stream().map(this::toModel).collect(Collectors.toList());
    }

    public List<CartItem> addItem(String username, CartItem item) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().stream()
                .filter(existing -> existing.getProductId().equals(item.getProductId()))
                .findFirst()
                .ifPresentOrElse(existing -> existing.setQuantity(existing.getQuantity() + item.getQuantity()), () -> {
                    CartItemEntity entity = new CartItemEntity();
                    entity.setCart(cart);
                    entity.setProductId(item.getProductId());
                    entity.setName(item.getName());
                    entity.setAmount(item.getAmount());
                    entity.setQuantity(item.getQuantity());
                    cart.getItems().add(entity);
                });
        cart.setUpdatedAt(LocalDateTime.now());
        return toModelList(cartRepository.save(cart));
    }

    public List<CartItem> updateQty(String username, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().stream().filter(item -> item.getProductId().equals(productId)).findFirst().ifPresent(item -> item.setQuantity(quantity));
        cart.setUpdatedAt(LocalDateTime.now());
        return toModelList(cartRepository.save(cart));
    }

    public List<CartItem> removeItem(String username, Long productId) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        cart.setUpdatedAt(LocalDateTime.now());
        return toModelList(cartRepository.save(cart));
    }

    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUsername(username);
            cart.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(cart);
        });
    }

    private CartItem toModel(CartItemEntity entity) {
        return new CartItem(entity.getProductId(), entity.getName(), entity.getAmount(), entity.getQuantity());
    }

    private List<CartItem> toModelList(Cart cart) {
        return cart.getItems().stream().map(this::toModel).collect(Collectors.toList());
    }
}
