package com.hubilon.google.modules.quote.adapter.in.web;

import com.hubilon.google.common.response.ApiResponse;
import com.hubilon.google.common.response.PageResponse;
import com.hubilon.google.modules.quote.application.port.in.GetQuoteListQuery;
import com.hubilon.google.modules.quote.application.port.in.GetQuoteListUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@Tag(name = "Quote", description = "견적 관리 API")
public class QuoteController {

    private final GetQuoteListUseCase getQuoteListUseCase;

    @GetMapping
    @Operation(summary = "견적 목록 조회")
    public ResponseEntity<ApiResponse<PageResponse<QuoteListResponse>>> getQuoteList(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            String contractorName,

            @RequestParam(defaultValue = "LATEST")
            QuoteSortType sort,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        LocalDate resolvedStartDate = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1).minusMonths(1);
        LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();

        GetQuoteListQuery query = new GetQuoteListQuery(
                resolvedStartDate,
                resolvedEndDate,
                contractorName,
                sort,
                page,
                size
        );

        PageResponse<QuoteListResponse> result = getQuoteListUseCase.getQuoteList(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
