package io.github.kansasprobably.orderflow.kafka.event;


import io.github.kansasprobably.orderflow.kafka.KafkaTopicProperties;
import io.github.kansasprobably.orderflow.order.event.OrderConfirmedEvent;
import io.github.kansasprobably.orderflow.order.event.OrderEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    @Mock
    private KafkaTopicProperties kafkaTopicProperties;

    @InjectMocks
    private OrderEventProducer orderEventProducer;

    @Test
    void shouldPublishOrderConfirmedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent(
                eventId,
                orderId,
                customerId,
                OffsetDateTime.now()
        );

        when(kafkaTopicProperties.orderConfirmed())
                .thenReturn("order-confirmed");

        orderEventProducer.publishOrderConfirmed(orderConfirmedEvent);

        verify(kafkaTemplate).send(
                "order-confirmed",
                orderId.toString(),
                orderConfirmedEvent
        );
    }
}
