package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {
    private Long customerId;
    private String username;
    private String method;
    private List<OrderItemRequest> items;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public BigDecimal totalAmount() {
        return items == null ? BigDecimal.ZERO : items.stream()
                .map(item -> BigDecimal.valueOf(item.getAmount()).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
