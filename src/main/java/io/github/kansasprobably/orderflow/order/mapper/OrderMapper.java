package io.github.kansasprobably.orderflow.order.mapper;

import io.github.kansasprobably.orderflow.order.Order;
import io.github.kansasprobably.orderflow.order.OrderItem;
import io.github.kansasprobably.orderflow.order.dto.OrderItemResponse;
import io.github.kansasprobably.orderflow.order.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getItems().stream()
                        .map(this::toItemResponse)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getWarehouse().getId(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
