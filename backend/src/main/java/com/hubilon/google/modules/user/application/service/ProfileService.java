package com.hubilon.google.modules.user.application.service;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.modules.user.application.port.in.EmailVerificationUseCase;
import com.hubilon.google.modules.user.application.port.in.ProfileUseCase;
import com.hubilon.google.modules.user.application.port.in.SignatureUseCase;
import com.hubilon.google.modules.user.application.port.out.UserRepository;
import com.hubilon.google.modules.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService implements ProfileUseCase, EmailVerificationUseCase, SignatureUseCase {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^010-\\d{4}-\\d{4}$");
    private static final int SIGNATURE_MAX_BYTES = 279_552; // 273 KB
    private static final int VERIFY_CODE_LENGTH = 6;
    private static final int VERIFY_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPT_COUNT = 5;

    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, VerifyEntry> verifyStore = new ConcurrentHashMap<>();

    // ──────────────────────────────────────────────────
    // ProfileUseCase
    // ──────────────────────────────────────────────────

    @Override
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UpdateProfileCommand command) {
        if (command.phone() != null && !command.phone().isBlank()) {
            if (!PHONE_PATTERN.matcher(command.phone()).matches()) {
                throw new ServiceException(ErrorCode.PHONE_FORMAT_INVALID);
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        user.updateProfile(command.name(), command.phone(), command.email());
        return userRepository.save(user);
    }

    // ──────────────────────────────────────────────────
    // EmailVerificationUseCase
    // ──────────────────────────────────────────────────

    @Override
    public boolean checkEmail(String email, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        if (email.equalsIgnoreCase(currentUser.getEmail())) {
            return true;
        }

        return !userRepository.existsByEmail(email);
    }

    @Override
    public void sendVerifyEmail(String email) {
        String code = generateNumericCode();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(VERIFY_TTL_MINUTES);

        verifyStore.put(email, new VerifyEntry(code, expireAt, 0));

        // TODO: integrate actual email sending (e.g., JavaMailSender or external SMTP)
        log.info("[EmailVerify] Verification code for {}: {}", email, code);
    }

    @Override
    public void confirmVerifyEmail(String email, String code) {
        VerifyEntry entry = verifyStore.get(email);

        if (entry == null || entry.expireAt().isBefore(LocalDateTime.now())) {
            verifyStore.remove(email);
            throw new ServiceException(ErrorCode.EMAIL_VERIFY_EXPIRED);
        }

        if (entry.attemptCount() >= MAX_ATTEMPT_COUNT) {
            verifyStore.remove(email);
            throw new ServiceException(ErrorCode.EMAIL_VERIFY_EXCEEDED);
        }

        if (!entry.code().equals(code)) {
            verifyStore.put(email, new VerifyEntry(entry.code(), entry.expireAt(), entry.attemptCount() + 1));
            throw new ServiceException(ErrorCode.EMAIL_VERIFY_INVALID);
        }

        verifyStore.remove(email);
    }

    // ──────────────────────────────────────────────────
    // SignatureUseCase
    // ──────────────────────────────────────────────────

    @Override
    @Transactional
    public String saveSignature(Long userId, String signatureData) {
        if (signatureData != null) {
            int byteLength = signatureData.getBytes(StandardCharsets.UTF_8).length;
            if (byteLength > SIGNATURE_MAX_BYTES) {
                throw new ServiceException(ErrorCode.SIGNATURE_TOO_LARGE);
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        user.updateSignature(signatureData);
        userRepository.save(user);
        return signatureData;
    }

    @Override
    @Transactional
    public void deleteSignature(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        user.clearSignature();
        userRepository.save(user);
    }

    // ──────────────────────────────────────────────────
    // Internals
    // ──────────────────────────────────────────────────

    private String generateNumericCode() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private record VerifyEntry(String code, LocalDateTime expireAt, int attemptCount) {}
}
