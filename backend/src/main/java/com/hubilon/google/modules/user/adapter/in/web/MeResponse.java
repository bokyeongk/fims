package com.hubilon.google.modules.user.adapter.in.web;

import com.hubilon.google.modules.user.domain.model.User;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;

import java.util.List;

public record MeResponse(
        Long id,
        String email,
        String name,
        List<String> providers
) {
    public static MeResponse from(User user, List<UserSocialAccount> socialAccounts) {
        List<String> providers = socialAccounts.stream()
                .map(sa -> sa.getProvider().name())
                .toList();
        return new MeResponse(user.getId(), user.getEmail(), user.getName(), providers);
    }
}
