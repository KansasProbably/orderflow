package io.github.kansasprobably.orderflow.product.service;

import io.github.kansasprobably.orderflow.config.cache.RedisCacheConfig;
import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductRepository;
import io.github.kansasprobably.orderflow.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.cache.type=redis",
        "app.cache.products.ttl=500ms"
})
@Testcontainers
public class ProductCacheTtlIntegrationTest {

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
    private CacheManager cacheManager;

    @Autowired
    private ProductService productService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabaseAndCache() {

        Cache cache = cacheManager.getCache(RedisCacheConfig.PRODUCTS_CACHE);
        if (cache != null) {
            cache.invalidate();
        }

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
    void shouldReadProductFromDatabaseAgainAfterCacheTtlExpired() throws InterruptedException {
        Product product = createProduct();

        Product savedproduct = productRepository.saveAndFlush(product);

        UUID productId = savedproduct.getId();

        productService.getProductById(productId);

        productService.getProductById(productId);

        verify(productRepository, times(1))
                .findById(productId);

        Thread.sleep(800);

        productService.getProductById(productId);

        verify(productRepository, times(2))
                .findById(productId);
    }

    private Product createProduct() {
        return new Product(
                "SKU-001",
                "iphone",
                new BigDecimal("100000.00")
        );
    }
}
