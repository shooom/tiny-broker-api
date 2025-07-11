package com.monolith.dto.portfolio;

import java.math.BigDecimal;

public record HoldingDto(
        String isin,
        BigDecimal quantity,
        BigDecimal averagePrice
        ) {}
