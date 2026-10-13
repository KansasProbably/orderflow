package io.github.kansasprobably.orderflow.warehouse;

import io.github.kansasprobably.orderflow.warehouse.controller.WarehouseController;
import io.github.kansasprobably.orderflow.warehouse.dto.CreateWarehouseRequest;
import io.github.kansasprobably.orderflow.warehouse.dto.WarehouseResponse;
import io.github.kansasprobably.orderflow.warehouse.exception.DuplicateWarehouseCodeException;
import io.github.kansasprobably.orderflow.warehouse.exception.WarehouseNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseController.class)
public class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WarehouseService warehouseService;

    @Test
    void shouldCreateWarehouse() throws Exception {
        UUID warehouseId = UUID.randomUUID();

        WarehouseResponse response = createResponse(warehouseId, "MSK-01", "Moscow warehouse");

        when(warehouseService.createWarehouse(any(CreateWarehouseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/warehouses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "code": "MSK-01",
                              "name": "Moscow warehouse"
                            }
                            """)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/warehouses/" + warehouseId
                ))
                .andExpect(jsonPath("$.id").value(warehouseId.toString()))
                .andExpect(jsonPath("$.code").value(response.code()))
                .andExpect(jsonPath("$.name").value(response.name()));

        verify(warehouseService)
                .createWarehouse(any(CreateWarehouseRequest.class));
    }

    @Test
    void shouldReturnWarehouse() throws Exception {
        UUID warehouseId = UUID.randomUUID();

        WarehouseResponse response = createResponse(warehouseId, "MSK-01", "Moscow warehouse");

        when(warehouseService.getWarehouseById(warehouseId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/warehouses/{id}", warehouseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(warehouseId.toString()))
                .andExpect(jsonPath("$.code").value(response.code()))
                .andExpect(jsonPath("$.name").value(response.name()));

        verify(warehouseService).getWarehouseById(warehouseId);

    }

    @Test
    void shouldReturnAllWarehouses() throws Exception {
        UUID firstWarehouseId = UUID.randomUUID();
        UUID secondWarehouseId = UUID.randomUUID();

        WarehouseResponse firstWarehouse = createResponse(firstWarehouseId,"MSK-01", "Moscow warehouse");
        WarehouseResponse secondWarehouse = createResponse(secondWarehouseId,"SPB-01", "Saint-Petersburg warehouse");


        when(warehouseService.getAllWarehouses())
                .thenReturn(List.of(firstWarehouse, secondWarehouse));

        mockMvc.perform(get("/api/v1/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstWarehouseId.toString()))
                .andExpect(jsonPath("$[1].id").value(secondWarehouseId.toString()));

        verify(warehouseService).getAllWarehouses();
    }

    @Test
    void shouldReturn404WhenWarehouseNotFound() throws Exception {
        UUID warehouseId = UUID.randomUUID();

        when(warehouseService.getWarehouseById(warehouseId))
                .thenThrow(new WarehouseNotFoundException(warehouseId));

        mockMvc.perform(get("/api/v1/warehouses/{id}", warehouseId)
                )
                .andExpect(status().isNotFound());

        verify(warehouseService).getWarehouseById(warehouseId);
    }

    @Test
    void shouldReturn400WhenRequestInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "code": "",
                              "name": ""
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(warehouseService);
    }

    @Test
    void shouldReturn409WhenCodeIsDuplicate() throws Exception {
        when(warehouseService.createWarehouse(any(CreateWarehouseRequest.class)))
                .thenThrow(new DuplicateWarehouseCodeException("MSK-01"));

        mockMvc.perform(post("/api/v1/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "code": "MSK-01",
                              "name": "Moscow warehouse"
                            }
                            """)
                )
                .andExpect(status().isConflict());

        verify(warehouseService).createWarehouse(any(CreateWarehouseRequest.class));

    }

    private WarehouseResponse createResponse(
            UUID id,
            String code,
            String name
    ) {
        return new WarehouseResponse(
                id,
                code,
                name
        );
    }
}
