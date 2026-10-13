package io.github.kansasprobably.orderflow.stock.controller;

import io.github.kansasprobably.orderflow.stock.StockService;
import io.github.kansasprobably.orderflow.stock.dto.CreateStockRequest;
import io.github.kansasprobably.orderflow.stock.dto.IncreaseStockRequest;
import io.github.kansasprobably.orderflow.stock.dto.StockResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static org.springframework.http.client.observation.ClientHttpObservationDocumentation.LowCardinalityKeyNames.URI;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public ResponseEntity<StockResponse> createStock(
            @Valid @RequestBody  CreateStockRequest request) {
        StockResponse response = stockService.createStock(request);

        URI location = java.net.URI.create("/api/v1/stocks/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/{id}")
    public StockResponse getStockById(@PathVariable UUID id) {
        return stockService.getStockById(id);
    }

    @GetMapping
    public StockResponse getStockByProductIdAndWarehouseId(
            @RequestParam UUID productId,
            @RequestParam UUID warehouseId
    ) {
        return stockService.getStockByProductIdAndWarehouseId(productId, warehouseId);
    }

    @PostMapping("/{id}/increase")
    public StockResponse increaseQuantity(
            @PathVariable UUID id,
            @Valid @RequestBody IncreaseStockRequest request
    ) {
        return stockService.increaseAvailable(id, request);
    }
}
