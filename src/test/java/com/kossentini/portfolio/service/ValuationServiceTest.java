package com.kossentini.portfolio.service;

import com.kossentini.portfolio.domain.*;
import com.kossentini.portfolio.repository.PositionRepository;
import com.kossentini.portfolio.web.dto.PortfolioValuationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValuationServiceTest {

    @Mock
    private PortfolioService portfolioService;
    @Mock
    private PositionRepository positionRepository;

    private ValuationService valuationService;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        valuationService = new ValuationService(portfolioService, positionRepository);
        portfolio = Portfolio.builder().id(1L).name("Growth Fund").ownerName("Mohamed").baseCurrency("EUR").build();
    }

    @Test
    void computesTotalMarketValueCostBasisAndPnl() {
        when(portfolioService.getById(1L)).thenReturn(portfolio);
        Instrument apple = instrument(1L, "AAPL", InstrumentType.EQUITY, "190.00");
        Instrument bond = instrument(2L, "GOVBOND", InstrumentType.BOND, "100.00");

        Position applePosition = position(apple, "10", "150.00"); // market 1900, cost 1500
        Position bondPosition = position(bond, "20", "100.00");   // market 2000, cost 2000

        when(positionRepository.findAllByPortfolioIdWithInstrument(1L))
                .thenReturn(List.of(applePosition, bondPosition));

        PortfolioValuationResponse result = valuationService.valuate(1L);

        assertThat(result.totalMarketValue()).isEqualByComparingTo("3900.0000");
        assertThat(result.totalCostBasis()).isEqualByComparingTo("3500.0000");
        assertThat(result.totalUnrealizedPnl()).isEqualByComparingTo("400.0000");
    }

    @Test
    void computesAllocationByInstrumentTypeAsPercentages() {
        when(portfolioService.getById(1L)).thenReturn(portfolio);
        Instrument apple = instrument(1L, "AAPL", InstrumentType.EQUITY, "100.00");
        Instrument bond = instrument(2L, "GOVBOND", InstrumentType.BOND, "100.00");

        // 75 EQUITY / 25 BOND split
        Position applePosition = position(apple, "30", "80.00");
        Position bondPosition = position(bond, "10", "90.00");

        when(positionRepository.findAllByPortfolioIdWithInstrument(1L))
                .thenReturn(List.of(applePosition, bondPosition));

        PortfolioValuationResponse result = valuationService.valuate(1L);

        assertThat(result.allocationByInstrumentType().get("EQUITY")).isEqualByComparingTo("75.00");
        assertThat(result.allocationByInstrumentType().get("BOND")).isEqualByComparingTo("25.00");
    }

    @Test
    void computesTopHoldingConcentrationAsLargestPositionShare() {
        when(portfolioService.getById(1L)).thenReturn(portfolio);
        Instrument apple = instrument(1L, "AAPL", InstrumentType.EQUITY, "100.00");
        Instrument bond = instrument(2L, "GOVBOND", InstrumentType.BOND, "100.00");

        Position applePosition = position(apple, "90", "100.00"); // market value 9000
        Position bondPosition = position(bond, "10", "100.00");   // market value 1000

        when(positionRepository.findAllByPortfolioIdWithInstrument(1L))
                .thenReturn(List.of(applePosition, bondPosition));

        PortfolioValuationResponse result = valuationService.valuate(1L);

        assertThat(result.topHoldingConcentrationPercent()).isEqualByComparingTo("90.00");
    }

    @Test
    void returnsZeroConcentrationWhenPortfolioIsEmpty() {
        when(portfolioService.getById(1L)).thenReturn(portfolio);
        when(positionRepository.findAllByPortfolioIdWithInstrument(1L)).thenReturn(List.of());

        PortfolioValuationResponse result = valuationService.valuate(1L);

        assertThat(result.totalMarketValue()).isEqualByComparingTo("0.0000");
        assertThat(result.topHoldingConcentrationPercent()).isEqualByComparingTo("0");
    }

    private Instrument instrument(Long id, String symbol, InstrumentType type, String price) {
        return Instrument.builder().id(id).symbol(symbol).name(symbol).type(type)
                .currency("USD").currentPrice(new BigDecimal(price)).build();
    }

    private Position position(Instrument instrument, String quantity, String averageCost) {
        return Position.builder().id(instrument.getId()).portfolio(portfolio).instrument(instrument)
                .quantity(new BigDecimal(quantity)).averageCost(new BigDecimal(averageCost)).build();
    }
}
