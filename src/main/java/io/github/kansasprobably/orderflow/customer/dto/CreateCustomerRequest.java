package io.github.kansasprobably.orderflow.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateCustomerRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must contain less than 255 characters")
        String name,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        @Size(max = 255, message = "Email must contain less than 255 characters")
        String email,

        @Size(max = 20, message = "Phone number must contain less than 20 characters")
        String phone
) {
}
