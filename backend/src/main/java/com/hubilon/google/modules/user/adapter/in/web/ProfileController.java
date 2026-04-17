package com.hubilon.google.modules.user.adapter.in.web;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.common.response.ApiResponse;
import com.hubilon.google.config.multiLanguage.MessageProvider;
import com.hubilon.google.modules.user.application.port.in.EmailVerificationUseCase;
import com.hubilon.google.modules.user.application.port.in.ProfileUseCase;
import com.hubilon.google.modules.user.application.port.in.SignatureUseCase;
import com.hubilon.google.modules.user.application.port.out.UserSocialAccountRepository;
import com.hubilon.google.modules.user.domain.model.User;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 관리 API")
public class ProfileController {

    private final ProfileUseCase profileUseCase;
    private final EmailVerificationUseCase emailVerificationUseCase;
    private final SignatureUseCase signatureUseCase;
    private final UserSocialAccountRepository userSocialAccountRepository;
    private final MessageProvider messageProvider;

    // ──────────────────────────────────────────────────
    // Profile
    // ──────────────────────────────────────────────────

    @GetMapping("/profile")
    @Operation(summary = "프로필 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        Long userId = resolveUserId();
        User user = profileUseCase.getProfile(userId);
        List<UserSocialAccount> socialAccounts = userSocialAccountRepository.findByUserId(userId);

        String message = messageProvider.getMessage("profile.get.success");
        return ResponseEntity.ok(ApiResponse.success(message, ProfileResponse.from(user, socialAccounts)));
    }

    @PutMapping("/profile")
    @Operation(summary = "프로필 수정", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request) {
        Long userId = resolveUserId();
        User user = profileUseCase.updateProfile(userId,
                new ProfileUseCase.UpdateProfileCommand(request.name(), request.phone(), request.email()));
        List<UserSocialAccount> socialAccounts = userSocialAccountRepository.findByUserId(userId);

        String message = messageProvider.getMessage("profile.update.success");
        return ResponseEntity.ok(ApiResponse.success(message, ProfileResponse.from(user, socialAccounts)));
    }

    // ──────────────────────────────────────────────────
    // Email Verification
    // ──────────────────────────────────────────────────

    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 확인", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(
            @RequestParam @Email String email) {
        Long userId = resolveUserId();
        boolean available = emailVerificationUseCase.checkEmail(email, userId);

        if (!available) {
            String message = messageProvider.getMessage("email.already.exists");
            return ResponseEntity.ok(ApiResponse.success(message, Map.of("available", false)));
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of("available", true)));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증 코드 발송", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> sendVerifyEmail(
            @RequestBody @Valid SendVerifyEmailRequest request) {
        emailVerificationUseCase.sendVerifyEmail(request.email());
        String message = messageProvider.getMessage("email.verify.sent");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/verify-email/confirm")
    @Operation(summary = "이메일 인증 코드 확인", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> confirmVerifyEmail(
            @RequestBody @Valid ConfirmVerifyEmailRequest request) {
        emailVerificationUseCase.confirmVerifyEmail(request.email(), request.code());
        String message = messageProvider.getMessage("email.verify.success");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // ──────────────────────────────────────────────────
    // Signature
    // ──────────────────────────────────────────────────

    @PutMapping("/profile/signature")
    @Operation(summary = "서명 저장", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, String>>> saveSignature(
            @RequestBody @Valid SaveSignatureRequest request) {
        Long userId = resolveUserId();
        String saved = signatureUseCase.saveSignature(userId, request.signatureData());
        String message = messageProvider.getMessage("signature.save.success");
        return ResponseEntity.ok(ApiResponse.success(message, Map.of("signatureData", saved)));
    }

    @DeleteMapping("/profile/signature")
    @Operation(summary = "서명 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteSignature() {
        Long userId = resolveUserId();
        signatureUseCase.deleteSignature(userId);
        String message = messageProvider.getMessage("signature.delete.success");
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // ──────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }
        return Long.parseLong((String) authentication.getPrincipal());
    }

    // ──────────────────────────────────────────────────
    // Request DTOs
    // ──────────────────────────────────────────────────

    public record UpdateProfileRequest(String name, String phone, String email) {}

    public record SendVerifyEmailRequest(@NotBlank @Email String email) {}

    public record ConfirmVerifyEmailRequest(@NotBlank @Email String email, @NotBlank String code) {}

    public record SaveSignatureRequest(@NotBlank String signatureData) {}
}
