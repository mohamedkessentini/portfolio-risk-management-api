package com.kossentini.portfolio.web.dto;

import com.kossentini.portfolio.domain.InstrumentType;

import java.math.BigDecimal;

public record PositionResponse(
        Long id,
        Long instrumentId,
        String instrumentSymbol,
        InstrumentType instrumentType,
        BigDecimal quantity,
        BigDecimal averageCost,
        BigDecimal marketPrice,
        BigDecimal marketValue,
        BigDecimal costBasis,
        BigDecimal unrealizedPnl
) {
}
