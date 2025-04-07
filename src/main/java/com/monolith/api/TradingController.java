package com.monolith.api;

import com.monolith.OrderSide;
import com.monolith.dto.OrderRequest;
import com.monolith.dto.OrderResponse;
import com.monolith.dto.SellOrderResponse;
import com.monolith.exception.InsufficientBuyingPowerException;
import com.monolith.exception.InsufficientInventoryException;
import com.monolith.repository.OrderEntity;
import com.monolith.service.TradingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Creates a new BUY or SELL order.
     *
     * @param request the order request
     * @return the created order response
     * @throws InsufficientBuyingPowerException if there's not enough buying power for a buy order
     * @throws InsufficientInventoryException if there's not enough inventory for a sell order
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request)
            throws InsufficientBuyingPowerException, InsufficientInventoryException {

        OrderEntity order = tradingService.createOrder(request);

        OrderResponse response = mapToResponse(order);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the order response
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        OrderEntity order = tradingService.getOrder(id);

        OrderResponse response = mapToResponse(order);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels an order by its ID.
     *
     * @param id the order ID
     * @return the updated order response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        OrderEntity cancelledOrder = tradingService.cancelOrder(id);

        OrderResponse response = mapToResponse(cancelledOrder);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Executes an order by its ID.
     * For BUY orders: Deducts buying power and adds to inventory.
     * For SELL orders: Removes from inventory and adds to buying power.
     *
     * @param id the order ID
     * @return the executed order response
     * @throws InsufficientBuyingPowerException if there's not enough buying power for a buy order
     * @throws InsufficientInventoryException if there's not enough inventory for a sell order
     */
    @PutMapping("/{id}/execute")
    public ResponseEntity<OrderResponse> executeOrder(@PathVariable Long id)
            throws InsufficientBuyingPowerException, InsufficientInventoryException {
        OrderEntity executedOrder = tradingService.executeOrder(id);

        OrderResponse response = mapToResponse(executedOrder);
        return ResponseEntity.ok(response);
    }

    private OrderResponse mapToResponse(OrderEntity order) {
        if (order.getSide() == OrderSide.SELL) {
            return new SellOrderResponse(
                    order.getId(),
                    order.getPortfolioId(),
                    order.getIsin(),
                    order.getQuantity(),
                    order.getStatus(),
                    order.getPrice()
            );
        } else {
            return new OrderResponse(
                    order.getId(),
                    order.getPortfolioId(),
                    order.getIsin(),
                    order.getSide(),
                    order.getQuantity(),
                    order.getStatus()
            );
        }
    }
}