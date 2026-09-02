package io.github.kansasprobably.orderflow.customer.controller;

import io.github.kansasprobably.orderflow.customer.dto.CreateCustomerRequest;
import io.github.kansasprobably.orderflow.customer.dto.CustomerResponse;
import io.github.kansasprobably.orderflow.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest createCustomerRequest
            ) {
        CustomerResponse customerResponse =  customerService.createCustomer(createCustomerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerResponse);
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(
            @PathVariable UUID id
            ) {
        return customerService.getCustomerById(id);
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }
}
