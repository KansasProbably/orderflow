package io.github.kansasprobably.orderflow.stock;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.stock.exception.InsufficientReservedStockException;
import io.github.kansasprobably.orderflow.stock.exception.InsufficientStockException;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class StockTest {

    @Test
    void shouldReserveStock() {
        Product product = createProduct();
        Warehouse warehouse = createWarehouse();

        Stock stock = new Stock(
                product,
                warehouse,
                10,
                0
        );

        stock.reserve(3);

        assertThat(stock.getAvailableQuantity())
                .isEqualTo(7);
        assertThat(stock.getReservedQuantity())
                .isEqualTo(3);
    }

    @Test
    void shouldReleaseReservedStock() {
        Product product = createProduct();
        Warehouse warehouse = createWarehouse();

        Stock stock = new Stock(
                product,
                warehouse,
                7,
                3
        );

        stock.release(3);

        assertThat(stock.getAvailableQuantity())
                .isEqualTo(10);
        assertThat(stock.getReservedQuantity())
                .isEqualTo(0);
    }

    @Test
    void shouldNotReserveMoreThanAvailable() {
        Product product = createProduct();
        Warehouse warehouse = createWarehouse();

        Stock stock = new Stock(
                product,
                warehouse,
                10,
                0
        );

        assertThatThrownBy(() -> stock.reserve(12))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(stock.getAvailableQuantity())
                .isEqualTo(10);
        assertThat(stock.getReservedQuantity())
                .isEqualTo(0);

    }

    @Test
    void shouldNotReleaseMoreThanReserved() {
        Product product = createProduct();
        Warehouse warehouse = createWarehouse();

        Stock stock = new Stock(
                product,
                warehouse,
                7,
                3
        );

        assertThatThrownBy(() -> stock.release(4))
                .isInstanceOf(InsufficientReservedStockException.class);

        assertThat(stock.getAvailableQuantity())
                .isEqualTo(7);
        assertThat(stock.getReservedQuantity())
                .isEqualTo(3);

    }

    private Product createProduct() {
        return new Product(
                "SKU-01",
                "iphone duo",
                new BigDecimal("280000.00")
        );
    }

    private Warehouse createWarehouse() {
        return new Warehouse(
                "MSK-01",
                "Moscow warehouse"
        );
    }
}
