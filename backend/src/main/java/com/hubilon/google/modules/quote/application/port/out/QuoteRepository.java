package com.hubilon.google.modules.quote.application.port.out;

import com.hubilon.google.modules.quote.domain.model.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuoteRepository {

    Page<Quote> findAll(QuoteSearchCondition condition, Pageable pageable);
}
