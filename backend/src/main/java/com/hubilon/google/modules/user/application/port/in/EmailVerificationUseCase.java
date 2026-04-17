package com.hubilon.google.modules.user.application.port.in;

public interface EmailVerificationUseCase {

    boolean checkEmail(String email, Long currentUserId);

    void sendVerifyEmail(String email);

    void confirmVerifyEmail(String email, String code);
}
