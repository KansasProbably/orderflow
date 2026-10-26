package io.github.kansasprobably.orderflow.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderConfirmedConsumer {

    @KafkaListener(topics = "${app.kafka.topics.order-confirmed}")
    public void consume(OrderConfirmedEvent orderConfirmedEvent) {
        log.info(
                "Order confirmed event received: eventId={}, orderId={}, customerId={}, confirmedAt={}",
                orderConfirmedEvent.eventId(),
                orderConfirmedEvent.orderId(),
                orderConfirmedEvent.customerId(),
                orderConfirmedEvent.confirmedAt()
        );
    }
}
