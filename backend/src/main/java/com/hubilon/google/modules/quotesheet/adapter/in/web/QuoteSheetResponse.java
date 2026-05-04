package com.hubilon.google.modules.quotesheet.adapter.in.web;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record QuoteSheetResponse(
        Long quoteId,
        String quoteNumber,
        String contractorName,
        LocalDate quoteDate,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
    public static QuoteSheetResponse from(QuoteSheet quoteSheet) {
        return new QuoteSheetResponse(
                quoteSheet.getId(),
                quoteSheet.getQuoteNumber(),
                quoteSheet.getContractorName(),
                quoteSheet.getQuoteDate(),
                quoteSheet.getTotalAmount(),
                quoteSheet.getCreateDate()
        );
    }
}
