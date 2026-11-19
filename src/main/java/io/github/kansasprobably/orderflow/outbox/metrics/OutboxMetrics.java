package io.github.kansasprobably.orderflow.outbox.metrics;

import io.github.kansasprobably.orderflow.outbox.OutboxEvent;
import io.github.kansasprobably.orderflow.outbox.OutboxEventRepository;
import io.github.kansasprobably.orderflow.outbox.OutboxEventStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OutboxMetrics {

    private final Counter published;

    private final Counter publishedFailed;

    public OutboxMetrics(MeterRegistry meterRegistry, OutboxEventRepository outboxEventRepository) {
        this.published = Counter.builder("outbox.published")
                .description("Total number of successfully published outbox events")
                .register(meterRegistry);
        this.publishedFailed = Counter.builder("outbox.publish.failed")
                .description("Total number of failed  outbox publish attempts")
                .register(meterRegistry);

        Gauge.builder(
                "outbox.events.pending",
                outboxEventRepository,
                repository -> repository.countByStatus(OutboxEventStatus.NEW)
        )
                .description("Current number of pending outbox events")
                .register(meterRegistry);
    }

    public void incrementPublished() {
        published.increment();
    }

    public void incrementPublishFailed() {
        publishedFailed.increment();
    }
}
