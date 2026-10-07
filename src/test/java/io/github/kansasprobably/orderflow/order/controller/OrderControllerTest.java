package io.github.kansasprobably.orderflow.order.controller;

import io.github.kansasprobably.orderflow.order.OrderService;
import io.github.kansasprobably.orderflow.order.OrderStatus;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderRequest;
import io.github.kansasprobably.orderflow.order.dto.OrderResponse;
import io.github.kansasprobably.orderflow.order.exception.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-01-02T11:00:00Z");
        OrderResponse orderResponse= new OrderResponse(
                orderId,
                customerId,
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
                              "customerId": "%s",
                              "items": [
                                               {
                                                 "productId": "%s",
                                                 "warehouseId": "%s",
                                                 "quantity": 1
                                               }
                                             ]
                            }
                            """.formatted(
                                    customerId,
                                    productId,
                                    warehouseId
                        ))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/orders/" + orderId
                ))
                .andExpect(jsonPath("$.id").value(orderId.toString()));

    }

    @Test
    void shouldReturn400WhenRequestInvalid() throws Exception {
        mockMvc.perform(
                post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "customerId": null,
                                "items": []
                                }
                                """)
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.getOrderById(id))
                .thenThrow(new OrderNotFoundException(id));

        mockMvc.perform(
                get("/api/v1/orders/{id}", id)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-01-02T11:00:00Z");

        OrderResponse orderResponse = new OrderResponse(
                orderId,
                customerId,
                OrderStatus.NEW,
                List.of(),
                createdAt,
                updatedAt
        );

        when(orderService.getOrderById(orderId))
                .thenReturn(orderResponse);

        mockMvc.perform(
                get("/api/v1/orders/{id}", orderId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.orderStatus").value("NEW"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());

        verify(orderService).getOrderById(orderId);
    }

    @Test
    void shouldReturn400WhenOrderItemQuantityIsInvalid() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        mockMvc.perform(
                post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerId": "%s",
                              "items": [
                                               {
                                                 "productId": "%s",
                                                 "warehouseId": "%s",
                                                 "quantity": 0
                                               }
                                             ]
                            }
                            """.formatted(
                                    customerId,
                                    productId,
                                    warehouseId
                        ))
        )
                .andExpect(status().isBadRequest());
        verifyNoInteractions(orderService);
    }

    @Test
    void shouldReturn400WhenRequestContainsUnknownField() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        mockMvc.perform(
                post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "customerId": "%s",
                              "items": [
                                               {
                                                 "productId": "%s",
                                                 "warehouseId": "%s",
                                                 "quantity": 0
                                               }
                                             ],
                              "unknownField": "value"
                            }
                            """.formatted(
                                customerId,
                                productId,
                                warehouseId
                        ))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Unknown field: unknownField"));

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldConfirmOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-01-02T11:00:00Z");

        OrderResponse orderResponse = new OrderResponse(
                orderId,
                customerId,
                OrderStatus.CONFIRMED,
                List.of(),
                createdAt,
                updatedAt
        );

        when(orderService.confirmOrder(orderId))
                .thenReturn(orderResponse);

        mockMvc.perform(
                post("/api/v1/orders/{id}/confirm", orderId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(orderId.toString()))
                .andExpect(jsonPath("$.customerId")
                    .value(customerId.toString()))
                .andExpect(jsonPath("$.orderStatus")
                        .value("CONFIRMED"));


        verify(orderService).confirmOrder(orderId);
    }

    @Test
    void shouldCancelOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-01-02T11:00:00Z");

        OrderResponse orderResponse = new OrderResponse(
                orderId,
                customerId,
                OrderStatus.CANCELLED,
                List.of(),
                createdAt,
                updatedAt
        );

        when(orderService.cancelOrder(orderId))
                .thenReturn(orderResponse);

        mockMvc.perform(
                        post("/api/v1/orders/{id}/cancel", orderId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(orderId.toString()))
                .andExpect(jsonPath("$.customerId")
                        .value(customerId.toString()))
                .andExpect(jsonPath("$.orderStatus")
                        .value("CANCELLED"));

        verify(orderService).cancelOrder(orderId);
    }
}
