package io.github.kansasprobably.orderflow.stock.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException() {
        super("Requested quantity is not available");
    }
}
