package io.github.kansasprobably.orderflow.customer;

import io.github.kansasprobably.orderflow.customer.dto.CreateCustomerRequest;
import io.github.kansasprobably.orderflow.customer.dto.CustomerResponse;
import io.github.kansasprobably.orderflow.customer.exception.CustomerEmailAlreadyExistsException;
import io.github.kansasprobably.orderflow.customer.exception.CustomerNotFoundException;
import io.github.kansasprobably.orderflow.customer.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest createCustomerRequest) {
        if (customerRepository.existsByEmailIgnoreCase(createCustomerRequest.email())) {
            throw new CustomerEmailAlreadyExistsException(createCustomerRequest.email());
        }
        Customer customer = customerMapper.toEntity(createCustomerRequest);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toCustomerResponse(savedCustomer);
    }

    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
        return customerMapper.toCustomerResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(customerMapper :: toCustomerResponse)
                .toList();
    }

}
