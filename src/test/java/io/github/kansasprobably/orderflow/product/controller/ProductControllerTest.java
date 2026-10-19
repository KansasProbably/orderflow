package io.github.kansasprobably.orderflow.product.controller;

import io.github.kansasprobably.orderflow.product.ProductService;
import io.github.kansasprobably.orderflow.product.ProductStatus;
import io.github.kansasprobably.orderflow.product.dto.CreateProductRequest;
import io.github.kansasprobably.orderflow.product.dto.ProductResponse;
import io.github.kansasprobably.orderflow.product.dto.UpdateProductStatusRequest;
import io.github.kansasprobably.orderflow.product.exception.DuplicateProductSkuException;
import io.github.kansasprobably.orderflow.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {
        UUID productId = UUID.randomUUID();

        ProductResponse response = createResponse(productId,ProductStatus.ACTIVE);

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "sku": "SKU-001",
                              "name": "iPhone",
                              "price": 280000.00
                            }
                            """)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/products/" + productId
                ))
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.sku").value(response.sku()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.price").value(response.price().doubleValue()))
                .andExpect(jsonPath("$.status").value(response.status().name()));

        verify(productService)
                .createProduct(any(CreateProductRequest.class));
    }

    @Test
    void shouldReturnProduct() throws Exception {
        UUID productId = UUID.randomUUID();

        ProductResponse response = createResponse(productId,ProductStatus.ACTIVE);

        when(productService.getProductById(productId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.sku").value(response.sku()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.price").value(response.price().doubleValue()))
                .andExpect(jsonPath("$.status").value(response.status().name()));

        verify(productService).getProductById(productId);

    }

    @Test
    void shouldGetProductsWithDefaultPagination() throws Exception {
        UUID firstProductId = UUID.randomUUID();
        UUID secondProductId = UUID.randomUUID();

        ProductResponse firstProduct = createResponse(firstProductId,ProductStatus.ACTIVE);

        ProductResponse secondProduct = new ProductResponse(
                secondProductId,
                "SKU-002",
                "Poco",
                new BigDecimal("28000.00"),
                ProductStatus.ACTIVE,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-02T11:00:00Z")
        );

        Page<ProductResponse> page =
                new PageImpl<>(
                        List.of(firstProduct,secondProduct),
                        PageRequest.of(0,20),
                        2
                );

        when(productService.getProducts(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);


        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(firstProductId.toString()))
                .andExpect(jsonPath("$.content[1].id").value(secondProductId.toString()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(productService).getProducts(isNull(),isNull(),any(Pageable.class));
    }

    @Test
    void shouldAcceptPaginationAndSortingParameters() throws Exception {
        Page<ProductResponse> page = Page.empty(
                PageRequest.of(
                        2,
                        10,
                        Sort.by("price").descending()
                )
        );

        when(productService.getProducts(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                    .param("page","2")
                    .param("size", "10")
                    .param("sort", "price,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(productService).getProducts(
                isNull(),
                isNull(),
                captor.capture()
        );

        Pageable pageable = captor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);

        Sort.Order priceOrder =
                pageable.getSort().getOrderFor("price");

        assertThat(priceOrder).isNotNull();
        assertThat(priceOrder.getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void shouldAcceptProductFilters() throws Exception {
        Page<ProductResponse> page = Page.empty();

        when(productService.getProducts(
                eq(ProductStatus.ACTIVE),
                eq("iphone"),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                    .param("status", "ACTIVE")
                    .param("name","iphone"))
                .andExpect(status().isOk());

        verify(productService).getProducts(
                eq(ProductStatus.ACTIVE),
                eq("iphone"),
                any(Pageable.class)
        );
    }

    @Test
    void shouldLimitPageSize() throws Exception {
        Page<ProductResponse> page = Page.empty();

        when(productService.getProducts(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                        .param("size", "1000"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(productService).getProducts(
                isNull(),
                isNull(),
                captor.capture()
        );

        Pageable pageable = captor.getValue();

        assertThat(pageable.getPageSize()).isEqualTo(100);
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        UUID productId = UUID.randomUUID();

        when(productService.getProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(get("/api/v1/products/{id}", productId)
        )
                .andExpect(status().isNotFound());

        verify(productService).getProductById(productId);
    }

    @Test
    void shouldReturn400WhenRequestInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "sku": "",
                              "name": "",
                              "price": -1
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void shouldReturn409WhenSkuIsDuplicate() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new DuplicateProductSkuException("SKU-001"));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                              "sku": "SKU-001",
                              "name": "iPhone",
                              "price": 280000.00
                            }
                            """)
                )
                .andExpect(status().isConflict());

        verify(productService).createProduct(any(CreateProductRequest.class));

    }

    @Test
    void shouldChangeStatus() throws Exception {
        UUID productId = UUID.randomUUID();

        ProductResponse response = createResponse(productId, ProductStatus.INACTIVE);

        UpdateProductStatusRequest request = new UpdateProductStatusRequest(ProductStatus.INACTIVE);

        when(productService.updateStatus(productId, request))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/products/{id}/status", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "status": "INACTIVE"
                        }
                        """)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(productService).updateStatus(productId,request);



    }

    private ProductResponse createResponse(
            UUID productId,
            ProductStatus status
    ) {
        return new ProductResponse(
                productId,
                "SKU-001",
                "iPhone",
                new BigDecimal("280000.00"),
                status,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"),
                OffsetDateTime.parse("2026-01-02T11:00:00Z")
        );
    }
}
