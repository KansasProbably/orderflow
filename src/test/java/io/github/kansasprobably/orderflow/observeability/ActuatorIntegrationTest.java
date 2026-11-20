package io.github.kansasprobably.orderflow.observeability;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.customer.CustomerRepository;
import io.github.kansasprobably.orderflow.order.OrderRepository;
import io.github.kansasprobably.orderflow.order.OrderService;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderItemRequest;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderRequest;
import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductRepository;
import io.github.kansasprobably.orderflow.stock.Stock;
import io.github.kansasprobably.orderflow.stock.StockRepository;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import io.github.kansasprobably.orderflow.warehouse.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class ActuatorIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7.4-alpine")
                    .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

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
    private OrderService orderService;


    @Test
    void shouldReturnUpHealthWithPostgresAndRedis() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.redis.status").value("UP"));
    }

    @Test
    void shouldIncrementOrdersCreatedMetricAfterOrderCreation() throws Exception {

        String beforeResponse = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        double before = getMetricValue(beforeResponse,
                "orders_creation_total");

        Customer customer = createCustomer();
        Product product = createProduct();
        Warehouse warehouse = createWarehouse();
        Stock stock = createStock(product, warehouse, 100, 0);

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

        String afterResponse = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        double after = getMetricValue(afterResponse, "orders_creation_total");

        assertThat(after).isEqualTo(before + 1);
    }

    private double getMetricValue(String prometheusResponse, String metricName) {

        String metricLine = prometheusResponse.lines()
                .filter(line -> line.startsWith(metricName + " "))
                .findFirst()
                .orElseThrow();

        String value = metricLine.split("\\s++")[1];

        return Double.parseDouble(value);
    }

    private Product createProduct() {
        return productRepository.save(
                new Product(
                        "SKU-001",
                        "Iphone",
                        new BigDecimal("100000.00")
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
