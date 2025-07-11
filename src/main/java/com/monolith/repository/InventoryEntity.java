package com.monolith.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

import java.math.BigDecimal;

@Entity
@IdClass(InventoryEntityId.class)
public class InventoryEntity {
    @Id
    private String portfolioId;
    @Id
    private String isin;
    private BigDecimal quantity;
    private BigDecimal averagePrice;

    public InventoryEntity() {
    }

    public InventoryEntity(String portfolioId, String isin, BigDecimal quantity, BigDecimal averagePrice) {
        this.portfolioId = portfolioId;
        this.isin = isin;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public String getIsin() {
        return isin;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
}

