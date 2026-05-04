package com.hubilon.google.modules.quotesheet.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateQuoteSheetRequest(
        @NotNull LocalDate quoteDate,
        @NotBlank String contractorName,
        @NotEmpty @Valid List<QuoteItemRequest> items,
        String note
) {
    public record QuoteItemRequest(
            @NotBlank String itemName,
            String spec,
            String category,
            @Min(1) int quantity,
            @NotBlank String unit,
            @Min(0) @NotNull BigDecimal unitPrice
    ) {}
}
