package io.github.kansasprobably.orderflow.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByEmailIgnoreCase(String email);
}
