package com.kossentini.portfolio.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PortfolioValuationResponse(
        Long portfolioId,
        String baseCurrency,
        BigDecimal totalMarketValue,
        BigDecimal totalCostBasis,
        BigDecimal totalUnrealizedPnl,
        Map<String, BigDecimal> allocationByInstrumentType,
        BigDecimal topHoldingConcentrationPercent,
        List<PositionResponse> positions
) {
}
