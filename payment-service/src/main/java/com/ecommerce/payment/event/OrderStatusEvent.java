package com.ecommerce.payment.event;

import java.math.BigDecimal;

public class OrderStatusEvent {
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal amount;
    private String status;
    private String transactionRef;
    private String username;

    public OrderStatusEvent() {
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
