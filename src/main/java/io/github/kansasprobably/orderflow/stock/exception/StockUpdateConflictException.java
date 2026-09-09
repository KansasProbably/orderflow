package io.github.kansasprobably.orderflow.stock.exception;

public class StockUpdateConflictException extends RuntimeException {
    public StockUpdateConflictException() {
        super("Stock was modified by another operation");
    }
}
