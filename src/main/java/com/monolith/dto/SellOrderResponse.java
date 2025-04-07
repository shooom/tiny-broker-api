package com.monolith.dto;

import com.monolith.OrderSide;
import com.monolith.OrderStatus;

import java.math.BigDecimal;

public class SellOrderResponse extends OrderResponse {
    private BigDecimal price;
    
    public SellOrderResponse(Long id, String portfolioId, String isin, BigDecimal quantity, OrderStatus status, BigDecimal price) {
        super(id, portfolioId, isin, OrderSide.SELL, quantity, status);
        this.price = price;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
}