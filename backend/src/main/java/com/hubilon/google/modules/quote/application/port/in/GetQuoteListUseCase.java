package com.hubilon.google.modules.quote.application.port.in;

import com.hubilon.google.common.response.PageResponse;
import com.hubilon.google.modules.quote.adapter.in.web.QuoteListResponse;

public interface GetQuoteListUseCase {

    PageResponse<QuoteListResponse> getQuoteList(GetQuoteListQuery query);
}
