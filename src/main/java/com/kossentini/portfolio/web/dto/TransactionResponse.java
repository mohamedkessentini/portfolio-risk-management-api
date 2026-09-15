package com.kossentini.portfolio.web.dto;

import com.kossentini.portfolio.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long portfolioId,
        Long instrumentId,
        String instrumentSymbol,
        TransactionType type,
        BigDecimal quantity,
        BigDecimal price,
        Instant transactionDate
) {
}
