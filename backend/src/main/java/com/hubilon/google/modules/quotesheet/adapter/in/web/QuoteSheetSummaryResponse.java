package com.hubilon.google.modules.quotesheet.adapter.in.web;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record QuoteSheetSummaryResponse(
        Long quoteId,
        String quoteNumber,
        String contractorName,
        LocalDate quoteDate,
        BigDecimal totalAmount,
        boolean hasGoogleSheet,
        LocalDateTime createdAt
) {
    public static QuoteSheetSummaryResponse from(QuoteSheet quoteSheet) {
        return new QuoteSheetSummaryResponse(
                quoteSheet.getId(),
                quoteSheet.getQuoteNumber(),
                quoteSheet.getContractorName(),
                quoteSheet.getQuoteDate(),
                quoteSheet.getTotalAmount(),
                quoteSheet.getSheetUrl() != null,
                quoteSheet.getCreateDate()
        );
    }
}
