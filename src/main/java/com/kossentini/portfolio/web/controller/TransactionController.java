package com.kossentini.portfolio.web.controller;

import com.kossentini.portfolio.domain.Transaction;
import com.kossentini.portfolio.domain.TransactionType;
import com.kossentini.portfolio.service.TransactionService;
import com.kossentini.portfolio.web.dto.CreateTransactionRequest;
import com.kossentini.portfolio.web.dto.PageResponse;
import com.kossentini.portfolio.web.dto.TransactionResponse;
import com.kossentini.portfolio.web.mapper.TransactionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/transactions")
@Tag(name = "Transactions", description = "Record and query BUY/SELL transactions for a portfolio")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping
    @Operation(summary = "Record a BUY or SELL transaction and update the resulting position")
    public ResponseEntity<TransactionResponse> record(
            @PathVariable Long portfolioId,
            @Valid @RequestBody CreateTransactionRequest request) {
        Transaction created = transactionService.record(portfolioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.toResponse(created));
    }

    @GetMapping
    @Operation(summary = "Search a portfolio's transactions by type and date range, paginated")
    public PageResponse<TransactionResponse> search(
            @PathVariable Long portfolioId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        Page<TransactionResponse> page = transactionService.search(portfolioId, type, from, to, pageable)
                .map(transactionMapper::toResponse);
        return PageResponse.from(page);
    }
}
