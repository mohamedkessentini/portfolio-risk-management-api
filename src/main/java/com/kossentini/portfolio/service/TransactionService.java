package com.kossentini.portfolio.service;

import com.kossentini.portfolio.domain.*;
import com.kossentini.portfolio.exception.BusinessRuleException;
import com.kossentini.portfolio.exception.ResourceNotFoundException;
import com.kossentini.portfolio.repository.InstrumentRepository;
import com.kossentini.portfolio.repository.PortfolioRepository;
import com.kossentini.portfolio.repository.PositionRepository;
import com.kossentini.portfolio.repository.TransactionRepository;
import com.kossentini.portfolio.web.dto.CreateTransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Records BUY/SELL transactions and keeps the portfolio's Position rows
 * (quantity + average cost) consistent using the weighted-average-cost method.
 */
@Service
@Transactional(readOnly = true)
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final int SCALE = 6;

    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;
    private final InstrumentRepository instrumentRepository;
    private final PositionRepository positionRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               PortfolioRepository portfolioRepository,
                               InstrumentRepository instrumentRepository,
                               PositionRepository positionRepository) {
        this.transactionRepository = transactionRepository;
        this.portfolioRepository = portfolioRepository;
        this.instrumentRepository = instrumentRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public Transaction record(Long portfolioId, CreateTransactionRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Portfolio", portfolioId));
        Instrument instrument = instrumentRepository.findById(request.instrumentId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Instrument", request.instrumentId()));

        Position position = positionRepository.findByPortfolioIdAndInstrumentId(portfolioId, instrument.getId())
                .orElse(null);

        if (request.type() == TransactionType.BUY) {
            position = applyBuy(portfolio, instrument, position, request.quantity(), request.price());
        } else {
            position = applySell(position, request.quantity());
        }
        positionRepository.save(position);

        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .instrument(instrument)
                .type(request.type())
                .quantity(request.quantity())
                .price(request.price())
                .transactionDate(Instant.now())
                .build();
        Transaction saved = transactionRepository.save(transaction);
        log.info("Recorded {} transaction id={} portfolioId={} instrument={} quantity={} price={}",
                request.type(), saved.getId(), portfolioId, instrument.getSymbol(), request.quantity(), request.price());
        return saved;
    }

    private Position applyBuy(Portfolio portfolio, Instrument instrument, Position position,
                               BigDecimal quantity, BigDecimal price) {
        if (position == null) {
            return Position.builder()
                    .portfolio(portfolio)
                    .instrument(instrument)
                    .quantity(quantity)
                    .averageCost(price)
                    .build();
        }
        BigDecimal existingCost = position.getQuantity().multiply(position.getAverageCost());
        BigDecimal newCost = quantity.multiply(price);
        BigDecimal newQuantity = position.getQuantity().add(quantity);
        BigDecimal newAverageCost = existingCost.add(newCost).divide(newQuantity, SCALE, RoundingMode.HALF_UP);

        position.setQuantity(newQuantity);
        position.setAverageCost(newAverageCost);
        return position;
    }

    private Position applySell(Position position, BigDecimal quantity) {
        if (position == null || position.getQuantity().compareTo(quantity) < 0) {
            BigDecimal available = position == null ? BigDecimal.ZERO : position.getQuantity();
            throw new BusinessRuleException(
                    "Cannot sell " + quantity + " units: only " + available + " available in this portfolio");
        }
        // Average cost is unchanged on a sell; only the remaining quantity shrinks.
        position.setQuantity(position.getQuantity().subtract(quantity));
        return position;
    }

    public Transaction getById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Transaction", id));
    }

    public Page<Transaction> search(Long portfolioId, TransactionType type, Instant from, Instant to, Pageable pageable) {
        return transactionRepository.search(portfolioId, type, from, to, pageable);
    }
}
