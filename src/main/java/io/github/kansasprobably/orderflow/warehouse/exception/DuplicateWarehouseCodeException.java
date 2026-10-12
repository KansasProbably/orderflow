package io.github.kansasprobably.orderflow.warehouse.exception;

public class DuplicateWarehouseCodeException extends RuntimeException {
    public DuplicateWarehouseCodeException(String code) {
        super("Warehouse with code " + code + " already exists");
    }
}
