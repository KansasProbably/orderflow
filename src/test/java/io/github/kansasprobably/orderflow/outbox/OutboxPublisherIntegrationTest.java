package io.github.kansasprobably.orderflow.outbox;

import io.github.kansasprobably.orderflow.order.event.OrderConfirmedConsumer;
import io.github.kansasprobably.orderflow.order.event.OrderConfirmedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        properties = "app.outbox.publisher.initial-delay=3600000"
)
@Testcontainers
public class OutboxPublisherIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Container
    @ServiceConnection
    static final KafkaContainer kafkaContainer =
            new KafkaContainer("apache/kafka-native:4.3.1");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @MockitoSpyBean
    private OrderConfirmedConsumer orderConfirmedConsumer;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE outbox_events
                """);
    }

    @Test
    void shouldPublishNewOutboxEventAndMarkAsPublished() {

        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent(
                eventId,
                orderId,
                customerId,
                OffsetDateTime.now()
        );

        outboxService.saveOrderConfirmedEvent(orderConfirmedEvent);

        OutboxEvent beforePublish = outboxEventRepository.findById(eventId)
                .orElseThrow();

        assertThat(beforePublish.getStatus())
                .isEqualTo(OutboxEventStatus.NEW);

        outboxPublisher.publishNewEvents();

        ArgumentCaptor<OrderConfirmedEvent> captor =
                ArgumentCaptor.forClass(OrderConfirmedEvent.class);

        verify(orderConfirmedConsumer, timeout(5000))
                .consume(captor.capture());

        OrderConfirmedEvent received = captor.getValue();

        assertThat(received.eventId())
                .isEqualTo(eventId);
        assertThat(received.orderId())
                .isEqualTo(orderId);
        assertThat(received.customerId())
                .isEqualTo(customerId);
        assertThat(received.confirmedAt().toInstant())
                .isEqualTo(orderConfirmedEvent.confirmedAt().toInstant());


        OutboxEvent afterPublish = outboxEventRepository.findById(eventId)
                .orElseThrow();

        assertThat(afterPublish.getStatus())
                .isEqualTo(OutboxEventStatus.PUBLISHED);

        assertThat(afterPublish.getPublishedAt())
                .isNotNull();
    }
}
