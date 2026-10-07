package io.github.kansasprobably.orderflow.stock.exception;

public class InsufficientReservedStockException extends RuntimeException {
    public InsufficientReservedStockException(Integer reservedQuantity,
                                              Integer releaseQuantity
    ) {
        super("Cannot release " + releaseQuantity + " items because only " + reservedQuantity + " are reserved");
    }
}
