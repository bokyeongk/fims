package com.hubilon.google.modules.quote.application.port.in;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.modules.quote.adapter.in.web.QuoteSortType;

import java.time.LocalDate;

public record GetQuoteListQuery(
        LocalDate startDate,
        LocalDate endDate,
        String contractorName,
        QuoteSortType sort,
        int page,
        int size
) {
    public GetQuoteListQuery {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new ServiceException(ErrorCode.INVALID_REQUEST, "startDate must be before or equal to endDate");
            }
            if (startDate.plusDays(365).isBefore(endDate)) {
                throw new ServiceException(ErrorCode.INVALID_REQUEST, "Date range must be within 365 days");
            }
        }
    }
}
