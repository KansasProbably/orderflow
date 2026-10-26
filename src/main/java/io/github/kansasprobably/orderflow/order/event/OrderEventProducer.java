package io.github.kansasprobably.orderflow.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final static String ORDER_CONFIRMED_TOPIC = "order-confirmed";

    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        kafkaTemplate.send(
                ORDER_CONFIRMED_TOPIC,
                event.orderId().toString(),
                event
        );
    }
}
