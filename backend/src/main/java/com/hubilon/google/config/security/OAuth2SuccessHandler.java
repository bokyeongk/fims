package com.hubilon.google.config.security;

import com.hubilon.google.modules.user.application.port.out.UserRepository;
import com.hubilon.google.modules.user.application.port.out.UserSocialAccountRepository;
import com.hubilon.google.modules.user.domain.model.OAuthProvider;
import com.hubilon.google.modules.user.domain.model.User;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String DEFAULT_REDIRECT_ORIGIN = "http://localhost:5173";
    private static final String COOKIE_NAME = "accessToken";

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final OAuth2Properties oAuth2Properties;
    private final UserRepository userRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long incomingUserId = (Long) attributes.get("_userId");
        String role = resolveRole(authentication);

        String allowedOrigin = resolveAllowedOrigin(request);

        // Check if a user is already authenticated (social account linking flow)
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth instanceof UsernamePasswordAuthenticationToken
                && existingAuth.isAuthenticated()
                && existingAuth.getPrincipal() instanceof String principalStr
                && !principalStr.equals("anonymousUser")) {

            Long existingUserId = Long.parseLong(principalStr);

            // Determine provider and providerId from the incoming OAuth2 user
            OAuthProvider provider = resolveProvider(attributes);
            String providerId = resolveProviderId(provider, attributes);
            String providerEmail = (String) attributes.get("_userEmail");

            boolean alreadyLinked = userSocialAccountRepository
                    .findByProviderAndProviderId(provider, providerId)
                    .isPresent();

            if (!alreadyLinked) {
                User linkedUser = userRepository.findById(existingUserId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + existingUserId));
                UserSocialAccount newAccount = UserSocialAccount.of(
                        linkedUser, provider, providerId, providerEmail);
                userSocialAccountRepository.save(newAccount);
                log.info("Social account linked: userId={}, provider={}", existingUserId, provider);
            } else {
                log.info("Social account already linked: userId={}, provider={}", existingUserId, provider);
            }

            String redirectUrl = allowedOrigin + "/settings/profile";
            log.info("Social link complete: userId={}, redirectTo={}", existingUserId, redirectUrl);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            return;
        }

        // Normal login flow
        String accessToken = jwtProvider.generateAccessToken(incomingUserId.toString(), role);

        int maxAgeSeconds = (int) (jwtProperties.getAccessTokenExpiry() / 1000);
        Cookie cookie = buildAccessTokenCookie(accessToken, maxAgeSeconds);
        response.addCookie(cookie);

        String redirectUrl = allowedOrigin + "/oauth2/callback";
        log.info("OAuth2 login success: userId={}, redirectTo={}", incomingUserId, redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private OAuthProvider resolveProvider(Map<String, Object> attributes) {
        // Determine provider from attribute fingerprint
        if (attributes.containsKey("sub") && attributes.containsKey("email")) {
            return OAuthProvider.GOOGLE;
        } else if (attributes.containsKey("response")) {
            return OAuthProvider.NAVER;
        } else if (attributes.containsKey("kakao_account")) {
            return OAuthProvider.KAKAO;
        }
        return OAuthProvider.GOOGLE;
    }

    @SuppressWarnings("unchecked")
    private String resolveProviderId(OAuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> (String) attributes.get("sub");
            case NAVER -> {
                Map<String, Object> res = (Map<String, Object>) attributes.get("response");
                yield res != null ? (String) res.get("id") : "";
            }
            case KAKAO -> String.valueOf(attributes.get("id"));
            default -> "";
        };
    }

    private Cookie buildAccessTokenCookie(String token, int maxAgeSeconds) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

    private String resolveRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
    }

    private String resolveAllowedOrigin(HttpServletRequest request) {
        List<String> allowedOrigins = oAuth2Properties.getAllowedOriginsList();
        String origin = request.getHeader("Origin");
        if (origin != null && allowedOrigins.contains(origin)) {
            return origin;
        }
        String referer = request.getHeader("Referer");
        if (referer != null) {
            for (String allowed : allowedOrigins) {
                if (referer.startsWith(allowed)) {
                    return allowed;
                }
            }
        }
        return allowedOrigins.isEmpty() ? DEFAULT_REDIRECT_ORIGIN : allowedOrigins.get(0);
    }
}
