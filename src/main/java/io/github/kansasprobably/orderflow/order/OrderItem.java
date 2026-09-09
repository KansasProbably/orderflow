package io.github.kansasprobably.orderflow.order;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items",
    uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_order_item_order_product_warehouse",
                columnNames = {
                        "order_id",
                        "product_id",
                        "warehouse_id"
                }
        )
    })
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Positive
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Positive
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    public OrderItem(Order order, Product product, Warehouse warehouse, Integer quantity, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.price = price;
    }

    public void increaseQuantity(Integer quantity) {
        this.quantity += quantity;
    }
}
