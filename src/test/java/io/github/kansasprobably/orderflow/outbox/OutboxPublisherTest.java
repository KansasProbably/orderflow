package io.github.kansasprobably.orderflow.outbox;

import io.github.kansasprobably.orderflow.order.event.OrderConfirmedEvent;
import io.github.kansasprobably.orderflow.order.event.OrderEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @Test
    void shouldKeepEventNewWhenKafkaPublishFails() {

        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent(
                eventId,
                orderId,
                customerId,
                OffsetDateTime.now()
        );

        OutboxEvent outboxEvent = new OutboxEvent(
                eventId,
                "ORDER",
                orderId,
                OutboxEventType.ORDER_CONFIRMED,
                "payload"
        );

        when(outboxEventRepository.findAllByStatusOrderByCreatedAtAsc(OutboxEventStatus.NEW))
                .thenReturn(List.of(outboxEvent));

        when(jsonMapper.readValue(
                "payload",
                OrderConfirmedEvent.class
        ))
                .thenReturn(orderConfirmedEvent);

        CompletableFuture<SendResult<String, OrderConfirmedEvent>> failureFuture =
                new CompletableFuture<>();

        failureFuture.completeExceptionally(
                new RuntimeException("Kafka unavailable!")
        );

        when(orderEventProducer.publishOrderConfirmed(orderConfirmedEvent))
                .thenReturn(failureFuture);

        outboxPublisher.publishNewEvents();

        assertThat(outboxEvent.getStatus())
                .isEqualTo(OutboxEventStatus.NEW);

        assertThat(outboxEvent.getPublishedAt())
                .isNull();
    }
}
