package io.github.kansasprobably.orderflow.stock.exception;

import java.util.UUID;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException() {
        super("Stock not found for product and warehouse");
    }
}
