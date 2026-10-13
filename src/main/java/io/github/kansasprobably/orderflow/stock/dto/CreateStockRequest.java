package io.github.kansasprobably.orderflow.stock.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateStockRequest(

        @NotNull
        UUID productId,

        @NotNull
        UUID warehouseId,

        @NotNull
        @PositiveOrZero
        Integer availableQuantity
) {
}
