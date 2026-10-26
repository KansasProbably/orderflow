package io.github.kansasprobably.orderflow.outbox;

public class OutboxSerializationException extends RuntimeException {
    public OutboxSerializationException(Throwable cause) {
        super("Failed to serialize outbox event", cause);
    }
}
