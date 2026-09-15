package com.kossentini.portfolio.web.controller;

import com.kossentini.portfolio.domain.Instrument;
import com.kossentini.portfolio.domain.InstrumentType;
import com.kossentini.portfolio.service.InstrumentService;
import com.kossentini.portfolio.web.dto.CreateInstrumentRequest;
import com.kossentini.portfolio.web.dto.InstrumentResponse;
import com.kossentini.portfolio.web.dto.PageResponse;
import com.kossentini.portfolio.web.dto.UpdatePriceRequest;
import com.kossentini.portfolio.web.mapper.InstrumentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instruments")
@Tag(name = "Instruments", description = "Manage tradable financial instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;
    private final InstrumentMapper instrumentMapper;

    public InstrumentController(InstrumentService instrumentService, InstrumentMapper instrumentMapper) {
        this.instrumentService = instrumentService;
        this.instrumentMapper = instrumentMapper;
    }

    @PostMapping
    @Operation(summary = "Create a new instrument")
    public ResponseEntity<InstrumentResponse> create(@Valid @RequestBody CreateInstrumentRequest request) {
        Instrument created = instrumentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(instrumentMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an instrument by id")
    public InstrumentResponse getById(@PathVariable Long id) {
        return instrumentMapper.toResponse(instrumentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Search instruments by type and/or free-text symbol/name, paginated")
    public PageResponse<InstrumentResponse> search(
            @RequestParam(required = false) InstrumentType type,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<InstrumentResponse> page = instrumentService.search(type, search, pageable).map(instrumentMapper::toResponse);
        return PageResponse.from(page);
    }

    @PatchMapping("/{id}/price")
    @Operation(summary = "Update the current market price of an instrument")
    public InstrumentResponse updatePrice(@PathVariable Long id, @Valid @RequestBody UpdatePriceRequest request) {
        return instrumentMapper.toResponse(instrumentService.updatePrice(id, request.price()));
    }
}
