package io.github.kansasprobably.orderflow.order.service;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.order.Order;
import io.github.kansasprobably.orderflow.order.OrderRepository;
import io.github.kansasprobably.orderflow.order.OrderService;
import io.github.kansasprobably.orderflow.order.event.OrderConfirmedEvent;
import io.github.kansasprobably.orderflow.order.event.OrderEventProducer;
import io.github.kansasprobably.orderflow.order.exception.InvalidOrderStatusTransitionException;
import io.github.kansasprobably.orderflow.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldPublishOrderConfirmedEventWhenOrderConfirmed() {

        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Customer customer = new Customer(
                "Vasyan",
                "vasya@mail.com",
                "79995221798"
        );

        ReflectionTestUtils.setField(customer,"id",customerId);

        Order order = new Order(customer);

        ReflectionTestUtils.setField(order,"id",orderId);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        orderService.confirmOrder(orderId);

        ArgumentCaptor<OrderConfirmedEvent> captor =
                ArgumentCaptor.forClass(OrderConfirmedEvent.class);

        verify(orderEventProducer)
                .publishOrderConfirmed(captor.capture());

        OrderConfirmedEvent event = captor.getValue();

        assertThat(event.eventId())
                .isNotNull();
        assertThat(event.orderId())
                .isEqualTo(orderId);
        assertThat(event.customerId())
                .isEqualTo(customerId);
        assertThat(event.confirmedAt())
                .isNotNull();
    }

    @Test
    void shouldNotPublishOrderConfirmedEventWhenOrderConfirmationFails() {

        UUID orderId = UUID.randomUUID();

        Customer customer = new Customer(
                "Vasyan",
                "vasya@mail.com",
                "79995221798"
        );

        Order order = new Order(customer);

        ReflectionTestUtils.setField(order,"id",orderId);

        order.cancel();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirmOrder(orderId))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);


        verify(orderEventProducer,never())
                .publishOrderConfirmed(any());

    }
}
