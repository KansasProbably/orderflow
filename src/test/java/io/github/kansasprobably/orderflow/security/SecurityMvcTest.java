package io.github.kansasprobably.orderflow.security;

import io.github.kansasprobably.orderflow.product.ProductService;
import io.github.kansasprobably.orderflow.product.ProductStatus;
import io.github.kansasprobably.orderflow.product.controller.ProductController;
import io.github.kansasprobably.orderflow.product.dto.CreateProductRequest;
import io.github.kansasprobably.orderflow.product.dto.ProductResponse;
import io.github.kansasprobably.orderflow.user.UserRole;
import io.github.kansasprobably.orderflow.user.UserService;
import io.github.kansasprobably.orderflow.user.controller.UserController;
import io.github.kansasprobably.orderflow.user.dto.UserCreateRequest;
import io.github.kansasprobably.orderflow.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
            ProductController.class,
            UserController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
public class SecurityMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        String token = "invalid-token";

        when(jwtService.isTokenValid(token))
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/products")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
        )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(jwtService).isTokenValid(token);
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(productService);

    }

    @Test
    void shouldReturn403WhenManagerAccessesAdminEndpoint() throws Exception {
        mockMvc.perform(
                        post("/api/v1/products")
                                .with(user("manager@example.com")
                                        .roles("MANAGER"))
                )
                .andExpect(status().isForbidden())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(productService);
    }

    @Test
    void shouldAllowAdminToAccessAdminEndpoint() throws Exception {
        String requestBody = """
                {
                    "sku": "ORD-01",
                    "name": "iphone",
                    "price": "100000.00"
                }
                """;

        ProductResponse response = new ProductResponse(
                UUID.randomUUID(),
                "ORD-01",
                "iphone",
                new BigDecimal("100000.00"),
                ProductStatus.ACTIVE,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/products")
                        .with(user("admin@example.com")
                                .roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION));

        verify(productService)
                .createProduct(any(CreateProductRequest.class));
    }

    @Test
    void shouldAllowManagerToAccessManagerEndpoint() throws Exception {

        when(productService.getProducts(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(
                get("/api/v1/products")
                        .with(user("manager@example.com")
                                .roles("MANAGER"))
        )
                .andExpect(status().isOk());

        verify(productService)
                .getProducts(
                        isNull(),
                        isNull(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldReturn403WhenManagerCreatesUser() throws Exception {
        mockMvc.perform(
                post("/api/v1/users")
                        .with(user("manager@axemple.com")
                                .roles("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "email": "admin2@example.com",
                                      "password": "password",
                                      "role": "ADMIN"
                                    }
                                    """)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToCreatesUser() throws Exception {
        UUID id = UUID.randomUUID();

        UserResponse response = new UserResponse(
                id,
                "admin2@example.com",
                UserRole.ADMIN,
                OffsetDateTime.now()
        );

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/users")
                                .with(user("admin@example.com")
                                        .roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "admin2@example.com",
                                      "password": "password",
                                      "role": "ADMIN"
                                    }
                                    """)
                )
                .andExpect(status().isCreated());

        verify(userService)
                .createUser(any(UserCreateRequest.class));
    }
}
