package io.github.kansasprobably.orderflow.user.controller;

import io.github.kansasprobably.orderflow.user.UserService;
import io.github.kansasprobably.orderflow.user.dto.UserCreateRequest;
import io.github.kansasprobably.orderflow.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse userResponse = userService.createUser(request);

        URI location = URI.create(
                "/api/v1/users/" + userResponse.id()
        );

        return ResponseEntity
                .created(location)
                .body(userResponse);
    }
}
