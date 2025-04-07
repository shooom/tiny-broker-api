package com.codility;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * WARNING: Do not modify the class.
 * The file does not need to be submitted, it is only for your reference.
 */
@Service
public class MarketDataService {

    /**
     * Returns the price of the given ISIN.
     * The following price will be returned:
     * <p>
     * - US67066G1040 -> 100.0
     * <p>
     * - US0378331005 -> 200.0
     * <p>
     * - US5949181045 -> 35.50
     * <p>
     * - Any other -> Exception
     *
     * @param isin the ISIN of the security
     *
     * @return the price of the security
     */
    public BigDecimal getPrice(String isin) {
        return switch (isin) {
            case "US67066G1040" -> new BigDecimal("100.00");
            case "US0378331005" -> new BigDecimal("200.00");
            case "US5949181045" -> new BigDecimal("35.50");
            default -> throw new IllegalArgumentException("Unknown ISIN: " + isin);
        };
    }
}