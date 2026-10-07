package io.github.kansasprobably.orderflow.order.service;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.customer.CustomerRepository;
import io.github.kansasprobably.orderflow.order.Order;
import io.github.kansasprobably.orderflow.order.OrderRepository;
import io.github.kansasprobably.orderflow.order.OrderService;
import io.github.kansasprobably.orderflow.order.OrderStatus;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderItemRequest;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderRequest;
import io.github.kansasprobably.orderflow.order.dto.OrderResponse;
import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductRepository;
import io.github.kansasprobably.orderflow.product.exception.InvalidOrderStatusTransitionException;
import io.github.kansasprobably.orderflow.stock.Stock;
import io.github.kansasprobably.orderflow.stock.StockRepository;
import io.github.kansasprobably.orderflow.stock.exception.InsufficientReservedStockException;
import io.github.kansasprobably.orderflow.stock.exception.InsufficientStockException;
import io.github.kansasprobably.orderflow.stock.exception.StockNotFoundException;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import io.github.kansasprobably.orderflow.warehouse.WarehouseRepository;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class OrderServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

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
    void shouldCreateOrder() {
        Customer customer = createCustomer();


        Product product = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );


        Warehouse warehouse = createWarehouse();

        createStock(product,warehouse,10,0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                product.getId(),
                                2
                        )
                )
        );


        OrderResponse response = orderService.createOrder(request);

        assertThat(response.id()).isNotNull();

        Order savedOrder = orderRepository.findById(response.id())
                .orElseThrow();

        assertThat(savedOrder.getId()).isEqualTo(response.id());
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void shouldSaveOrderItem() {
        Customer customer = createCustomer();

        Product product = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Warehouse warehouse = createWarehouse();

        createStock(product,warehouse,10,0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                product.getId(),
                                2
                        )
                )
        );

        OrderResponse response = orderService.createOrder(request);


        Map<String,Object> savedItem = jdbcTemplate.queryForMap(
                """
                SELECT product_id, warehouse_id, quantity
                FROM order_items
                WHERE order_id = ?
                """,
                response.id()
        );

        assertThat(savedItem.get("product_id"))
                .isEqualTo(product.getId());

        assertThat(savedItem.get("warehouse_id"))
                .isEqualTo(warehouse.getId());

        assertThat(savedItem.get("quantity"))
                .isEqualTo(2);
    }

    @Test
    void shouldUpdateStockWhenOrderCreated() {
        Customer customer = createCustomer();

        Product product = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Warehouse warehouse = createWarehouse();

        createStock(product,warehouse,10,0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                product.getId(),
                                2
                        )
                )
        );

        orderService.createOrder(request);

        Stock updatedStock = stockRepository.findByProductIdAndWarehouseId(
                product.getId(),
                warehouse.getId()
        )
                .orElseThrow();

        assertThat(updatedStock.getAvailableQuantity()).isEqualTo(8);
        assertThat(updatedStock.getReservedQuantity()).isEqualTo(2);
    }

    @Test
    void shouldRollbackWhenStockIsInsufficient() {
        Customer customer = createCustomer();

        Product firstProduct = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Product secondProduct = createProduct("SKU-002",
                "samsung",
                new BigDecimal("60000.00")
        );

        Warehouse warehouse = createWarehouse();

        Stock firstStock = createStock(firstProduct,warehouse,10,0);

        Stock secondStock = createStock(secondProduct,warehouse,1,0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                firstProduct.getId(),
                                2
                        ),
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                secondProduct.getId(),
                                5
                        )
                )
        );


        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class);

        Stock firstStockAfterRollback = stockRepository.findById(firstStock.getId())
                .orElseThrow();

        Stock secondStockAfterRollback = stockRepository.findById(secondStock.getId())
                .orElseThrow();

        assertThat(firstStockAfterRollback.getAvailableQuantity()).isEqualTo(10);
        assertThat(firstStockAfterRollback.getReservedQuantity()).isEqualTo(0);
        assertThat(secondStockAfterRollback.getAvailableQuantity()).isEqualTo(1);
        assertThat(secondStockAfterRollback.getReservedQuantity()).isEqualTo(0);

        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void shouldRollbackWhenStockIsNotFound() {
        Customer customer = createCustomer();

        Product firstProduct = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Product secondProduct = createProduct("SKU-002",
                "samsung",
                new BigDecimal("65000.00")
        );

        Warehouse warehouse = createWarehouse();

        Stock firstStock = createStock(firstProduct,warehouse,10,0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                firstProduct.getId(),
                                2
                        ),
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                secondProduct.getId(),
                                5
                        )
                )
        );


        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(StockNotFoundException.class);

        Stock firstStockAfterRollback = stockRepository.findById(firstStock.getId())
                .orElseThrow();

        assertThat(firstStockAfterRollback.getAvailableQuantity()).isEqualTo(10);
        assertThat(firstStockAfterRollback.getReservedQuantity()).isEqualTo(0);

        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void shouldKeepProductPriceSnapshotInOrderItem() {
        Customer customer = createCustomer();

        Product product = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Warehouse warehouse = createWarehouse();

        createStock(product,warehouse,10,0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                product.getId(),
                                2
                        )
                )
        );

        OrderResponse response = orderService.createOrder(request);

        jdbcTemplate.update(
                """
                UPDATE products
                SET price = ?
                WHERE id = ?
                """,
                new BigDecimal("67000.00"),
                product.getId()
        );

        BigDecimal orderItemPrice = jdbcTemplate.queryForObject(
                """
                SELECT price
                FROM order_items
                WHERE order_id = ?
                """,
                BigDecimal.class,
                response.id()
        );

        BigDecimal currentProductPrice = jdbcTemplate.queryForObject(
                """
                SELECT price
                FROM products
                WHERE id = ?        
                """,
                BigDecimal.class,
                product.getId()
        );

        assertThat(currentProductPrice).isEqualByComparingTo("67000.00");
        assertThat(orderItemPrice).isEqualByComparingTo("70000.00");
    }

    @Test
    void shouldFailWhenStockUpdatedConcurrently() {
        Product product = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Warehouse warehouse = createWarehouse();

        Stock stock = createStock(product,warehouse,10,0);

        EntityManager firstEntityManager = entityManagerFactory.createEntityManager();
        EntityManager secondEntityManager = entityManagerFactory.createEntityManager();

        EntityTransaction firstTransaction = firstEntityManager.getTransaction();
        EntityTransaction secondTransaction = secondEntityManager.getTransaction();

        try {
            firstTransaction.begin();
            secondTransaction.begin();

            Stock firstStock = firstEntityManager.find(
                    Stock.class,
                    stock.getId()
            );

            Stock secondStock = secondEntityManager.find(
                    Stock.class,
                    stock.getId()
            );

            firstStock.reserve(1);
            firstTransaction.commit();

            secondStock.reserve(1);

            assertThatThrownBy(secondTransaction::commit)
                    .isInstanceOf(RollbackException.class)
                    .hasCauseInstanceOf(OptimisticLockException.class);
        } finally {
            if (firstTransaction.isActive()) {
                firstTransaction.rollback();
            }

            if (secondTransaction.isActive()) {
                secondTransaction.rollback();
            }

            firstEntityManager.close();
            secondEntityManager.close();
        }

        Stock actualStock = stockRepository.findById(stock.getId())
                .orElseThrow();

        assertThat(actualStock.getAvailableQuantity())
                .isEqualTo(9);

        assertThat(actualStock.getReservedQuantity())
                .isEqualTo(1);
    }

    @Test
    void shouldCancelOrderAndReleaseStock() {
        Customer customer = createCustomer();

        Product product = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Warehouse warehouse = createWarehouse();

        Stock stock = createStock(product,warehouse,10,0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                product.getId(),
                                3
                        )
                )

        );

        OrderResponse createdOrder = orderService.createOrder(request);

        OrderResponse cancelledOrder = orderService.cancelOrder(createdOrder.id());

        Stock stockAfterCancellation = stockRepository.findById(stock.getId())
                .orElseThrow();

        Order orderAfterCancellation = orderRepository.findById(createdOrder.id())
                .orElseThrow();

        assertThat(cancelledOrder.orderStatus())
                .isEqualTo(OrderStatus.CANCELLED);
        assertThat(orderAfterCancellation.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);

        assertThat(stockAfterCancellation.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(stockAfterCancellation.getReservedQuantity())
                .isEqualTo(0);

    }

    @Test
    void shouldNotReleaseStockWhenOrderCancelledTwice() {

        Customer customer = createCustomer();

        Product product = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Warehouse warehouse = createWarehouse();

        Stock stock = createStock(product, warehouse, 10, 0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                product.getId(),
                                3
                        )
                )

        );

        OrderResponse createdOrder = orderService.createOrder(request);

        orderService.cancelOrder(createdOrder.id());

        assertThatThrownBy(() ->
                orderService.cancelOrder(createdOrder.id())
        )
                .isInstanceOf(
                        InvalidOrderStatusTransitionException.class
                );
        Stock stockAfterSecondCancellation = stockRepository.findById(stock.getId())
                .orElseThrow();

        Order orderAfterSecondCancellation = orderRepository.findById(createdOrder.id())
                .orElseThrow();

        assertThat(stockAfterSecondCancellation.getAvailableQuantity())
                .isEqualTo(10);
        assertThat(stockAfterSecondCancellation.getReservedQuantity())
                .isEqualTo(0);

        assertThat(orderAfterSecondCancellation.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);

    }

    @Test
    void shouldRollbackCancellationWhenStockReleaseFails() {
        Customer customer = createCustomer();

        Product firstProduct = createProduct("SKU-001",
                "iphone",
                new BigDecimal("70000.00")
        );

        Product secondProduct = createProduct("SKU-002",
                "samsung",
                new BigDecimal("60000.00")
        );

        Warehouse warehouse = createWarehouse();

        Stock firstStock = createStock(firstProduct,warehouse,10,0);
        Stock secondStock = createStock(secondProduct,warehouse,10,0);

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                firstProduct.getId(),
                                2
                        ),
                        new CreateOrderItemRequest(
                                warehouse.getId(),
                                secondProduct.getId(),
                                2
                        )
                )
        );

        OrderResponse createdOrder = orderService.createOrder(request);

        jdbcTemplate.update(
                """
                UPDATE stocks
                SET reserved_quantity = 0
                WHERE id = ?
                """,
                secondStock.getId()
        );

        assertThatThrownBy(
                () -> orderService.cancelOrder(createdOrder.id()))
                    .isInstanceOf(InsufficientReservedStockException.class);

        Stock firstStockAfterRollback = stockRepository
                .findById(firstStock.getId())
                .orElseThrow();

        Stock secondStockAfterRollback = stockRepository
                .findById(secondStock.getId())
                .orElseThrow();

        Order orderAfterRollback = orderRepository.findById(createdOrder.id())
                .orElseThrow();

        assertThat(firstStockAfterRollback.getAvailableQuantity())
                .isEqualTo(8);

        assertThat(firstStockAfterRollback.getReservedQuantity())
                .isEqualTo(2);

        assertThat(secondStockAfterRollback.getAvailableQuantity())
                .isEqualTo(8);

        assertThat(secondStockAfterRollback.getReservedQuantity())
                .isEqualTo(0);

        assertThat(orderAfterRollback.getStatus())
                .isEqualTo(OrderStatus.NEW);
    }

    private Product createProduct(String sku, String name, BigDecimal price) {
        return productRepository.save(
                new Product(
                        sku,
                        name,
                        price
                )
        );
    }

    private Customer createCustomer() {
        return customerRepository.save(
                new Customer(
                        "Vasyan",
                        "Vasya@mail.ru",
                        "+79995221798"
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

    private Stock createStock(Product product, Warehouse warehouse, Integer availableQuantity, Integer reservedQuantity) {
        return stockRepository.save(
                new Stock(
                        product,
                        warehouse,
                        availableQuantity,
                        reservedQuantity
                )
        );
    }
}
