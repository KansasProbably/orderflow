package io.github.kansasprobably.orderflow.warehouse;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.dto.ProductResponse;
import io.github.kansasprobably.orderflow.product.exception.DuplicateProductSkuException;
import io.github.kansasprobably.orderflow.product.exception.ProductNotFoundException;
import io.github.kansasprobably.orderflow.warehouse.dto.CreateWarehouseRequest;
import io.github.kansasprobably.orderflow.warehouse.dto.WarehouseResponse;
import io.github.kansasprobably.orderflow.warehouse.exception.DuplicateWarehouseCodeException;
import io.github.kansasprobably.orderflow.warehouse.exception.WarehouseNotFoundException;
import io.github.kansasprobably.orderflow.warehouse.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    private final WarehouseMapper warehouseMapper;

    @Transactional
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.code())) {
            throw new DuplicateWarehouseCodeException(request.code());
        }

        Warehouse warehouse = warehouseMapper.toEntity(request);

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        return warehouseMapper.toWarehouseResponse(savedWarehouse);
    }

    public WarehouseResponse getWarehouseById(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));

        return warehouseMapper.toWarehouseResponse(warehouse);
    }

    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream()
                .map(warehouseMapper::toWarehouseResponse)
                .toList();
    }

}
