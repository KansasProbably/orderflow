package io.github.kansasprobably.orderflow.product.dto;

import io.github.kansasprobably.orderflow.product.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        BigDecimal price,
        ProductStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}
