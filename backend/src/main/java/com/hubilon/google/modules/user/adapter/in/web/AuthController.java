package com.hubilon.google.modules.user.adapter.in.web;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.common.response.ApiResponse;
import com.hubilon.google.config.multiLanguage.MessageProvider;
import com.hubilon.google.modules.user.application.port.in.GetUserUseCase;
import com.hubilon.google.modules.user.application.port.out.UserSocialAccountRepository;
import com.hubilon.google.modules.user.domain.model.User;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final GetUserUseCase getUserUseCase;
    private final UserSocialAccountRepository userSocialAccountRepository;
    private final MessageProvider messageProvider;

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "accessToken 쿠키를 만료시켜 로그아웃합니다.")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "JWT 쿠키 또는 Bearer 토큰 기반으로 현재 로그인 사용자 정보를 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MeResponse>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }

        String userIdStr = (String) authentication.getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        User user = getUserUseCase.getUserById(userId);
        List<UserSocialAccount> socialAccounts = userSocialAccountRepository.findByUserId(userId);

        String message = messageProvider.getMessage("user.oauth2.me.success");
        return ResponseEntity.ok(ApiResponse.success(message, MeResponse.from(user, socialAccounts)));
    }
}
