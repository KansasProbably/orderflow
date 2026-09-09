package io.github.kansasprobably.orderflow.order;

import io.github.kansasprobably.orderflow.customer.Customer;
import io.github.kansasprobably.orderflow.customer.CustomerRepository;
import io.github.kansasprobably.orderflow.customer.exception.CustomerNotFoundException;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderItemRequest;
import io.github.kansasprobably.orderflow.order.dto.CreateOrderRequest;
import io.github.kansasprobably.orderflow.order.dto.OrderResponse;
import io.github.kansasprobably.orderflow.order.mapper.OrderMapper;
import io.github.kansasprobably.orderflow.stock.Stock;
import io.github.kansasprobably.orderflow.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final StockService stockService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        Customer customer = customerRepository.findById(createOrderRequest.customerId())
                .orElseThrow(() ->
                        new CustomerNotFoundException(createOrderRequest.customerId()));

        Order order = new Order(customer);

        for (CreateOrderItemRequest createOrderItemRequest : createOrderRequest.items()) {
            Stock stock = stockService.reserve(
                    createOrderItemRequest.productId(),
                    createOrderItemRequest.warehouseId(),
                    createOrderItemRequest.quantity()
            );

            order.addItem(
                    stock.getProduct(),
                    stock.getWarehouse(),
                    createOrderItemRequest.quantity()
            );
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);


    }
}
