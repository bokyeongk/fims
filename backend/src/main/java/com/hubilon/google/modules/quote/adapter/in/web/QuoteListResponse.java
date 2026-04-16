package com.hubilon.google.modules.quote.adapter.in.web;

import com.hubilon.google.modules.quote.domain.model.Quote;

import java.time.LocalDate;

public record QuoteListResponse(
        Long id,
        String status,
        LocalDate contractDate,
        String contractorName,
        String constructionLocation
) {
    public static QuoteListResponse from(Quote quote) {
        return new QuoteListResponse(
                quote.getId(),
                quote.getStatus().name(),
                quote.getContractDate(),
                quote.getContractorName(),
                quote.getConstructionLocation()
        );
    }
}
