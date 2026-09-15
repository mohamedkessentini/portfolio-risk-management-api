package com.kossentini.portfolio.service;

import com.kossentini.portfolio.domain.Instrument;
import com.kossentini.portfolio.domain.InstrumentType;
import com.kossentini.portfolio.exception.DuplicateResourceException;
import com.kossentini.portfolio.exception.ResourceNotFoundException;
import com.kossentini.portfolio.repository.InstrumentRepository;
import com.kossentini.portfolio.web.dto.CreateInstrumentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class InstrumentService {

    private static final Logger log = LoggerFactory.getLogger(InstrumentService.class);

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Transactional
    public Instrument create(CreateInstrumentRequest request) {
        if (instrumentRepository.existsBySymbolIgnoreCase(request.symbol())) {
            throw new DuplicateResourceException("Instrument already exists with symbol: " + request.symbol());
        }
        Instrument instrument = Instrument.builder()
                .symbol(request.symbol().toUpperCase())
                .name(request.name())
                .type(request.type())
                .currency(request.currency().toUpperCase())
                .currentPrice(request.currentPrice())
                .priceUpdatedAt(Instant.now())
                .build();
        Instrument saved = instrumentRepository.save(instrument);
        log.info("Created instrument id={} symbol={}", saved.getId(), saved.getSymbol());
        return saved;
    }

    public Instrument getById(Long id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Instrument", id));
    }

    public Page<Instrument> search(InstrumentType type, String search, Pageable pageable) {
        return instrumentRepository.search(type, search, pageable);
    }

    @Transactional
    public Instrument updatePrice(Long id, BigDecimal newPrice) {
        Instrument instrument = getById(id);
        instrument.setCurrentPrice(newPrice);
        instrument.setPriceUpdatedAt(Instant.now());
        log.info("Updated price for instrument id={} symbol={} newPrice={}", id, instrument.getSymbol(), newPrice);
        return instrument;
    }
}
