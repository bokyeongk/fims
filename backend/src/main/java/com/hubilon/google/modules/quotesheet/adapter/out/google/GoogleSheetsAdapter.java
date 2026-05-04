package com.hubilon.google.modules.quotesheet.adapter.out.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.modules.quotesheet.application.port.out.GoogleSheetsPort;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteItem;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class GoogleSheetsAdapter implements GoogleSheetsPort {

    private static final String APPLICATION_NAME = "FIMS";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String createSheet(QuoteSheet quoteSheet, List<QuoteItem> items, String accessToken) {
        try {
            Sheets sheetsService = buildSheetsService(accessToken);

            String spreadsheetTitle = "인테리어 필름 견적서 - " + quoteSheet.getQuoteNumber();
            Spreadsheet spreadsheet = new Spreadsheet()
                    .setProperties(new SpreadsheetProperties().setTitle(spreadsheetTitle));

            Spreadsheet created = sheetsService.spreadsheets()
                    .create(spreadsheet)
                    .execute();

            String spreadsheetId = created.getSpreadsheetId();
            String sheetName = created.getSheets().get(0).getProperties().getTitle();

            List<ValueRange> data = buildSheetData(quoteSheet, items, sheetName);

            BatchUpdateValuesRequest batchRequest = new BatchUpdateValuesRequest()
                    .setValueInputOption("USER_ENTERED")
                    .setData(data);

            sheetsService.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchRequest)
                    .execute();

            return spreadsheetId;

        } catch (IOException | GeneralSecurityException e) {
            log.error("Google Sheets 생성 실패: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("insufficientPermissions")) {
                throw new ServiceException(ErrorCode.GOOGLE_SCOPE_INSUFFICIENT, e);
            }
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public boolean isSheetValid(String spreadsheetId, String accessToken) {
        try {
            Drive driveService = buildDriveService(accessToken);
            driveService.files().get(spreadsheetId).execute();
            return true;
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                return false;
            }
            log.warn("Drive 파일 유효성 확인 중 오류: {}", e.getMessage());
            return false;
        } catch (GeneralSecurityException e) {
            log.warn("Drive 서비스 초기화 오류: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteSheet(String spreadsheetId, String accessToken) {
        try {
            Drive driveService = buildDriveService(accessToken);
            driveService.files().delete(spreadsheetId).execute();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Drive 파일 삭제 실패. spreadsheetId={}, error={}", spreadsheetId, e.getMessage());
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    private Sheets buildSheetsService(String accessToken) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.create(
                new AccessToken(accessToken, new Date(Long.MAX_VALUE))
        );
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }

    private Drive buildDriveService(String accessToken) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.create(
                new AccessToken(accessToken, new Date(Long.MAX_VALUE))
        );
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }

    private List<ValueRange> buildSheetData(QuoteSheet quoteSheet, List<QuoteItem> items, String sheetName) {
        List<ValueRange> data = new ArrayList<>();

        // 행1: 제목
        data.add(range(sheetName, "A1", List.of(List.of("인테리어 필름 견적서"))));

        // 행2~4: 기본 정보
        String quoteDate = quoteSheet.getQuoteDate() != null
                ? quoteSheet.getQuoteDate().format(DATE_FORMATTER)
                : "";
        data.add(range(sheetName, "A2:B4", List.of(
                List.of("견적번호", quoteSheet.getQuoteNumber()),
                List.of("견적일자", quoteDate),
                List.of("계약자", nvl(quoteSheet.getContractorName()))
        )));

        // 행6: 헤더
        data.add(range(sheetName, "A6:H6", List.of(
                List.of("번호", "품명", "규격", "구분", "수량", "단위", "단가", "금액")
        )));

        // 행7~N: 품목
        int startRow = 7;
        List<List<Object>> itemRows = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            QuoteItem item = items.get(i);
            int rowNum = startRow + i;
            String amountFormula = String.format("=E%d*G%d", rowNum, rowNum);
            itemRows.add(List.of(
                    i + 1,
                    nvl(item.getItemName()),
                    nvl(item.getSpec()),
                    nvl(item.getCategory()),
                    item.getQuantity() != null ? item.getQuantity() : 0,
                    nvl(item.getUnit()),
                    item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO,
                    amountFormula
            ));
        }
        if (!itemRows.isEmpty()) {
            data.add(range(sheetName, "A7:H" + (startRow + items.size() - 1), itemRows));
        }

        // 합계 행
        int lastItemRow = startRow + items.size() - 1;
        int sumRow = lastItemRow + 1;
        String sumFormula = String.format("=SUM(H7:H%d)", lastItemRow);
        data.add(range(sheetName, "A" + sumRow + ":H" + sumRow, List.of(
                List.of("합계", "", "", "", "", "", "", sumFormula)
        )));

        // 비고 행
        int noteRow = sumRow + 1;
        data.add(range(sheetName, "A" + noteRow + ":B" + noteRow, List.of(
                List.of("비고", nvl(quoteSheet.getNote()))
        )));

        return data;
    }

    private ValueRange range(String sheetName, String a1Notation, List<List<Object>> values) {
        return new ValueRange()
                .setRange(sheetName + "!" + a1Notation)
                .setValues(values);
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }
}
