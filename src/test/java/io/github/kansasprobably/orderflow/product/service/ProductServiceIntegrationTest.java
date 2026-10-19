package io.github.kansasprobably.orderflow.product.service;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductRepository;
import io.github.kansasprobably.orderflow.product.ProductService;
import io.github.kansasprobably.orderflow.product.ProductStatus;
import io.github.kansasprobably.orderflow.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Testcontainers
public class ProductServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void shouldReturnAllProductsWithoutFilters() {
        Product firstProduct = new Product(
                "SKU-001",
                "iphone",
                new BigDecimal("75000.00")
        );

        Product secondProduct = new Product(
                "SKU-002",
                "samsung",
                new BigDecimal("65000.00")
        );

        Product thirdProduct = new Product(
                "SKU-003",
                "xiaomi",
                new BigDecimal("55000.00")
        );

        productRepository.saveAll(List.of(firstProduct,secondProduct,thirdProduct));


        Pageable pageable = PageRequest.of(0,20);

        Page<ProductResponse> result = productService.getProducts(
                null,
                null,
                pageable
        );

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void shouldFilterProductsByStatus() {

        Product firstProduct = new Product(
                "SKU-001",
                "iphone",
                new BigDecimal("75000.00")
        );

        Product secondProduct = new Product(
                "SKU-002",
                "samsung",
                new BigDecimal("65000.00")
        );

        Product thirdProduct = new Product(
                "SKU-003",
                "xiaomi",
                new BigDecimal("55000.00")
        );

        thirdProduct.changeStatus(ProductStatus.INACTIVE);

        productRepository.saveAll(List.of(firstProduct,secondProduct,thirdProduct));

        Pageable pageable = PageRequest.of(0,20);

        Page<ProductResponse> result = productService.getProducts(
                ProductStatus.ACTIVE,
                null,
                pageable
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(ProductResponse::status)
                .containsOnly(ProductStatus.ACTIVE);
    }

    @Test
    void shouldFilterProductsByNameIgnoringCase() {
        Product firstProduct = new Product(
                "SKU-001",
                "iphone",
                new BigDecimal("75000.00")
        );

        Product secondProduct = new Product(
                "SKU-002",
                "IPHONE 13 case",
                new BigDecimal("300.00")
        );

        Product thirdProduct = new Product(
                "SKU-003",
                "xiaomi",
                new BigDecimal("55000.00")
        );

        productRepository.saveAll(List.of(firstProduct,secondProduct,thirdProduct));

        Pageable pageable = PageRequest.of(0,20);

        Page<ProductResponse> result = productService.getProducts(
                null,
                "iphone",
                pageable
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(ProductResponse::name)
                .containsExactlyInAnyOrder("iphone","IPHONE 13 case");
    }

    @Test
    void shouldFilterProductsByNameAndStatus() {
        Product firstProduct = new Product(
                "SKU-001",
                "iphone",
                new BigDecimal("75000.00")
        );

        Product secondProduct = new Product(
                "SKU-002",
                "IPHONE 13 case",
                new BigDecimal("300.00")
        );

        Product thirdProduct = new Product(
                "SKU-003",
                "xiaomi",
                new BigDecimal("55000.00")
        );

        secondProduct.changeStatus(ProductStatus.INACTIVE);

        productRepository.saveAll(List.of(firstProduct,secondProduct,thirdProduct));

        Pageable pageable = PageRequest.of(0,20);

        Page<ProductResponse> result = productService.getProducts(
                ProductStatus.ACTIVE,
                "iphone",
                pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        assertThat(result.getContent().getFirst().sku())
                .isEqualTo("SKU-001");
    }

    @Test
    void shouldReturnRequestedProductPage() {
        Product firstProduct = new Product(
                "SKU-001",
                "iphone",
                new BigDecimal("75000.00")
        );

        Product secondProduct = new Product(
                "SKU-002",
                "IPHONE 13 case",
                new BigDecimal("300.00")
        );

        Product thirdProduct = new Product(
                "SKU-003",
                "xiaomi",
                new BigDecimal("55000.00")
        );

        Product fourthProduct = new Product(
                "SKU-004",
                "lg",
                new BigDecimal("55000.00")
        );

        Product fifthProduct = new Product(
                "SKU-005",
                "nokia",
                new BigDecimal("55000.00")
        );

        productRepository.saveAll(List.of(firstProduct,secondProduct,thirdProduct, fourthProduct, fifthProduct));

        Pageable pageable = PageRequest.of(1,2, Sort.by("sku").ascending());

        Page<ProductResponse> result = productService.getProducts(
                null,
                null,
                pageable
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(ProductResponse::sku)
                .containsExactly("SKU-003","SKU-004");

    }

    @Test
    void shouldSortProductsByPriceDescending() {
        Product firstProduct = new Product(
                "SKU-001",
                "iphone",
                new BigDecimal("75000.00")
        );

        Product secondProduct = new Product(
                "SKU-002",
                "IPHONE 13 case",
                new BigDecimal("300.00")
        );

        Product thirdProduct = new Product(
                "SKU-003",
                "xiaomi",
                new BigDecimal("55000.00")
        );

        productRepository.saveAll(List.of(firstProduct,secondProduct,thirdProduct));

        Pageable pageable = PageRequest.of(0,20, Sort.by("price").descending());

        Page<ProductResponse> result = productService.getProducts(
                null,
                null,
                pageable
        );

        assertThat(result.getContent())
                .extracting(ProductResponse::price)
                .containsExactly(
                        new BigDecimal("75000.00"),
                        new BigDecimal("55000.00"),
                        new BigDecimal("300.00")
                );
    }



}

