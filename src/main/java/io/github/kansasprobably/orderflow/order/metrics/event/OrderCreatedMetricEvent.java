package io.github.kansasprobably.orderflow.order.metrics.event;

import java.util.UUID;

public record OrderCreatedMetricEvent(UUID orderId) {
}
