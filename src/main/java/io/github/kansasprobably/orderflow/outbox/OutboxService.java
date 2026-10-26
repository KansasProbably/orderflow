package io.github.kansasprobably.orderflow.outbox;

import io.github.kansasprobably.orderflow.order.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    public void saveOrderConfirmedEvent(OrderConfirmedEvent event) {
        String payload;

        try {
            payload = jsonMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new OutboxSerializationException(exception);
        }

        OutboxEvent outboxEvent = new OutboxEvent(
                event.eventId(),
                "ORDER",
                event.orderId(),
                OutboxEventType.ORDER_CONFIRMED,
                payload
        );

        outboxEventRepository.save(outboxEvent);
    }
}
