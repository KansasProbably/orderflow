package io.github.kansasprobably.orderflow.order.event;

import io.github.kansasprobably.orderflow.kafka.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTopicProperties kafkaTopicProperties;

    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        kafkaTemplate.send(
                kafkaTopicProperties.orderConfirmed(),
                event.orderId().toString(),
                event
        );
    }
}
