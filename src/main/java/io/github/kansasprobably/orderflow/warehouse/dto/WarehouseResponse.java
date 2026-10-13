package io.github.kansasprobably.orderflow.warehouse.dto;

import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        String code,
        String name

) {
}
