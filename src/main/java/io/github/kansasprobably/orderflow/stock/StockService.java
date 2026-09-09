package io.github.kansasprobably.orderflow.stock;

import io.github.kansasprobably.orderflow.stock.exception.StockNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public Stock reserve(UUID productId, UUID warehouseId, Integer quantity) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(StockNotFoundException::new);

        stock.reserve(quantity);
        return stock;
    }
}
