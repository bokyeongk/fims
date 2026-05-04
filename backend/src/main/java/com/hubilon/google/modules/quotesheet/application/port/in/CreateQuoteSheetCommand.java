package com.hubilon.google.modules.quotesheet.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateQuoteSheetCommand(
        Long userId,
        LocalDate quoteDate,
        String contractorName,
        List<QuoteItemCommand> items,
        String note
) {
    public record QuoteItemCommand(
            String itemName,
            String spec,
            String category,
            int quantity,
            String unit,
            BigDecimal unitPrice
    ) {}
}
