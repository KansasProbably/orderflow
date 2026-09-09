package io.github.kansasprobably.orderflow.stock;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.stock.exception.InsufficientStockException;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "stocks",
        uniqueConstraints = {
                @UniqueConstraint(
                    name = "uk_stock_product_warehouse",
                        columnNames = {
                            "product_id",
                                "warehouse_id"
                        }
                )
        })
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "warehouse_id",
            nullable = false
    )
    private Warehouse warehouse;

    @PositiveOrZero
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @PositiveOrZero
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    public Stock(Product product, Warehouse warehouse, Integer availableQuantity, Integer reservedQuantity) {
        this.product = product;
        this.warehouse = warehouse;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    public void reserve(Integer quantity) {
        if (availableQuantity < quantity) {
            throw new InsufficientStockException();
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }
}
