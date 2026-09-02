package io.github.kansasprobably.orderflow.customer.mapper;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.customer.dto.CreateCustomerRequest;
import io.github.kansasprobably.orderflow.customer.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public Customer toEntity(CreateCustomerRequest createCustomerRequest) {
        return new Customer(createCustomerRequest.name(), createCustomerRequest.email().toLowerCase(), createCustomerRequest.phone());
    }

    public CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail(),
                customer.getPhone(), customer.getStatus(),customer.getCreatedAt(),customer.getUpdatedAt());
    }
}
