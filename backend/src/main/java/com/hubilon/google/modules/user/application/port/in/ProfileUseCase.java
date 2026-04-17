package com.hubilon.google.modules.user.application.port.in;

import com.hubilon.google.modules.user.domain.model.User;

public interface ProfileUseCase {

    User getProfile(Long userId);

    User updateProfile(Long userId, UpdateProfileCommand command);

    record UpdateProfileCommand(String name, String phone, String email) {}
}
