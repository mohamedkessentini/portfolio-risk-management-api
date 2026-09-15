package com.kossentini.portfolio.web.dto;

import com.kossentini.portfolio.domain.InstrumentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateInstrumentRequest(

        @NotBlank(message = "symbol is required")
        String symbol,

        @NotBlank(message = "name is required")
        String name,

        @NotNull(message = "type is required")
        InstrumentType type,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO 4217 code, e.g. USD")
        String currency,

        @NotNull(message = "currentPrice is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "currentPrice must be positive")
        BigDecimal currentPrice
) {
}
