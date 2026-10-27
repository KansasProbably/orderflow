package io.github.kansasprobably.orderflow.product;

import io.github.kansasprobably.orderflow.config.cache.RedisCacheConfig;
import io.github.kansasprobably.orderflow.product.dto.CreateProductRequest;
import io.github.kansasprobably.orderflow.product.dto.ProductResponse;
import io.github.kansasprobably.orderflow.product.dto.UpdateProductStatusRequest;
import io.github.kansasprobably.orderflow.product.exception.DuplicateProductSkuException;
import io.github.kansasprobably.orderflow.product.exception.ProductNotFoundException;
import io.github.kansasprobably.orderflow.product.mapper.ProductMapper;
import io.github.kansasprobably.orderflow.product.specification.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateProductSkuException(request.sku());
        }

        Product product = productMapper.toEntity(request);

        Product savedProduct = productRepository.save(product);

        return productMapper.toProductResponse(savedProduct);
    }

    @Cacheable(
            cacheNames = RedisCacheConfig.PRODUCTS_CACHE,
            key = "#id"
    )
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productMapper.toProductResponse(product);
    }

    public Page<ProductResponse> getProducts(
            ProductStatus status,
            String name,
            Pageable pageable
    ) {
        Specification<Product> specification =
                Specification.unrestricted();

        if (status != null) {
            specification = specification.and(ProductSpecifications.hasStatus(status));
        }

        if (name != null && !name.isBlank()) {
            specification = specification.and(ProductSpecifications.nameContains(name));
        }

        return productRepository.findAll(specification, pageable)
                .map(productMapper::toProductResponse);
    }

    @Transactional
    @CacheEvict(
            cacheNames = RedisCacheConfig.PRODUCTS_CACHE,
            key = "#id"
    )
    public ProductResponse updateStatus(UUID id, UpdateProductStatusRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.changeStatus(request.status());

        return productMapper.toProductResponse(product);
    }
}
