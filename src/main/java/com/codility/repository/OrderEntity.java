package com.codility.repository;

import com.codility.OrderSide;
import com.codility.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/**
 * WARNING: Do not modify the entity.
 * The file does not need to be submitted, it is only for your reference.
 */
@Entity
public class OrderEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String portfolioId;
    private String isin;
    private OrderStatus status;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;

    public OrderEntity(String portfolioId, String isin, OrderStatus orderStatus, OrderSide side, BigDecimal quantity, BigDecimal price) {
        this.portfolioId = portfolioId;
        this.isin = isin;
        this.status = orderStatus;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderEntity() {
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
