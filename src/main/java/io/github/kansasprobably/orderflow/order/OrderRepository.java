package io.github.kansasprobably.orderflow.order;

import io.github.kansasprobably.orderflow.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface OrderRepository
        extends JpaRepository<Order, UUID>,
                JpaSpecificationExecutor<Order> {
}
