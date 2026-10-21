package io.github.kansasprobably.orderflow.user.dto;

import io.github.kansasprobably.orderflow.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank
        @Email
        String email,

        @NotNull
        @Size(min = 8, max = 100)
        String password,

        @NotNull
        UserRole role

) {
}
