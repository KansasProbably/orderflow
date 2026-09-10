package io.github.kansasprobably.orderflow.order.controller;

import io.github.kansasprobably.orderflow.order.OrderService;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderRequest;
import io.github.kansasprobably.orderflow.order.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderResponse);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            @PathVariable UUID id
    ) {
        return orderService.getOrderById(id);
    }
}
