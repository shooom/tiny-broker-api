package com.monolith.service;

import com.monolith.dto.portfolio.HoldingDto;
import com.monolith.dto.portfolio.PendingOrderDto;
import com.monolith.dto.portfolio.PortfolioViewResponse;
import com.monolith.repository.BuyingPowerEntity;
import com.monolith.repository.InventoryEntity;
import com.monolith.repository.OrderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortfolioService {

    private final BuyingPowerService buyingPowerService;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    public PortfolioService(BuyingPowerService buyingPowerService, OrderService orderService, InventoryService inventoryService) {
        this.buyingPowerService = buyingPowerService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public PortfolioViewResponse getPortfolio(String portfolioId) {
        BuyingPowerEntity portfolioPower = buyingPowerService.getBuyingPower(portfolioId);
        List<InventoryEntity> inventoryList = inventoryService.getInventoriesByPortfolioId(portfolioId);
        List<OrderEntity> ordersList = orderService.getOrdersForPortfolio(portfolioId);

        List<HoldingDto> holdings = inventoryList.stream()
                .map(inventory -> new HoldingDto(
                        inventory.getIsin(),
                        inventory.getQuantity()
                ))
                .toList();

        List<PendingOrderDto> pendingOrders = ordersList.stream()
                .map(order -> new PendingOrderDto(
                        order.getId(),
                        order.getIsin(),
                        order.getSide(),
                        order.getQuantity()
                ))
                .toList();

        return new PortfolioViewResponse(
                portfolioId,
                portfolioPower.getAmount(),
                holdings,
                pendingOrders);
    }

}
