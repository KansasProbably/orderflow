package io.github.kansasprobably.orderflow.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
        @NotBlank
        @Size(max = 32)
        String code,

        @Size(max = 255)
        String name
) {
}
