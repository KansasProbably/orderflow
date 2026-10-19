package io.github.kansasprobably.orderflow.order.controller;

import io.github.kansasprobably.orderflow.order.OrderService;
import io.github.kansasprobably.orderflow.order.OrderStatus;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderRequest;
import io.github.kansasprobably.orderflow.order.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest
    ) {
        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);

        URI location = URI.create(
                "/api/v1/orders/" + orderResponse.id()
        );

        return ResponseEntity
                .created(location)
                .body(orderResponse);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            @PathVariable UUID id
    ) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public Page<OrderResponse> getOrders(
            @RequestParam(required = false)
            OrderStatus status,

            @RequestParam(required = false)
            UUID customerId,

            @RequestParam(required = false)
            OffsetDateTime createdFrom,

            @RequestParam(required = false)
            OffsetDateTime createdTo,

            Pageable pageable
    ) {
        return orderService.getOrders(status, customerId, createdFrom, createdTo, pageable);
    }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirmOrder(
            @PathVariable UUID id
    ) {
        return orderService.confirmOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable UUID id
    ) {
        return orderService.cancelOrder(id);
    }
}
