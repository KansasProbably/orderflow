package io.github.kansasprobably.orderflow.warehouse.controller;

import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import io.github.kansasprobably.orderflow.warehouse.WarehouseService;
import io.github.kansasprobably.orderflow.warehouse.dto.CreateWarehouseRequest;
import io.github.kansasprobably.orderflow.warehouse.dto.WarehouseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request
    ){
        WarehouseResponse response = warehouseService.createWarehouse(request);
        URI location = URI.create(
                "api/v1/warehouses/" + response.id()
        );
        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping
    public WarehouseResponse getWarehouseById(@PathVariable UUID id) {
        return warehouseService.getWarehouseById(id);
    }

    @GetMapping
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }
}
