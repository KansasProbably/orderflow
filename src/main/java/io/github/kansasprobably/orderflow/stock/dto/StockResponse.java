package io.github.kansasprobably.orderflow.stock.dto;

import java.util.UUID;

public record StockResponse(
        UUID id,
        UUID productId,
        UUID warehouseId,
        Integer availableQuantity,
        Integer reservedQuantity
) {
}
