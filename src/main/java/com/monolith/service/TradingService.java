package com.monolith.service;

import com.monolith.MarketDataService;
import com.monolith.dto.OrderRequest;
import com.monolith.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.monolith.OrderSide.*;

@Service
public class TradingService {

    private static final Logger log = LoggerFactory.getLogger(TradingService.class);

    private final OrderService orderService;
    private final BuyingPowerService buyingPowerService;
    private final InventoryService inventoryService;
    private final MarketDataService marketDataService;

    public TradingService(OrderService orderService,
                          BuyingPowerService buyingPowerService,
                          InventoryService inventoryService,
                          MarketDataService marketDataService) {
        this.orderService = orderService;
        this.buyingPowerService = buyingPowerService;
        this.inventoryService = inventoryService;
        this.marketDataService = marketDataService;
    }

    /**
     * @param request new order request
     * @return the created order entity
     */
    @Transactional
    public OrderEntity createOrder(OrderRequest request) {
        log.info("Trying to create {} order for {} portfolio", request.getSide(), request.getPortfolioId());
        return switch (request.getSide()) {
            case BUY -> createBuyOrder(request);
            case SELL -> createSellOrder(request);
        };
    }

    /**
     * @param orderId the ID of the order to retrieve
     * @return the order entity
     */
    public OrderEntity getOrder(Long orderId) {
        log.info("Trying to look for {} order", orderId);
        return orderService.getOrder(orderId);
    }

    /**
     * @param orderId the ID of the order to execute
     * @return the executed order entity
     */
    @Transactional
    public OrderEntity executeOrder(Long orderId) {
        OrderEntity order = orderService.getOrderForExecution(orderId);
        log.info("Trying to execute {} order with id {}", order.getSide(), orderId);
        if (order.getSide() == BUY) {
            executeBuyOrder(order);
        } else if (order.getSide() == SELL) {
            executeSellOrder(order);
        }

        return orderService.finalizeOrderExecution(order);
    }

    /**
     * @param order the BUY order to execute
     */
    private void executeBuyOrder(OrderEntity order) {
        BigDecimal totalCost = order.getPrice().multiply(order.getQuantity());

        // Verify and deduct buying power
        buyingPowerService.deductBuyingPower(order.getPortfolioId(), totalCost);

        // Add securities to inventory
        inventoryService.addToInventory(order.getPortfolioId(), order.getIsin(), order.getQuantity());
    }

    /**
     * @param order the SELL order to execute
     */
    private void executeSellOrder(OrderEntity order) {
        BigDecimal totalProceeds = order.getPrice().multiply(order.getQuantity());

        // Verify and remove from inventory
        inventoryService.removeFromInventory(order.getPortfolioId(), order.getIsin(), order.getQuantity());

        // Add proceeds to buying power
        buyingPowerService.addBuyingPower(order.getPortfolioId(), totalProceeds);
    }

    /**
     * @param orderId the ID of the order to cancel
     * @return the updated order entity
     */
    @Transactional
    public OrderEntity cancelOrder(Long orderId) {
        log.info("Trying to cancel {} order", orderId);
        return orderService.cancelOrder(orderId);
    }

    /**
     * @return the created order entity
     */
    @Transactional
    protected OrderEntity createBuyOrder(OrderRequest request) {

        BigDecimal currentPrice = marketDataService.getPrice(request.getIsin());
        BigDecimal requiredBuyingPower = currentPrice.multiply(request.getQuantity());
        buyingPowerService.verifySufficientBuyingPower(request.getPortfolioId(), requiredBuyingPower);

        return orderService.createOrder(request, currentPrice);
    }

    /**
     * @return the created order entity
     */
    @Transactional
    protected OrderEntity createSellOrder(OrderRequest request) {
        BigDecimal currentPrice = marketDataService.getPrice(request.getIsin());
        inventoryService.getAndVerifyInventory(request.getPortfolioId(), request.getIsin(), request.getQuantity());

        return orderService.createOrder(request, currentPrice);
    }
}