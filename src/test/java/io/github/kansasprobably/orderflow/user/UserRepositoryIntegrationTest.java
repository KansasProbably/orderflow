package io.github.kansasprobably.orderflow.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectDuplicateEmailAtDatabaseLevel() {
        User firstUser = new User(
                "admin@example.com",
                "password",
                UserRole.ADMIN
        );

        User secondUser = new User(
                "admin@example.com",
                "password",
                UserRole.ADMIN
        );

        userRepository.saveAndFlush(firstUser);

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(secondUser)
        )
                .isInstanceOf(DataIntegrityViolationException.class);

    }
}
