package io.github.kansasprobably.orderflow.order;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.product.exception.InvalidOrderStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class OrderTest {

    @Test
    void shouldConfirmNewOrder() {
        Customer customer = createCustomer();

        Order order = new Order(customer);

        order.confirm();

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldCancelNewOrder() {
        Customer customer = createCustomer();

        Order order = new Order(customer);

        order.cancel();

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldCancelConfirmedOrder() {
        Customer customer = createCustomer();

        Order order = new Order(customer);

        order.confirm();
        order.cancel();

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldNotConfirmCancelledOrder() {
        Customer customer = createCustomer();

        Order order = new Order(customer);

        order.cancel();

        assertThatThrownBy(order::confirm)
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldNotCancelCompletedOrder(){
        Customer customer = createCustomer();

        Order order = new Order(customer);

        order.confirm();
        order.complete();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderStatusTransitionException.class);

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.COMPLETED);
    }

    private Customer createCustomer() {
        return new Customer(
                        "Vasyan",
                        "Vasya@mail.ru",
                        "+79995221798"

        );
    }
}
