package com.kossentini.portfolio.service;

import com.kossentini.portfolio.domain.Portfolio;
import com.kossentini.portfolio.exception.ResourceNotFoundException;
import com.kossentini.portfolio.repository.PortfolioRepository;
import com.kossentini.portfolio.web.dto.CreatePortfolioRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional
    public Portfolio create(CreatePortfolioRequest request) {
        Portfolio portfolio = Portfolio.builder()
                .name(request.name())
                .ownerName(request.ownerName())
                .baseCurrency(request.baseCurrency().toUpperCase())
                .build();
        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("Created portfolio id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    public Portfolio getById(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Portfolio", id));
    }

    public Page<Portfolio> search(String name, Pageable pageable) {
        if (StringUtils.hasText(name)) {
            return portfolioRepository.findByNameContainingIgnoreCase(name, pageable);
        }
        return portfolioRepository.findAll(pageable);
    }
}
