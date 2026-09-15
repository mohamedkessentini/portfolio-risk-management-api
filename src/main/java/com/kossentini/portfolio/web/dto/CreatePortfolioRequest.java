package com.kossentini.portfolio.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatePortfolioRequest(

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "ownerName is required")
        String ownerName,

        @NotBlank(message = "baseCurrency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "baseCurrency must be a 3-letter ISO 4217 code, e.g. EUR")
        String baseCurrency
) {
}
