package io.github.kansasprobably.orderflow.user.dto;

import io.github.kansasprobably.orderflow.user.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        UserRole role,
        OffsetDateTime createdAt
) {
}
