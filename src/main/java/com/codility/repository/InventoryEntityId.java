package com.codility.repository;

import java.io.Serializable;

/**
 * WARNING: Do not modify the class.
 * The file does not need to be submitted, it is only for your reference.
 */
public class InventoryEntityId implements Serializable {
    private String portfolioId;
    private String isin;

    public InventoryEntityId(String portfolioId, String isin) {
        this.portfolioId = portfolioId;
        this.isin = isin;
    }

    public InventoryEntityId() {
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public String getIsin() {
        return isin;
    }
}
