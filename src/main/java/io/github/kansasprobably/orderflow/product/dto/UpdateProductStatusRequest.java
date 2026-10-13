package io.github.kansasprobably.orderflow.product.dto;

import io.github.kansasprobably.orderflow.product.ProductStatus;
import jakarta.validation.constraints.NotNull;


public record UpdateProductStatusRequest(

        @NotNull
        ProductStatus status

) {
}
