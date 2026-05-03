package com.ecommerce.cart.service;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItemEntity;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<CartItem> getCart(String username) {
        return getOrCreateCart(username).getItems().stream().map(this::toModel).collect(Collectors.toList());
    }

    public List<CartItem> addItem(String username, CartItem item) {
        validateItem(item);

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
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart(username);
        CartItemEntity item = cart.getItems().stream()
                .filter(existing -> existing.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: productId=" + productId));

        item.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());
        return toModelList(cartRepository.save(cart));
    }

    public List<CartItem> removeItem(String username, Long productId) {
        Cart cart = getOrCreateCart(username);
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new IllegalArgumentException("Cart item not found: productId=" + productId);
        }
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

    private void validateItem(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cart item must not be null");
        }
        if (item.getProductId() == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (item.getAmount() == null || item.getAmount() < 0) {
            throw new IllegalArgumentException("amount must be greater than or equal to 0");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
    }

    private CartItem toModel(CartItemEntity entity) {
        return new CartItem(entity.getProductId(), entity.getName(), entity.getAmount(), entity.getQuantity());
    }

    private List<CartItem> toModelList(Cart cart) {
        return cart.getItems().stream().map(this::toModel).collect(Collectors.toList());
    }
}
