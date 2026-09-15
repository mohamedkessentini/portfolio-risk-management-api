package com.kossentini.portfolio.service;

import com.kossentini.portfolio.domain.Instrument;
import com.kossentini.portfolio.domain.InstrumentType;
import com.kossentini.portfolio.domain.Portfolio;
import com.kossentini.portfolio.domain.Position;
import com.kossentini.portfolio.repository.PositionRepository;
import com.kossentini.portfolio.web.dto.PortfolioValuationResponse;
import com.kossentini.portfolio.web.dto.PositionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes portfolio valuation and basic risk-exposure metrics
 * (asset-class allocation, concentration) from current positions and market prices.
 */
@Service
@Transactional(readOnly = true)
public class ValuationService {

    private static final int MONEY_SCALE = 4;
    private static final int PERCENT_SCALE = 2;

    private final PortfolioService portfolioService;
    private final PositionRepository positionRepository;

    public ValuationService(PortfolioService portfolioService, PositionRepository positionRepository) {
        this.portfolioService = portfolioService;
        this.positionRepository = positionRepository;
    }

    public PortfolioValuationResponse valuate(Long portfolioId) {
        Portfolio portfolio = portfolioService.getById(portfolioId);
        List<Position> positions = positionRepository.findAllByPortfolioIdWithInstrument(portfolioId);

        List<PositionResponse> positionResponses = positions.stream()
                .map(this::toPositionResponse)
                .collect(Collectors.toList());

        BigDecimal totalMarketValue = sum(positionResponses, PositionResponse::marketValue);
        BigDecimal totalCostBasis = sum(positionResponses, PositionResponse::costBasis);
        BigDecimal totalUnrealizedPnl = totalMarketValue.subtract(totalCostBasis);

        Map<String, BigDecimal> allocation = computeAllocationByType(positions, totalMarketValue);
        BigDecimal concentration = computeTopHoldingConcentration(positionResponses, totalMarketValue);

        return new PortfolioValuationResponse(
                portfolio.getId(),
                portfolio.getBaseCurrency(),
                totalMarketValue,
                totalCostBasis,
                totalUnrealizedPnl,
                allocation,
                concentration,
                positionResponses
        );
    }

    private PositionResponse toPositionResponse(Position position) {
        Instrument instrument = position.getInstrument();
        BigDecimal marketValue = position.getQuantity().multiply(instrument.getCurrentPrice())
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal costBasis = position.getQuantity().multiply(position.getAverageCost())
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal unrealizedPnl = marketValue.subtract(costBasis);

        return new PositionResponse(
                position.getId(),
                instrument.getId(),
                instrument.getSymbol(),
                instrument.getType(),
                position.getQuantity(),
                position.getAverageCost(),
                instrument.getCurrentPrice(),
                marketValue,
                costBasis,
                unrealizedPnl
        );
    }

    private Map<String, BigDecimal> computeAllocationByType(List<Position> positions, BigDecimal totalMarketValue) {
        Map<InstrumentType, BigDecimal> valueByType = new EnumMap<>(InstrumentType.class);
        for (Position position : positions) {
            BigDecimal marketValue = position.getQuantity().multiply(position.getInstrument().getCurrentPrice());
            valueByType.merge(position.getInstrument().getType(), marketValue, BigDecimal::add);
        }

        Map<String, BigDecimal> allocation = new java.util.LinkedHashMap<>();
        for (Map.Entry<InstrumentType, BigDecimal> entry : valueByType.entrySet()) {
            allocation.put(entry.getKey().name(), toPercentage(entry.getValue(), totalMarketValue));
        }
        return allocation;
    }

    private BigDecimal computeTopHoldingConcentration(List<PositionResponse> positions, BigDecimal totalMarketValue) {
        return positions.stream()
                .map(PositionResponse::marketValue)
                .max(BigDecimal::compareTo)
                .map(max -> toPercentage(max, totalMarketValue))
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal toPercentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, PERCENT_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PositionResponse> positions, java.util.function.Function<PositionResponse, BigDecimal> extractor) {
        return positions.stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
