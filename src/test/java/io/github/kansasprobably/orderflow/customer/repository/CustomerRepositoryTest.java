package io.github.kansasprobably.orderflow.customer.repository;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.customer.CustomerRepository;
import io.github.kansasprobably.orderflow.customer.CustomerStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
@Testcontainers
class CustomerRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveCustomer() {
        Customer customer = new Customer("ООО Ромашка", "test@example.com",null);

        Customer savedCustomer = customerRepository.saveAndFlush(customer);


        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(savedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(savedCustomer.getPhone()).isNull();
        assertThat(savedCustomer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(savedCustomer.getCreatedAt()).isNotNull();
        assertThat(savedCustomer.getUpdatedAt()).isNotNull();

    }

    @Test
    void shouldFindCustomerById() {
        Customer customer = new Customer("ООО Ромашка", "test@example.com",null);

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        entityManager.clear();

        Optional<Customer> result = customerRepository.findById(savedCustomer.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(customer.getName());
        assertThat(result.get().getEmail()).isEqualTo(customer.getEmail());
        assertThat(result.get().getPhone()).isNull();
        assertThat(result.get().getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldReturnTrueWhenEmailAlreadyExists() {
        Customer customer = new Customer("ООО Ромашка", "test@example.com",null);

        customerRepository.saveAndFlush(customer);

        boolean exists = customerRepository.existsByEmailIgnoreCase("TeSt@example.com");

        assertThat(exists).isTrue();
    }
}
