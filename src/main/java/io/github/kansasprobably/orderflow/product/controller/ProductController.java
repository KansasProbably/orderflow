package io.github.kansasprobably.orderflow.product.controller;

import io.github.kansasprobably.orderflow.product.ProductService;
import io.github.kansasprobably.orderflow.product.ProductStatus;
import io.github.kansasprobably.orderflow.product.dto.CreateProductRequest;
import io.github.kansasprobably.orderflow.product.dto.ProductResponse;
import io.github.kansasprobably.orderflow.product.dto.UpdateProductStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse productResponse = productService.createProduct(request);

        URI location = URI.create(
                "/api/v1/products/" + productResponse.id()
        );

        return ResponseEntity
                .created(location)
                .body(productResponse);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping
    public Page<ProductResponse> getProducts(
            @RequestParam(required = false)
            ProductStatus status,

            @RequestParam(required = false)
            String name,

            Pageable pageable) {

        return productService.getProducts(status, name, pageable);
    }

    @PatchMapping("/{id}/status")
    public ProductResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductStatusRequest request
            ) {
        return productService.updateStatus(id, request);
    }
}
