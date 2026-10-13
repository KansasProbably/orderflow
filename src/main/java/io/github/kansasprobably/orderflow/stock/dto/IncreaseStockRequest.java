package io.github.kansasprobably.orderflow.stock.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IncreaseStockRequest(

        @NotNull
        @Positive
        Integer quantity

) {
}
