package com.hubilon.google.modules.user.adapter.out.persistence;

import com.hubilon.google.modules.user.domain.model.OAuthProvider;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSocialAccountJpaRepository extends JpaRepository<UserSocialAccount, Long> {

    Optional<UserSocialAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);

    List<UserSocialAccount> findByUserId(Long userId);

    Optional<UserSocialAccount> findByUserIdAndProvider(Long userId, OAuthProvider provider);

    int countByUserId(Long userId);
}
