package com.kossentini.portfolio.web.dto;

import java.time.Instant;

public record PortfolioResponse(
        Long id,
        String name,
        String ownerName,
        String baseCurrency,
        Instant createdAt
) {
}
