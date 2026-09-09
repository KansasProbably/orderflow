package io.github.kansasprobably.orderflow.order.dto;

import io.github.kansasprobably.orderflow.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        OrderStatus orderStatus,
        List<OrderItemResponse> items,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
