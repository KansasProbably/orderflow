package io.github.kansasprobably.orderflow.stock.mapper;

import io.github.kansasprobably.orderflow.stock.Stock;
import io.github.kansasprobably.orderflow.stock.dto.StockResponse;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public StockResponse toStockResponse(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getProduct().getId(),
                stock.getWarehouse().getId(),
                stock.getAvailableQuantity(),
                stock.getReservedQuantity()
        );
    }
}
