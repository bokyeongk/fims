package com.hubilon.google.modules.quotesheet.adapter.in.web;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record QuoteSheetDetailResponse(
        Long quoteId,
        String quoteNumber,
        String contractorName,
        LocalDate quoteDate,
        BigDecimal totalAmount,
        boolean hasGoogleSheet,
        String sheetUrl,
        String note,
        List<QuoteItemDetailResponse> items,
        LocalDateTime createdAt
) {
    public static QuoteSheetDetailResponse from(QuoteSheet quoteSheet) {
        List<QuoteItemDetailResponse> itemResponses = quoteSheet.getItems().stream()
                .map(QuoteItemDetailResponse::from)
                .toList();

        return new QuoteSheetDetailResponse(
                quoteSheet.getId(),
                quoteSheet.getQuoteNumber(),
                quoteSheet.getContractorName(),
                quoteSheet.getQuoteDate(),
                quoteSheet.getTotalAmount(),
                quoteSheet.getSheetUrl() != null,
                quoteSheet.getSheetUrl(),
                quoteSheet.getNote(),
                itemResponses,
                quoteSheet.getCreateDate()
        );
    }
}
