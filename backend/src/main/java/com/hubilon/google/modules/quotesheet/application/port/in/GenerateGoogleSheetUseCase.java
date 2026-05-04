package com.hubilon.google.modules.quotesheet.application.port.in;

public interface GenerateGoogleSheetUseCase {

    String generateOrGetSheetUrl(Long quoteSheetId, Long userId);
}
