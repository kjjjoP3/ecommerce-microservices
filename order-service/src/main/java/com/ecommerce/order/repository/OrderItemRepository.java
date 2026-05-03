package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder_IdIn(List<Long> orderIds);
}
