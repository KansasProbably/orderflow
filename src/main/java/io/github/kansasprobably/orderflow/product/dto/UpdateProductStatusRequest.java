package io.github.kansasprobably.orderflow.product.dto;

import io.github.kansasprobably.orderflow.product.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductStatusRequest(

        @NotNull
        ProductStatus status

) {
}
