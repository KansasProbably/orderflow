package io.github.kansasprobably.orderflow.stock;

import io.github.kansasprobably.orderflow.product.Product;
import io.github.kansasprobably.orderflow.product.ProductRepository;
import io.github.kansasprobably.orderflow.product.exception.ProductNotFoundException;
import io.github.kansasprobably.orderflow.stock.dto.CreateStockRequest;
import io.github.kansasprobably.orderflow.stock.dto.StockResponse;
import io.github.kansasprobably.orderflow.stock.exception.DuplicateStockException;
import io.github.kansasprobably.orderflow.stock.exception.StockNotFoundException;
import io.github.kansasprobably.orderflow.stock.mapper.StockMapper;
import io.github.kansasprobably.orderflow.warehouse.Warehouse;
import io.github.kansasprobably.orderflow.warehouse.WarehouseRepository;
import io.github.kansasprobably.orderflow.warehouse.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockMapper stockMapper;

    @Transactional
    public Stock reserve(UUID productId, UUID warehouseId, Integer quantity) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new StockNotFoundException(productId, warehouseId));

        stock.reserve(quantity);
        return stock;
    }

    @Transactional
    public Stock release(UUID productId, UUID warehouseId, Integer quantity) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId,warehouseId)
                .orElseThrow(() -> new StockNotFoundException(productId, warehouseId));
        stock.release(quantity);
        return stock;
    }

    @Transactional
    public StockResponse createStock(CreateStockRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.warehouseId()));

        if (stockRepository.existsStockByProductIdAndWarehouseId(request.productId(), request.warehouseId())) {
            throw new DuplicateStockException(request.productId(), request.warehouseId());
        }

        Stock stock = new Stock(
                product,
                warehouse,
                request.availableQuantity()
        );

        Stock savedStock = stockRepository.save(stock);

        return stockMapper.toStockResponse(savedStock);
    }

    @Transactional
    public StockResponse increaseAvailable(UUID id, Integer quantity) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(id));

        stock.increaseAvailable(quantity);
        return new StockResponse(
                stock.getId(),
                stock.getProduct().getId(),
                stock.getWarehouse().getId(),
                stock.getAvailableQuantity(),
                stock.getReservedQuantity()
        );
    }

    public StockResponse getStockById(UUID id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(id));

        return  stockMapper.toStockResponse(stock);
    }

    public StockResponse getStockByProductIdAndWarehouseId(UUID productId, UUID warehouseId) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new StockNotFoundException(productId, warehouseId));

        return stockMapper.toStockResponse(stock);
    }
}
