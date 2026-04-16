package com.hubilon.google.modules.user.application.port.in;

import com.hubilon.google.modules.user.domain.model.User;

public interface RegisterUserUseCase {

    User register(RegisterCommand command);

    record RegisterCommand(
            String email,
            String name,
            String password
    ) {}
}
