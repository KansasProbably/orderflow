package io.github.kansasprobably.orderflow.user;

import io.github.kansasprobably.orderflow.user.dto.UserCreateRequest;
import io.github.kansasprobably.orderflow.user.dto.UserResponse;
import io.github.kansasprobably.orderflow.user.exception.UserAlreadyExistsException;
import io.github.kansasprobably.orderflow.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                email,
                passwordHash,
                request.role()
        );

        User savedUser = userRepository.saveAndFlush(user);

        return userMapper.toResponse(savedUser);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
