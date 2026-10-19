package io.github.kansasprobably.orderflow.order.service;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.customer.CustomerRepository;
import io.github.kansasprobably.orderflow.order.Order;
import io.github.kansasprobably.orderflow.order.OrderRepository;
import io.github.kansasprobably.orderflow.order.OrderService;
import io.github.kansasprobably.orderflow.order.dto.OrderResponse;
import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductRepository;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import io.github.kansasprobably.orderflow.warehouse.WarehouseRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class OrderJpaPerformanceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;


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

    private Statistics statistics;

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

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    @Test
    void shouldLoadOrderWithItemsInSingleQuery() {
        Customer customer = createCustomer("Vasiliy", "vasya@mail.ru","+79995221798");

        Product firstProduct = createProduct("SKU-001", "iphone", new BigDecimal("80000.00"));
        Product secondProduct = createProduct("SKU-002","macbook",new BigDecimal("150000.00"));

        Warehouse warehouse = createWarehouse("MSK-01","Moscow warehouse");

        Order order = new Order(customer);

        order.addItem(firstProduct,warehouse,1);
        order.addItem(secondProduct,warehouse,2);

        orderRepository.saveAndFlush(order);

        statistics.clear();


        OrderResponse result = orderService.getOrderById(order.getId());
        assertThat(result.id()).isEqualTo(order.getId());
        assertThat(statistics.getPrepareStatementCount())
                .isEqualTo(1);
    }

    @Test
    void shouldLoadOrderPageWithItemsWithoutNPlusOne() {
        Customer customer = createCustomer("Vasiliy", "vasya@mail.ru","+79995221798");

        Product firstProduct = createProduct("SKU-001", "iphone", new BigDecimal("80000.00"));
        Product secondProduct = createProduct("SKU-002", "samsung", new BigDecimal("60000.00"));

        Warehouse warehouse = createWarehouse("MSK-01","Moscow warehouse");

        Order firstOrder = new Order(customer);
        firstOrder.addItem(firstProduct,warehouse,1);
        firstOrder.addItem(secondProduct,warehouse,1);

        Order secondOrder = new Order(customer);
        secondOrder.addItem(firstProduct,warehouse,1);
        secondOrder.addItem(secondProduct,warehouse,1);

        Order thirdOrder = new Order(customer);
        thirdOrder.addItem(firstProduct,warehouse,1);
        thirdOrder.addItem(secondProduct,warehouse,1);

        Order fourthOrder = new Order(customer);
        fourthOrder.addItem(firstProduct,warehouse,1);
        fourthOrder.addItem(secondProduct,warehouse,1);

        orderRepository.saveAllAndFlush(List.of(firstOrder,secondOrder,thirdOrder,fourthOrder));

        statistics.clear();

        Pageable pageable = PageRequest.of(0,3);

        Page<OrderResponse> result = orderService.getOrders(
                null,
                null,
                null,
                null,
                pageable
        );


        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(OrderResponse::id)
                .doesNotHaveDuplicates();

        assertThat(result.getContent())
                .allSatisfy(order ->
                        assertThat(order.items()).hasSize(2));

        assertThat(statistics.getPrepareStatementCount())
                .isEqualTo(2);
    }



    private Customer createCustomer(String name, String email, String phone){
        return customerRepository.save(
                new Customer(
                        name,
                        email,
                        phone
                )
        );
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

    private Warehouse createWarehouse(String code, String name) {
        return warehouseRepository.save(
                new Warehouse(
                        code,
                        name
                )
        );
    }



}
