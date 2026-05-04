package com.hubilon.google.modules.quotesheet.application.port.in;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetQuoteSheetListUseCase {

    Page<QuoteSheet> getList(Long userId, Pageable pageable);
}
