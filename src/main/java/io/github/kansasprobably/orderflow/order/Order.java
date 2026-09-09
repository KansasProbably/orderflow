package io.github.kansasprobably.orderflow.order;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    public Order(Customer customer) {
        this.customer = customer;
        this.status = OrderStatus.NEW;
    }

    public void addItem(Product product, Warehouse warehouse, Integer quantity) {
        OrderItem existingItem = items.stream()
                .filter(item ->
                        item.getProduct().getId()
                                .equals(product.getId())
                        &&
                        item.getWarehouse().getId()
                                .equals(warehouse.getId())
                )
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
            return;
        }

        OrderItem newItem = new OrderItem(
                this,
                product,
                warehouse,
                quantity,
                product.getPrice()
        );
        items.add(newItem);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

}
