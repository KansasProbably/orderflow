package io.github.kansasprobably.orderflow.product.exception;

import io.github.kansasprobably.orderflow.order.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Invalid order status transition from " + from + " to " + to);
    }
}
