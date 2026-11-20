package io.github.kansasprobably.orderflow.outbox.exception;

public class OutboxSerializationException extends RuntimeException {
    public OutboxSerializationException(Throwable cause) {
        super("Failed to serialize outbox event", cause);
    }
}
