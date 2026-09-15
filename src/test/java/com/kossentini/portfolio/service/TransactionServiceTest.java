package com.kossentini.portfolio.service;

import com.kossentini.portfolio.domain.*;
import com.kossentini.portfolio.exception.BusinessRuleException;
import com.kossentini.portfolio.exception.ResourceNotFoundException;
import com.kossentini.portfolio.repository.InstrumentRepository;
import com.kossentini.portfolio.repository.PortfolioRepository;
import com.kossentini.portfolio.repository.PositionRepository;
import com.kossentini.portfolio.repository.TransactionRepository;
import com.kossentini.portfolio.web.dto.CreateTransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private InstrumentRepository instrumentRepository;
    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Portfolio portfolio;
    private Instrument instrument;

    @BeforeEach
    void setUp() {
        portfolio = Portfolio.builder().id(1L).name("Growth Fund").ownerName("Mohamed").baseCurrency("EUR").build();
        instrument = Instrument.builder().id(10L).symbol("AAPL").name("Apple Inc.")
                .type(InstrumentType.EQUITY).currency("USD").currentPrice(new BigDecimal("190.00")).build();
    }

    private void stubExistingPortfolioAndInstrument() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(instrumentRepository.findById(10L)).thenReturn(Optional.of(instrument));
    }

    private void stubTransactionSave() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void buyOpensANewPositionWhenNoneExists() {
        stubExistingPortfolioAndInstrument();
        stubTransactionSave();
        when(positionRepository.findByPortfolioIdAndInstrumentId(1L, 10L)).thenReturn(Optional.empty());
        CreateTransactionRequest request = new CreateTransactionRequest(10L, TransactionType.BUY,
                new BigDecimal("10"), new BigDecimal("100.00"));

        transactionService.record(1L, request);

        verify(positionRepository).save(argThat(position ->
                position.getQuantity().compareTo(new BigDecimal("10")) == 0
                        && position.getAverageCost().compareTo(new BigDecimal("100.00")) == 0));
    }

    @Test
    void secondBuyRecomputesWeightedAverageCost() {
        stubExistingPortfolioAndInstrument();
        stubTransactionSave();
        Position existing = Position.builder().id(100L).portfolio(portfolio).instrument(instrument)
                .quantity(new BigDecimal("10")).averageCost(new BigDecimal("100.00")).build();
        when(positionRepository.findByPortfolioIdAndInstrumentId(1L, 10L)).thenReturn(Optional.of(existing));

        // Buy 10 more units at 200 -> new average = (10*100 + 10*200) / 20 = 150
        CreateTransactionRequest request = new CreateTransactionRequest(10L, TransactionType.BUY,
                new BigDecimal("10"), new BigDecimal("200.00"));

        transactionService.record(1L, request);

        verify(positionRepository).save(argThat(position ->
                position.getQuantity().compareTo(new BigDecimal("20")) == 0
                        && position.getAverageCost().compareTo(new BigDecimal("150.000000")) == 0));
    }

    @Test
    void sellReducesQuantityWithoutChangingAverageCost() {
        stubExistingPortfolioAndInstrument();
        stubTransactionSave();
        Position existing = Position.builder().id(100L).portfolio(portfolio).instrument(instrument)
                .quantity(new BigDecimal("10")).averageCost(new BigDecimal("100.00")).build();
        when(positionRepository.findByPortfolioIdAndInstrumentId(1L, 10L)).thenReturn(Optional.of(existing));

        CreateTransactionRequest request = new CreateTransactionRequest(10L, TransactionType.SELL,
                new BigDecimal("4"), new BigDecimal("120.00"));

        transactionService.record(1L, request);

        verify(positionRepository).save(argThat(position ->
                position.getQuantity().compareTo(new BigDecimal("6")) == 0
                        && position.getAverageCost().compareTo(new BigDecimal("100.00")) == 0));
    }

    @Test
    void sellingMoreThanHeldIsRejected() {
        stubExistingPortfolioAndInstrument();
        Position existing = Position.builder().id(100L).portfolio(portfolio).instrument(instrument)
                .quantity(new BigDecimal("5")).averageCost(new BigDecimal("100.00")).build();
        when(positionRepository.findByPortfolioIdAndInstrumentId(1L, 10L)).thenReturn(Optional.of(existing));

        CreateTransactionRequest request = new CreateTransactionRequest(10L, TransactionType.SELL,
                new BigDecimal("6"), new BigDecimal("120.00"));

        assertThatThrownBy(() -> transactionService.record(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("only 5");

        verify(positionRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void sellingWithNoExistingPositionIsRejected() {
        stubExistingPortfolioAndInstrument();
        when(positionRepository.findByPortfolioIdAndInstrumentId(1L, 10L)).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(10L, TransactionType.SELL,
                new BigDecimal("1"), new BigDecimal("120.00"));

        assertThatThrownBy(() -> transactionService.record(1L, request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void recordThrowsWhenPortfolioDoesNotExist() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.empty());
        CreateTransactionRequest request = new CreateTransactionRequest(10L, TransactionType.BUY,
                new BigDecimal("1"), new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.record(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
