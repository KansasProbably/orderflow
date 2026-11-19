package io.github.kansasprobably.orderflow.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersConfirmed;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.ordersCreated = Counter.builder("orders.creation")
                .description("Total number of created orders")
                .register(meterRegistry);

        this.ordersConfirmed = Counter.builder("orders.confirmed")
                .description("Total number of confirmed orders")
                .register(meterRegistry);
    }

    public void incrementCreated() {
        ordersCreated.increment();
    }

    public void incrementConfirmed() {
        ordersConfirmed.increment();
    }
}
