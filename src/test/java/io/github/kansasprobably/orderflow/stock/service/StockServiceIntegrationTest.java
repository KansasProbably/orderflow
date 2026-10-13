package io.github.kansasprobably.orderflow.stock.service;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductRepository;
import io.github.kansasprobably.orderflow.stock.Stock;
import io.github.kansasprobably.orderflow.stock.StockRepository;
import io.github.kansasprobably.orderflow.stock.StockService;
import io.github.kansasprobably.orderflow.stock.dto.CreateStockRequest;
import io.github.kansasprobably.orderflow.stock.dto.StockResponse;
import io.github.kansasprobably.orderflow.stock.exception.DuplicateStockException;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import io.github.kansasprobably.orderflow.warehouse.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class StockServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private  JdbcTemplate jdbcTemplate;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;


    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE 
                    order_items,
                    orders,
                    stocks,
                    warehouses,
                    products,
                    customers
                CASCADE
                """);
    }

    @Test
    void shouldCreateAndPersistStock() {
        Product product = createProduct();
        Warehouse warehouse = createWarehouse();

        CreateStockRequest request = new CreateStockRequest(
                product.getId(),
                warehouse.getId(),
                10
        );

        StockResponse response = stockService.createStock(request);

        Stock savedStock = stockRepository.findById(response.id())
                .orElseThrow();

        assertThat(savedStock.getProduct().getId())
                .isEqualTo(product.getId());

        assertThat(savedStock.getWarehouse().getId())
                .isEqualTo(warehouse.getId());

        assertThat(savedStock.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(savedStock.getReservedQuantity())
                .isEqualTo(0);

        assertThat(savedStock.getVersion())
                .isZero();
    }

    @Test
    void shouldNotCreateDuplicateStock() {
        Product product = createProduct();
        Warehouse warehouse = createWarehouse();

        CreateStockRequest firstRequest = new CreateStockRequest(
                product.getId(),
                warehouse.getId(),
                10
        );

        CreateStockRequest secondRequest = new CreateStockRequest(
                product.getId(),
                warehouse.getId(),
                15
        );

        StockResponse response = stockService.createStock(firstRequest);

        assertThatThrownBy(
                () -> stockService.createStock(secondRequest)
        )
                .isInstanceOf(DuplicateStockException.class);

        assertThat(stockRepository.count())
                .isEqualTo(1);
    }

    @Test
    void shouldNotPersistDuplicateProductWarehouseStock() {
        Product product = createProduct();
        Warehouse warehouse = createWarehouse();

        Stock firstStock = new Stock(
                product,
                warehouse,
                10,
                0
        );

        Stock diplicateStock = new Stock(
                product,
                warehouse,
                20,
                0
        );

        stockRepository.saveAndFlush(firstStock);

        assertThatThrownBy(
                () -> stockRepository.saveAndFlush(diplicateStock)
        )
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(stockRepository.count())
                .isEqualTo(1);
    }

    private Product createProduct() {
        return productRepository.save(
                new Product(
                        "SKU-01",
                        "iphone duo",
                        new BigDecimal("280000.00")
                )
        );
    }

    private Warehouse createWarehouse() {
        return warehouseRepository.save(
                new Warehouse(
                "MSK-01",
                "Moscow warehouse"
                )
        );
    }
}
