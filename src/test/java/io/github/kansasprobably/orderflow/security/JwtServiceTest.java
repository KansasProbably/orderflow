package io.github.kansasprobably.orderflow.security;

import io.github.kansasprobably.orderflow.user.User;
import io.github.kansasprobably.orderflow.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[32];
        Arrays.fill(keyBytes,(byte) 1);

        String secret = Base64.getEncoder()
                .encodeToString(keyBytes);

        JwtProperties jwtProperties = new JwtProperties(
                secret,
                Duration.ofHours(1)
        );

        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        User user = createUser();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(token).isNotBlank();
        assertThat(username).isEqualTo(user.getEmail());
    }

    @Test
    void shouldValidateToken() {
        User user = createUser();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        User user = createUser();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        byte[] anotherKeyBytes = new byte[32];
        Arrays.fill(anotherKeyBytes,(byte) 2);

        String anotherSecret = Base64.getEncoder().encodeToString(anotherKeyBytes);

        JwtProperties anotherJwtProperties = new JwtProperties(
                anotherSecret,
                Duration.ofHours(1)
        );

        JwtService anotherJwtService = new JwtService(anotherJwtProperties);

        boolean valid = anotherJwtService.isTokenValid(token);

        assertThat(valid).isFalse();

    }

    @Test
    void shouldRejectExpiredToken() {
        User user = createUser();

        byte[] keyBytes = new byte[32];
        Arrays.fill(keyBytes,(byte) 1);

        String secret = Base64.getEncoder()
                .encodeToString(keyBytes);

        JwtProperties jwtProperties = new JwtProperties(
                secret,
                Duration.ofSeconds(-1)
        );

        JwtService jwtService = new JwtService(jwtProperties);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isFalse();
    }

    private User createUser() {
        return new User(
                "admin@example.ru",
                "password",
                UserRole.ADMIN
        );
    }
}
