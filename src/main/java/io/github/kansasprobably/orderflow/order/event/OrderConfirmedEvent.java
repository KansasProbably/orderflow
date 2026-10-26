package io.github.kansasprobably.orderflow.order.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderConfirmedEvent(
        UUID eventId,
        UUID orderId,
        UUID customerId,
        OffsetDateTime confirmedAt
) {
}
