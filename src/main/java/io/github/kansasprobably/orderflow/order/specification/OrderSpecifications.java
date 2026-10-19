package io.github.kansasprobably.orderflow.order.specification;

import io.github.kansasprobably.orderflow.order.Order;
import io.github.kansasprobably.orderflow.order.OrderStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSpecifications {

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<Order> hasCustomerId(UUID customerId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("customer").get("id"),
                        customerId
                );
    }

    public static Specification<Order> hasBeenCreatedFrom(OffsetDateTime createdFrom) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        createdFrom
                );
    }

    public static Specification<Order> hasBeenCreatedTo(OffsetDateTime createdTo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        createdTo
                );
    }

}
