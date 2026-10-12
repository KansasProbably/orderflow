package io.github.kansasprobably.orderflow.product.mapper;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.dto.CreateProductRequest;
import io.github.kansasprobably.orderflow.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public Product toEntity(CreateProductRequest request) {
        return new Product(
                request.sku(),
                request.name(),
                request.price()
        );
    }
}
