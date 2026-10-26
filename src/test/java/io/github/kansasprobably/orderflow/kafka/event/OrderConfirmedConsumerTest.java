package io.github.kansasprobably.orderflow.kafka.event;

import io.github.kansasprobably.orderflow.order.event.OrderConfirmedConsumer;
import io.github.kansasprobably.orderflow.order.event.OrderConfirmedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class OrderConfirmedConsumerTest {

    private final OrderConfirmedConsumer orderConfirmedConsumer = new OrderConfirmedConsumer();

    @Test
    void shouldConsumeOrderConfirmedEvent(CapturedOutput capturedOutput) {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent(
                eventId,
                orderId,
                customerId,
                OffsetDateTime.now()
        );

        orderConfirmedConsumer.consume(orderConfirmedEvent);

        assertThat(capturedOutput)
                .contains("Order confirmed event received")
                .contains(eventId.toString())
                .contains(orderId.toString())
                .contains(customerId.toString());

    }
}
