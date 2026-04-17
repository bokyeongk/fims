package com.hubilon.google.modules.user.application.port.out;

import com.hubilon.google.modules.user.domain.model.OAuthProvider;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;

import java.util.List;
import java.util.Optional;

public interface UserSocialAccountRepository {

    Optional<UserSocialAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);

    UserSocialAccount save(UserSocialAccount account);

    List<UserSocialAccount> findByUserId(Long userId);

    Optional<UserSocialAccount> findByUserIdAndProvider(Long userId, OAuthProvider provider);

    int countByUserId(Long userId);
}
