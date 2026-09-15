package com.kossentini.portfolio.web.dto;

import com.kossentini.portfolio.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotNull(message = "instrumentId is required")
        Long instrumentId,

        @NotNull(message = "type is required")
        TransactionType type,

        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantity must be positive")
        BigDecimal quantity,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "price must be positive")
        BigDecimal price
) {
}
