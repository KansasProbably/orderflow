package io.github.kansasprobably.orderflow.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateOrderItemRequest(

        @NotNull
        UUID warehouseId,
        
        @NotNull
        UUID productId,

        @NotNull
        @Positive
        Integer quantity
) {
}
