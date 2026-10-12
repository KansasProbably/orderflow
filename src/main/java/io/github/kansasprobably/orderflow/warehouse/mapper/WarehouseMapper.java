package io.github.kansasprobably.orderflow.warehouse.mapper;

import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import io.github.kansasprobably.orderflow.warehouse.dto.CreateWarehouseRequest;
import io.github.kansasprobably.orderflow.warehouse.dto.WarehouseResponse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {

    public WarehouseResponse toWarehouseResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName()
        );
    }

    public Warehouse toEntity(CreateWarehouseRequest request) {
        return new Warehouse(
                request.code(),
                request.name()
        );
    }
}
