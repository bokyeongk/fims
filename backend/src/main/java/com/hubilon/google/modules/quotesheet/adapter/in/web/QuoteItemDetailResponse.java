package com.hubilon.google.modules.quotesheet.adapter.in.web;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteItem;

import java.math.BigDecimal;

public record QuoteItemDetailResponse(
        String itemName,
        String spec,
        String category,
        int quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal amount
) {
    public static QuoteItemDetailResponse from(QuoteItem item) {
        return new QuoteItemDetailResponse(
                item.getItemName(),
                item.getSpec(),
                item.getCategory(),
                item.getQuantity() != null ? item.getQuantity() : 0,
                item.getUnit(),
                item.getUnitPrice(),
                item.getAmount()
        );
    }
}
