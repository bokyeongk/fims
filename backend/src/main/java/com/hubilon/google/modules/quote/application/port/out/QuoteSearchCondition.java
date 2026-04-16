package com.hubilon.google.modules.quote.application.port.out;

import java.time.LocalDate;

public record QuoteSearchCondition(
        LocalDate startDate,
        LocalDate endDate,
        String contractorName
) {
}
