package com.hubilon.google.modules.user.adapter.in.web;

import com.hubilon.google.modules.user.application.port.in.LoginUserUseCase;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String name,
        String role
) {
    public static LoginResponse from(LoginUserUseCase.LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.userId(),
                result.email(),
                result.name(),
                result.role()
        );
    }
}
