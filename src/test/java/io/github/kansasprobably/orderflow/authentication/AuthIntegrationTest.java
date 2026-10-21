package io.github.kansasprobably.orderflow.authentication;

import com.jayway.jsonpath.JsonPath;
import io.github.kansasprobably.orderflow.user.User;
import io.github.kansasprobably.orderflow.user.UserRepository;
import io.github.kansasprobably.orderflow.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLoginAndReturnJwt() throws Exception {
        User user = new User(
                "admin@example.com",
                passwordEncoder.encode("password"),
                UserRole.ADMIN
        );

        userRepository.saveAndFlush(user);

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "admin@example.com",
                                    "password": "password"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void shouldReturn401ForWrongPassword() throws Exception {
        User user = new User(
                "admin@example.com",
                passwordEncoder.encode("password"),
                UserRole.ADMIN
        );

        userRepository.saveAndFlush(user);

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "email": "admin@example.com",
                                    "password": "wrong-password"
                                }
                                """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldAccessProtectedEndpointWithRealJwt() throws Exception {
        User user = new User(
                "admin@example.com",
                passwordEncoder.encode("password"),
                UserRole.ADMIN
        );

        userRepository.saveAndFlush(user);

        MvcResult response = mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "admin@example.com",
                                    "password": "password"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = response
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(
                responseBody,
                "$.accessToken"
        );

        mockMvc.perform(
                get("/api/v1/products")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
        )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403ForManagerUsingRealJwtOnAdminEndpoint() throws Exception {
        User user = new User(
                "manager@example.com",
                passwordEncoder.encode("password"),
                UserRole.MANAGER
        );

        userRepository.saveAndFlush(user);

        MvcResult response = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "email": "manager@example.com",
                                    "password": "password"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = response
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(
                responseBody,
                "$.accessToken"
        );

        mockMvc.perform(
                        post("/api/v1/products")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn401WhenUserWasDeletedAfterTokenWasIssued() throws Exception {
        User user = new User(
                "manager@example.com",
                passwordEncoder.encode("password"),
                UserRole.MANAGER
        );

        userRepository.saveAndFlush(user);

        MvcResult response = mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "manager@example.com",
                                    "password": "password"
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = response
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(
                responseBody,
                "$.accessToken"
        );

        userRepository.deleteAll();
        userRepository.flush();

        mockMvc.perform(
                get("/api/v1/products")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        )
        )
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());

    }

}
