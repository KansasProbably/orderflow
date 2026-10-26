package io.github.kansasprobably.orderflow.order.event;

import io.github.kansasprobably.orderflow.kafka.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTopicProperties kafkaTopicProperties;

    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, OrderConfirmedEvent>> publishOrderConfirmed(OrderConfirmedEvent event) {
        return kafkaTemplate.send(
                kafkaTopicProperties.orderConfirmed(),
                event.orderId().toString(),
                event
        );
    }
}
