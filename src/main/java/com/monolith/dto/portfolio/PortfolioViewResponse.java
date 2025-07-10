package com.monolith.dto.portfolio;


import java.math.BigDecimal;
import java.util.List;

public record PortfolioViewResponse(
        String portfolioId,
        BigDecimal availableCash,
        List<HoldingDto> holdings,
        List<PendingOrderDto> pendingOrders
) {
}
