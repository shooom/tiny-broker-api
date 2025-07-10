package com.monolith.dto.portfolio;

import com.monolith.OrderSide;

import java.math.BigDecimal;

public record PendingOrderDto(
        Long orderId,
        String ticker,
        OrderSide type,
        BigDecimal quantity
//        BigDecimal limitPrice, TODO: limitPrice will be added later
//        Instant createdAt TODO: createdAt will be added later
) {}
