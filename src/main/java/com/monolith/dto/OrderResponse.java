package com.monolith.dto;

import com.monolith.OrderSide;
import com.monolith.OrderStatus;

import java.math.BigDecimal;

public class OrderResponse {
    private Long id;
    private String portfolioId;
    private String isin;
    private OrderSide side;
    private BigDecimal quantity;
    private OrderStatus status;

    public OrderResponse(Long id, String portfolioId, String isin, OrderSide side, BigDecimal quantity, OrderStatus status) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.isin = isin;
        this.side = side;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public String getIsin() {
        return isin;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }
}