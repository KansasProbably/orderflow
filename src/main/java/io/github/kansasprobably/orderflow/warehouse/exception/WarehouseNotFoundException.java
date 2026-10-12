package io.github.kansasprobably.orderflow.warehouse.exception;

import java.util.UUID;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(UUID id) {
        super("Warehouse with id " + id + " not found");
    }
}
