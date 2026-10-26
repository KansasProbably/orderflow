package io.github.kansasprobably.orderflow.outbox;

import io.github.kansasprobably.orderflow.order.event.OrderConfirmedEvent;
import io.github.kansasprobably.orderflow.order.event.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final OutboxEventRepository outboxEventRepository;

    private final OrderEventProducer orderEventProducer;

    private final JsonMapper jsonMapper;

    @Scheduled(
            fixedDelayString = "${app.outbox.publisher.fixed-delay}",
            initialDelayString = "${app.outbox.publisher.initial-delay}"
    )
    @Transactional
    public void publishNewEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findAllByStatusOrderByCreatedAtAsc(OutboxEventStatus.NEW);

        for (OutboxEvent event : events) {
            publish(event);
        }
    }

    private void publish(OutboxEvent outboxEvent) {
        try {
            switch (outboxEvent.getEventType()) {
                case ORDER_CONFIRMED -> {
                    OrderConfirmedEvent orderConfirmedEvent =
                            jsonMapper.readValue(
                                    outboxEvent.getPayload(),
                                    OrderConfirmedEvent.class
                            );

                    orderEventProducer
                            .publishOrderConfirmed(orderConfirmedEvent)
                            .get(
                                    SEND_TIMEOUT_SECONDS,
                                    TimeUnit.SECONDS
                            );

                    outboxEvent.markAsPublished();
                }
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            log.warn(
                    "Outbox publishing interrupted: eventId={}",
                    outboxEvent.getId()
            );

        } catch (Exception exception) {
            log.error(
                    "Failed to publish outbox event: eventId={}, eventType={}",
                    outboxEvent.getId(),
                    outboxEvent.getEventType(),
                    exception
            );
        }
    }
}
