package com.hubilon.google.modules.quotesheet.adapter.out.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class TokenRefreshHelper {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    /**
     * Refresh Token을 사용해 새 Access Token을 발급한다.
     * 실패 시 GOOGLE_AUTH_EXPIRED ServiceException을 throw한다.
     * 보안: refreshToken 값은 로그에 출력하지 않는다.
     */
    public String refresh(String refreshToken) {
        try {
            GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    refreshToken,
                    clientId,
                    clientSecret
            ).execute();

            return response.getAccessToken();
        } catch (IOException e) {
            log.warn("Google Token refresh failed: {}", e.getMessage());
            throw new ServiceException(ErrorCode.GOOGLE_AUTH_EXPIRED, e);
        }
    }
}
