package io.github.kansasprobably.orderflow.order.controller;

import io.github.kansasprobably.orderflow.order.OrderService;
import io.github.kansasprobably.orderflow.order.OrderStatus;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderRequest;
import io.github.kansasprobably.orderflow.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-01-02T11:00:00Z");
        OrderResponse orderResponse= new OrderResponse(
                orderId,
                UUID.randomUUID(),
                OrderStatus.NEW,
                List.of(),
                createdAt,
                updatedAt
        );

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(orderResponse);

        mockMvc.perform(
                post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerId": "11111111-1111-1111-1111-111111111111",
                              "items": [
                                               {
                                                 "productId": "22222222-2222-2222-2222-222222222222",
                                                 "warehouseId": "22222222-2222-2222-2222-222222222222",
                                                 "quantity": 1
                                               }
                                             ]
                            }
                            """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()));

    }
}
