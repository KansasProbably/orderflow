package io.github.kansasprobably.orderflow.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        UUID warehouseId,
        Integer quantity,
        BigDecimal price
) {
}
