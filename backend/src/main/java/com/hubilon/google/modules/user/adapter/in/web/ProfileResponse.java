package com.hubilon.google.modules.user.adapter.in.web;

import com.hubilon.google.modules.user.domain.model.User;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;

import java.util.List;

public record ProfileResponse(
        Long id,
        String name,
        String phone,
        String email,
        String signatureData,
        List<SocialAccountInfo> socialAccounts
) {
    public record SocialAccountInfo(String provider, String email) {}

    public static ProfileResponse from(User user, List<UserSocialAccount> socialAccounts) {
        List<SocialAccountInfo> accountInfos = socialAccounts.stream()
                .map(sa -> new SocialAccountInfo(sa.getProvider().name(), sa.getEmail()))
                .toList();
        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getSignatureData(),
                accountInfos
        );
    }
}
