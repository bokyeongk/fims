package com.hubilon.google.modules.quote.application.service;

import com.hubilon.google.common.response.PageResponse;
import com.hubilon.google.modules.quote.adapter.in.web.QuoteListResponse;
import com.hubilon.google.modules.quote.adapter.in.web.QuoteSortType;
import com.hubilon.google.modules.quote.application.port.in.GetQuoteListQuery;
import com.hubilon.google.modules.quote.application.port.in.GetQuoteListUseCase;
import com.hubilon.google.modules.quote.application.port.out.QuoteRepository;
import com.hubilon.google.modules.quote.application.port.out.QuoteSearchCondition;
import com.hubilon.google.modules.quote.domain.model.Quote;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService implements GetQuoteListUseCase {

    private final QuoteRepository quoteRepository;

    @Override
    public PageResponse<QuoteListResponse> getQuoteList(GetQuoteListQuery query) {
        Sort sort = query.sort() == QuoteSortType.LATEST
                ? Sort.by(Sort.Direction.DESC, "contractDate")
                : Sort.by(Sort.Direction.ASC, "contractDate");

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        QuoteSearchCondition condition = new QuoteSearchCondition(
                query.startDate(),
                query.endDate(),
                query.contractorName()
        );

        Page<Quote> quotePage = quoteRepository.findAll(condition, pageable);

        return PageResponse.of(quotePage.map(QuoteListResponse::from));
    }
}
