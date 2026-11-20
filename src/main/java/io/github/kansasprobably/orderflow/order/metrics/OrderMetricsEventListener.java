package io.github.kansasprobably.orderflow.order.metrics;

import io.github.kansasprobably.orderflow.order.metrics.event.OrderConfirmedMetricEvent;
import io.github.kansasprobably.orderflow.order.metrics.event.OrderCreatedMetricEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderMetricsEventListener {

    private final OrderMetrics orderMetrics;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedMetricEvent orderCreatedMetricEvent) {
        orderMetrics.incrementCreated();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderConfirmed(OrderConfirmedMetricEvent orderConfirmedMetricEvent) {
        orderMetrics.incrementConfirmed();
    }
}
