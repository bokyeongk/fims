package com.hubilon.google.modules.quote.application.service;

import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.modules.quote.adapter.in.web.QuoteSortType;
import com.hubilon.google.modules.quote.application.port.in.GetQuoteListQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("QuoteService - GetQuoteListQuery 유효성 검증")
class QuoteServiceTest {

    @Test
    @DisplayName("startDate가 endDate보다 이후이면 ServiceException 발생")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        LocalDate startDate = LocalDate.of(2026, 4, 15);
        LocalDate endDate   = LocalDate.of(2026, 4, 1);

        assertThatThrownBy(() ->
                new GetQuoteListQuery(startDate, endDate, null, QuoteSortType.LATEST, 0, 10))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("startDate must be before or equal to endDate");
    }

    @Test
    @DisplayName("조회 범위가 365일을 초과하면 ServiceException 발생")
    void shouldThrowExceptionWhenDateRangeExceeds365Days() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate   = LocalDate.of(2026, 4, 16); // 470일 이상

        assertThatThrownBy(() ->
                new GetQuoteListQuery(startDate, endDate, null, QuoteSortType.LATEST, 0, 10))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Date range must be within 365 days");
    }

    @Test
    @DisplayName("startDate와 endDate가 같으면 정상 생성")
    void shouldNotThrowWhenStartDateEqualsEndDate() {
        LocalDate date = LocalDate.of(2026, 4, 16);

        assertThatCode(() ->
                new GetQuoteListQuery(date, date, null, QuoteSortType.LATEST, 0, 10))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("조회 범위가 365일 이내이면 정상 생성")
    void shouldNotThrowWhenDateRangeIsWithin365Days() {
        LocalDate startDate = LocalDate.of(2025, 4, 16);
        LocalDate endDate   = LocalDate.of(2026, 4, 16); // 정확히 365일

        assertThatCode(() ->
                new GetQuoteListQuery(startDate, endDate, null, QuoteSortType.LATEST, 0, 10))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("startDate, endDate가 null이면 유효성 검증 건너뜀")
    void shouldNotThrowWhenDatesAreNull() {
        assertThatCode(() ->
                new GetQuoteListQuery(null, null, "홍길동", QuoteSortType.OLDEST, 0, 10))
                .doesNotThrowAnyException();
    }
}
