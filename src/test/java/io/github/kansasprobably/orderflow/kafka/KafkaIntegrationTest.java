package io.github.kansasprobably.orderflow.kafka;

import io.github.kansasprobably.orderflow.order.event.OrderConfirmedConsumer;
import io.github.kansasprobably.orderflow.order.event.OrderConfirmedEvent;
import io.github.kansasprobably.orderflow.order.event.OrderEventProducer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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

@SpringBootTest
@Testcontainers
public class KafkaIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Container
    @ServiceConnection
    static final KafkaContainer kafkaContainer =
            new KafkaContainer("apache/kafka-native:3.8.0");

    @Autowired
    private OrderEventProducer orderEventProducer;

    @MockitoSpyBean
    private OrderConfirmedConsumer orderConfirmedConsumer;

    @Test
    void shouldPublishAndConsumeOrderConfirmedEvent() {
        OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.now()
        );

        orderEventProducer.publishOrderConfirmed(orderConfirmedEvent);

        ArgumentCaptor<OrderConfirmedEvent> captor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);

        verify(orderConfirmedConsumer,timeout(5000))
                .consume(captor.capture());

        OrderConfirmedEvent received = captor.getValue();

        assertThat(received.eventId())
                .isEqualTo(orderConfirmedEvent.eventId());

        assertThat(received.orderId())
                .isEqualTo(orderConfirmedEvent.orderId());

        assertThat(received.customerId())
                .isEqualTo(orderConfirmedEvent.customerId());

        assertThat(received.confirmedAt().toInstant())
                .isEqualTo(orderConfirmedEvent.confirmedAt().toInstant());
    }


}
