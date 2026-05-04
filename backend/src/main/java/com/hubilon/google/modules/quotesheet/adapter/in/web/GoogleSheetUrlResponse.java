package com.hubilon.google.modules.quotesheet.adapter.in.web;

public record GoogleSheetUrlResponse(
        String sheetUrl
) {
    public static GoogleSheetUrlResponse of(String sheetUrl) {
        return new GoogleSheetUrlResponse(sheetUrl);
    }
}
