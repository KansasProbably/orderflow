package io.github.kansasprobably.orderflow.product.service;

import io.github.kansasprobably.orderflow.config.cache.RedisCacheConfig;
import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductRepository;
import io.github.kansasprobably.orderflow.product.ProductService;
import io.github.kansasprobably.orderflow.product.ProductStatus;
import io.github.kansasprobably.orderflow.product.dto.ProductResponse;
import io.github.kansasprobably.orderflow.product.dto.UpdateProductStatusRequest;
import io.github.kansasprobably.orderflow.product.exception.ProductNotFoundException;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.cache.type=redis"
})
@Testcontainers
public class ProductCacheIntegrationTest {

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
    void shouldReadProductFromDatabaseOnceAndThenUseCache() {
        Product product = createProduct();

        Product savedProduct = productRepository.saveAndFlush(product);

        UUID productId = savedProduct.getId();

        ProductResponse firstResponse = productService.getProductById(productId);

        Cache cache =
                cacheManager.getCache(RedisCacheConfig.PRODUCTS_CACHE);


        assertThat(cache)
                .isNotNull();

        assertThat(cache.get(productId))
                .isNotNull();

        ProductResponse secondResponse = productService.getProductById(productId);

        assertThat(firstResponse.id())
                .isEqualTo(productId);

        assertThat(secondResponse.id())
                .isEqualTo(productId);

        verify(productRepository, times(1))
                .findById(productId);
    }

    @Test
    void shouldEvictProductFromCacheAfterStatusUpdate() {
        Product product = createProduct();

        Product savedProduct = productRepository.saveAndFlush(product);

        UUID productId = savedProduct.getId();

        productService.getProductById(productId);

        Cache productCache = cacheManager.getCache(RedisCacheConfig.PRODUCTS_CACHE);

        assertThat(productCache)
                .isNotNull();

        productService.updateStatus(productId,new UpdateProductStatusRequest(ProductStatus.INACTIVE));

        assertThat(productCache.get(productId))
                .isNull();
    }

    @Test
    void shouldReadProductFromDatabaseAfterCacheEviction() {
        Product product = createProduct();

        Product savedProduct = productRepository.saveAndFlush(product);

        UUID productId = savedProduct.getId();

        ProductResponse firstResponse = productService.getProductById(productId);

        assertThat(firstResponse.status())
                .isEqualTo(ProductStatus.ACTIVE);

        productService.updateStatus(productId,new UpdateProductStatusRequest(ProductStatus.INACTIVE));

        ProductResponse responseAfterUpdate = productService.getProductById(productId);

        assertThat(responseAfterUpdate.status())
                .isEqualTo(ProductStatus.INACTIVE);

        verify(productRepository,times(3))
                .findById(productId);

    }

    @Test
    void shouldReturnStaleValueUntilCacheIsEvicted() {
        Product product = createProduct();

        Product savedProduct = productRepository.saveAndFlush(product);

        UUID productId = savedProduct.getId();

        ProductResponse firstResponse = productService.getProductById(productId);

        assertThat(firstResponse.status())
                .isEqualTo(ProductStatus.ACTIVE);

        jdbcTemplate.update(
                """
                UPDATE products 
                SET product_status = ?
                WHERE id = ?
                """,
                ProductStatus.INACTIVE.name(),
                productId
        );

        ProductResponse staleResponse = productService.getProductById(productId);

        assertThat(staleResponse.status())
                .isEqualTo(ProductStatus.ACTIVE);

        Cache cache = cacheManager.getCache(RedisCacheConfig.PRODUCTS_CACHE);

        assertThat(cache)
                .isNotNull();

        cache.evict(productId);

        ProductResponse freshResponse = productService.getProductById(productId);

        assertThat(freshResponse.status())
                .isEqualTo(ProductStatus.INACTIVE);

        verify(productRepository, times(2))
                .findById(productId);
    }

    @Test
    void shouldNotCacheProductNotFoundException() {

        UUID productId = UUID.randomUUID();

        assertThatThrownBy(() ->
                productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class);

        assertThatThrownBy(() ->
                productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class);

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
