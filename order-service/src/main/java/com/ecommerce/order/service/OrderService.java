package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Order savePaidOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId() == null ? 1L : request.getCustomerId());
        order.setTotalAmount(request.totalAmount().doubleValue());
        order.setStatus("PAID");
        order.setPaymentStatus("PAID");
        Order savedOrder = orderRepository.save(order);

        List<OrderItemRequest> items = request.getItems();
        if (items != null) {
            for (OrderItemRequest itemRequest : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProductId(itemRequest.getProductId());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setUnitPrice(itemRequest.getAmount());
                orderItemRepository.save(orderItem);
            }
        }

        return savedOrder;
    }
}
