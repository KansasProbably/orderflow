package io.github.kansasprobably.orderflow.product.specification;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductSpecifications {

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<Product> nameContains(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
    }

}
