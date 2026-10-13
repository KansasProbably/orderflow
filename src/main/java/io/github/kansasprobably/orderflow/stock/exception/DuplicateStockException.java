package io.github.kansasprobably.orderflow.stock.exception;

import java.util.UUID;

public class DuplicateStockException extends RuntimeException {
    public DuplicateStockException(UUID productId, UUID warehouseId) {
        super("Stock for product " + productId + " and warehouse " + warehouseId + " already exists.");
    }
}
