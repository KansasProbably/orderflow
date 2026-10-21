package io.github.kansasprobably.orderflow.stock.controller;


import io.github.kansasprobably.orderflow.security.JwtAuthenticationFilter;
import io.github.kansasprobably.orderflow.stock.StockService;
import io.github.kansasprobably.orderflow.stock.dto.CreateStockRequest;
import io.github.kansasprobably.orderflow.stock.dto.IncreaseStockRequest;
import io.github.kansasprobably.orderflow.stock.dto.StockResponse;
import io.github.kansasprobably.orderflow.stock.exception.DuplicateStockException;
import io.github.kansasprobably.orderflow.stock.exception.StockNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        value = StockController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    void shouldCreateStock() throws Exception {
        UUID stockId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        StockResponse response = createResponse(stockId,productId,warehouseId,10,0);

        when(stockService.createStock(any(CreateStockRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/stocks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "productId": "%s",
                              "warehouseId": "%s",
                              "availableQuantity": 10
                            }
                            """.formatted(productId,warehouseId))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/stocks/" + stockId
                ))
                .andExpect(jsonPath("$.id").value(stockId.toString()))
                .andExpect(jsonPath("$.productId").value(response.productId().toString()))
                .andExpect(jsonPath("$.warehouseId").value(response.warehouseId().toString()))
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.reservedQuantity").value(0));

        verify(stockService)
                .createStock(any(CreateStockRequest.class));
    }

    @Test
    void shouldReturnStock() throws Exception {
        UUID stockId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        StockResponse response = createResponse(stockId,productId,warehouseId,10,0);

        when(stockService.getStockById(stockId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/stocks/{id}",stockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stockId.toString()))
                .andExpect(jsonPath("$.productId").value(response.productId().toString()))
                .andExpect(jsonPath("$.warehouseId").value(response.warehouseId().toString()))
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.reservedQuantity").value(0));

        verify(stockService).getStockById(stockId);
    }

    @Test
    void shouldReturnStockByProductIdAndWarehouseId() throws Exception {
        UUID stockId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        StockResponse response = createResponse(stockId,productId,warehouseId,10,0);

        when(stockService.getStockByProductIdAndWarehouseId(productId,warehouseId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/stocks")
                        .param("productId", productId.toString())
                        .param("warehouseId", warehouseId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stockId.toString()))
                .andExpect(jsonPath("$.productId").value(response.productId().toString()))
                .andExpect(jsonPath("$.warehouseId").value(response.warehouseId().toString()))
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.reservedQuantity").value(0));

        verify(stockService).getStockByProductIdAndWarehouseId(productId,warehouseId);
    }

    @Test
    void shouldReturn404WhenStockNotFound() throws Exception {
        UUID stockId = UUID.randomUUID();

        when(stockService.getStockById(stockId))
                .thenThrow(new StockNotFoundException(stockId));

        mockMvc.perform(get("/api/v1/stocks/{id}", stockId)
                )
                .andExpect(status().isNotFound());

        verify(stockService).getStockById(stockId);
    }

    @Test
    void shouldReturn409WhenStockIsDuplicate() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        when(stockService.createStock(any(CreateStockRequest.class)))
                .thenThrow(new DuplicateStockException(productId,warehouseId));

        mockMvc.perform(
                        post("/api/v1/stocks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "productId": "%s",
                              "warehouseId": "%s",
                              "availableQuantity": 10
                            }
                            """.formatted(productId,warehouseId))
                )
                .andExpect(status().isConflict()
        );

        verify(stockService).createStock(any(CreateStockRequest.class));
    }

    @Test
    void shouldIncreaseStock() throws Exception {
        UUID stockId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        StockResponse response = createResponse(stockId,productId,warehouseId,20,0);

        IncreaseStockRequest request = new IncreaseStockRequest(10);

        when(stockService.increaseAvailable(stockId,request))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/stocks/{id}/increase", stockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "quantity": 10
                                }
                                """)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stockId.toString()))
                .andExpect(jsonPath("$.availableQuantity").value(20))
                .andExpect(jsonPath("$.reservedQuantity").value(0));

        verify(stockService).increaseAvailable(stockId, request);
    }

    @Test
    void shouldReturn400WhenIncreaseQuantityIsNotPositive() throws Exception {
        UUID stockId = UUID.randomUUID();


        mockMvc.perform(
                        post("/api/v1/stocks/{id}/increase", stockId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "quantity": -10
                                }
                                """)
                )
                .andExpect(status().isBadRequest());
        verifyNoInteractions(stockService);
    }

    private StockResponse createResponse(
            UUID id,
            UUID productId,
            UUID warehouseId,
            Integer availableQuantity,
            Integer reservedQuantity
    ) {
        return new StockResponse(
                id,
                productId,
                warehouseId,
                availableQuantity,
                reservedQuantity
        );
    }
}
