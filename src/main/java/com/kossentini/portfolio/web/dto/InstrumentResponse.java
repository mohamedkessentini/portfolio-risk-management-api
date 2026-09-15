package com.kossentini.portfolio.web.dto;

import com.kossentini.portfolio.domain.InstrumentType;

import java.math.BigDecimal;
import java.time.Instant;

public record InstrumentResponse(
        Long id,
        String symbol,
        String name,
        InstrumentType type,
        String currency,
        BigDecimal currentPrice,
        Instant priceUpdatedAt
) {
}
