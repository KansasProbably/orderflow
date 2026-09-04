package io.github.kansasprobably.orderflow.customer.service;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.customer.CustomerRepository;
import io.github.kansasprobably.orderflow.customer.CustomerService;
import io.github.kansasprobably.orderflow.customer.dto.CreateCustomerRequest;
import io.github.kansasprobably.orderflow.customer.dto.CustomerResponse;
import io.github.kansasprobably.orderflow.customer.exception.CustomerEmailAlreadyExistsException;
import io.github.kansasprobably.orderflow.customer.exception.CustomerNotFoundException;
import io.github.kansasprobably.orderflow.customer.mapper.CustomerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "ООО Ромашка", "test@example.com",null);

        Customer customer = new Customer(request.name(), request.email(), request.phone());

        Customer savedCustomer = new Customer(request.name(), request.email(), request.phone());

        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-01-02T11:00:00Z");

        CustomerResponse expectedResponse = new CustomerResponse(
                savedCustomer.getId(),savedCustomer.getName(),savedCustomer.getEmail(), savedCustomer.getPhone(), savedCustomer.getStatus(),
                createdAt, updatedAt);

        when(customerRepository.existsByEmailIgnoreCase(request.email()))
                .thenReturn(false);

        when(customerMapper.toEntity(request))
                .thenReturn(customer);

        when(customerRepository.save(customer))
                .thenReturn(savedCustomer);

        when(customerMapper.toCustomerResponse(savedCustomer))
                .thenReturn(expectedResponse);

        CustomerResponse actualResponse = customerService.createCustomer(request);

        assertEquals(expectedResponse,actualResponse);

        verify(customerRepository).save(customer);

    }

    @Test
    void shouldThrowEmailAlreadyExists() {

        CreateCustomerRequest request = new CreateCustomerRequest(
                "ООО Ромашка", "test@example.com",null);


        when(customerRepository.existsByEmailIgnoreCase(request.email()))
                .thenReturn(true);

        assertThrows(CustomerEmailAlreadyExistsException.class,
                () -> customerService.createCustomer(request));

        verify(customerRepository,never())
                .save(any());


    }

    @Test
    void shouldReturnCustomerById() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-01-02T11:00:00Z");
        Customer customer = new Customer("ООО Ромашка", "test@example.com",null);
        CustomerResponse expectedCustomerResponse = new CustomerResponse(id,customer.getName(),customer.getEmail(),
                customer.getPhone(), customer.getStatus(),createdAt,updatedAt);
        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        when(customerMapper.toCustomerResponse(customer))
                .thenReturn(expectedCustomerResponse);

        CustomerResponse actualCustomerResponse = customerService.getCustomerById(id);

        assertEquals(expectedCustomerResponse,actualCustomerResponse);

    }

    @Test
    void shouldThrowCustomerNotFound() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomerById(id));

        verify(customerMapper,never())
                .toCustomerResponse(any());

    }
}
