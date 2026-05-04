package com.hubilon.google.modules.quotesheet.application.port.out;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteItem;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;

import java.util.List;

public interface GoogleSheetsPort {

    /**
     * Google Sheets API를 통해 견적서 스프레드시트를 생성하고 spreadsheetId를 반환한다.
     */
    String createSheet(QuoteSheet quoteSheet, List<QuoteItem> items, String accessToken);

    /**
     * Drive API로 파일 존재 여부를 확인한다. 파일이 없거나 접근 불가 시 false를 반환한다.
     */
    boolean isSheetValid(String spreadsheetId, String accessToken);

    /**
     * Drive API로 파일을 삭제한다.
     */
    void deleteSheet(String spreadsheetId, String accessToken);
}
