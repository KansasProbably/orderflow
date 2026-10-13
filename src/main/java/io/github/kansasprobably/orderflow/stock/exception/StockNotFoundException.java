package io.github.kansasprobably.orderflow.stock.exception;

import java.util.UUID;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(UUID id) {
        super("Stock with id " + id + " not found");
    }

    public StockNotFoundException(UUID productId, UUID warehouseId) {
        super("Stock with product id " + productId + " and warehouse id " + warehouseId + " not found");
    }
}
