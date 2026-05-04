package com.hubilon.google.modules.quotesheet.application.port.in;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;

public interface CreateQuoteSheetUseCase {

    QuoteSheet create(CreateQuoteSheetCommand command);
}
