package com.hubilon.google.modules.quotesheet.application.service;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.modules.quotesheet.adapter.out.google.TokenRefreshHelper;
import com.hubilon.google.modules.quotesheet.application.port.in.GenerateGoogleSheetUseCase;
import com.hubilon.google.modules.quotesheet.application.port.out.GoogleSheetsPort;
import com.hubilon.google.modules.quotesheet.application.port.out.QuoteSheetRepository;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteItem;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;
import com.hubilon.google.modules.user.application.port.out.UserSocialAccountRepository;
import com.hubilon.google.modules.user.domain.model.OAuthProvider;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetOrchestrationService implements GenerateGoogleSheetUseCase {

    private static final String SHEET_URL_PREFIX = "https://docs.google.com/spreadsheets/d/";

    private final QuoteSheetRepository quoteSheetRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;
    private final GoogleSheetsPort googleSheetsPort;
    private final TokenRefreshHelper tokenRefreshHelper;

    @Transactional
    @Override
    public String generateOrGetSheetUrl(Long quoteSheetId, Long userId) {
        // 1. FOR UPDATE 락으로 조회 (동시 중복 생성 방지)
        QuoteSheet quoteSheet = findWithLock(quoteSheetId);

        // 2. 소유권 검증
        if (!quoteSheet.getUserId().equals(userId)) {
            throw new ServiceException(ErrorCode.QUOTE_SHEET_FORBIDDEN);
        }

        // 3. sheetUrl이 이미 있으면 유효성 검사
        if (quoteSheet.getSheetUrl() != null) {
            String spreadsheetId = extractSpreadsheetId(quoteSheet.getSheetUrl());
            String accessToken = resolveAccessToken(userId);

            if (googleSheetsPort.isSheetValid(spreadsheetId, accessToken)) {
                return quoteSheet.getSheetUrl();
            }

            // 무효화: sheetUrl 초기화
            clearSheetUrl(quoteSheetId);
        }

        // 4. 새 Google Sheet 생성
        return createAndPersistSheet(quoteSheetId, userId, quoteSheet);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public QuoteSheet findWithLock(Long quoteSheetId) {
        return quoteSheetRepository.findByIdWithLock(quoteSheetId)
                .orElseThrow(() -> new ServiceException(ErrorCode.QUOTE_SHEET_NOT_FOUND));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearSheetUrl(Long quoteSheetId) {
        quoteSheetRepository.updateSheetUrl(quoteSheetId, null);
    }

    private String createAndPersistSheet(Long quoteSheetId, Long userId, QuoteSheet quoteSheet) {
        String accessToken = resolveAccessToken(userId);
        List<QuoteItem> items = quoteSheet.getItems();

        // Google API 호출 (트랜잭션 외부)
        String spreadsheetId = googleSheetsPort.createSheet(quoteSheet, items, accessToken);
        String sheetUrl = SHEET_URL_PREFIX + spreadsheetId;

        // DB 업데이트 (별도 트랜잭션)
        try {
            persistSheetUrl(quoteSheetId, sheetUrl);
        } catch (Exception e) {
            log.error("시트 URL DB 저장 실패, 보상 트랜잭션 시작. spreadsheetId={}", spreadsheetId);
            compensate(spreadsheetId, accessToken);
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }

        return sheetUrl;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistSheetUrl(Long quoteSheetId, String sheetUrl) {
        quoteSheetRepository.updateSheetUrl(quoteSheetId, sheetUrl);
    }

    private void compensate(String spreadsheetId, String accessToken) {
        try {
            googleSheetsPort.deleteSheet(spreadsheetId, accessToken);
            log.info("보상 성공: 고아 시트 삭제 완료. spreadsheetId={}", spreadsheetId);
        } catch (Exception ex) {
            log.error("고아 시트 발생. spreadsheetId={}", spreadsheetId);
        }
    }

    private String resolveAccessToken(Long userId) {
        UserSocialAccount socialAccount = userSocialAccountRepository
                .findByUserIdAndProvider(userId, OAuthProvider.GOOGLE)
                .orElseThrow(() -> new ServiceException(ErrorCode.GOOGLE_AUTH_EXPIRED));

        String accessToken = socialAccount.getAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            // Access Token 없으면 Refresh Token으로 재발급
            String refreshToken = socialAccount.getRefreshToken();
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new ServiceException(ErrorCode.GOOGLE_AUTH_EXPIRED);
            }
            accessToken = tokenRefreshHelper.refresh(refreshToken);
            socialAccount.updateTokens(accessToken, refreshToken);
            userSocialAccountRepository.save(socialAccount);
        }

        return accessToken;
    }

    private String extractSpreadsheetId(String sheetUrl) {
        if (sheetUrl == null || !sheetUrl.startsWith(SHEET_URL_PREFIX)) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return sheetUrl.substring(SHEET_URL_PREFIX.length());
    }
}
