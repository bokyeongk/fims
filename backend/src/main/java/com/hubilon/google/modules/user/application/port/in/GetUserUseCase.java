package com.hubilon.google.modules.user.application.port.in;

import com.hubilon.google.modules.user.domain.model.User;

import java.util.List;

public interface GetUserUseCase {

    User getUserById(Long id);

    User getUserByEmail(String email);

    List<User> getAllUsers();
}
