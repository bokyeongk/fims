package com.hubilon.google.modules.user.application.port.in;

public interface LoginUserUseCase {

    LoginResult login(LoginCommand command);

    record LoginCommand(
            String email,
            String password
    ) {}

    record LoginResult(
            String accessToken,
            String tokenType,
            Long userId,
            String email,
            String name,
            String role
    ) {}
}
