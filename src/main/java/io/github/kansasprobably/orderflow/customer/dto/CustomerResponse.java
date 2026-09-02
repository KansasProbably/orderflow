package io.github.kansasprobably.orderflow.customer.dto;

import io.github.kansasprobably.orderflow.customer.CustomerStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String email,
        String phone,
        CustomerStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
