package io.github.kansasprobably.orderflow.order;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
@NullMarked
public interface OrderRepository
        extends JpaRepository<Order, UUID>,
                JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = "items")
    @Query("""
            select o
            from Order o
            where o.id = :id
            """)
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @EntityGraph(attributePaths = "items")
    @Override
    Page<Order> findAll(
            Specification<Order> specification,
            Pageable pageable);
}
