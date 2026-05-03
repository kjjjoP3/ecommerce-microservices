package com.ecommerce.order.dto;

import java.util.List;

public class OrderSummaryResponse {
    private Long id;
    private Long customerId;
    private Double totalAmount;
    private String status;
    private String paymentStatus;
    private List<OrderItemSummaryResponse> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public List<OrderItemSummaryResponse> getItems() { return items; }
    public void setItems(List<OrderItemSummaryResponse> items) { this.items = items; }
}
