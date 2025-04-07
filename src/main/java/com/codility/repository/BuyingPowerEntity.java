package com.codility.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/**
 * WARNING: Do not modify the entity.
 * The file does not need to be submitted, it is only for your reference.
 */
@Entity
public class BuyingPowerEntity {
    @Id
    private String portfolioId;
    private BigDecimal amount;

    public BuyingPowerEntity(String portfolioId, BigDecimal amount) {
        this.portfolioId = portfolioId;
        this.amount = amount;
    }

    public BuyingPowerEntity() {
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}