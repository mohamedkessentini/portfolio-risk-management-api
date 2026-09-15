package com.kossentini.portfolio.web.controller;

import com.kossentini.portfolio.domain.Portfolio;
import com.kossentini.portfolio.service.PortfolioService;
import com.kossentini.portfolio.service.ValuationService;
import com.kossentini.portfolio.web.dto.*;
import com.kossentini.portfolio.web.mapper.PortfolioMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios")
@Tag(name = "Portfolios", description = "Manage investment portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final ValuationService valuationService;
    private final PortfolioMapper portfolioMapper;

    public PortfolioController(PortfolioService portfolioService, ValuationService valuationService,
                                PortfolioMapper portfolioMapper) {
        this.portfolioService = portfolioService;
        this.valuationService = valuationService;
        this.portfolioMapper = portfolioMapper;
    }

    @PostMapping
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<PortfolioResponse> create(@Valid @RequestBody CreatePortfolioRequest request) {
        Portfolio created = portfolioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a portfolio by id")
    public PortfolioResponse getById(@PathVariable Long id) {
        return portfolioMapper.toResponse(portfolioService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Search portfolios by name, paginated")
    public PageResponse<PortfolioResponse> search(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        Page<PortfolioResponse> page = portfolioService.search(name, pageable).map(portfolioMapper::toResponse);
        return PageResponse.from(page);
    }

    @GetMapping("/{id}/valuation")
    @Operation(summary = "Compute current market valuation and risk exposure for a portfolio")
    public PortfolioValuationResponse valuation(@PathVariable Long id) {
        return valuationService.valuate(id);
    }
}
