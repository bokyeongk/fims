package com.hubilon.google.modules.user.adapter.out.persistence;

import com.hubilon.google.modules.user.application.port.out.UserSocialAccountRepository;
import com.hubilon.google.modules.user.domain.model.OAuthProvider;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserSocialAccountRepositoryImpl implements UserSocialAccountRepository {

    private final UserSocialAccountJpaRepository userSocialAccountJpaRepository;

    @Override
    public Optional<UserSocialAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId) {
        return userSocialAccountJpaRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public UserSocialAccount save(UserSocialAccount account) {
        return userSocialAccountJpaRepository.save(account);
    }

    @Override
    public List<UserSocialAccount> findByUserId(Long userId) {
        return userSocialAccountJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserSocialAccount> findByUserIdAndProvider(Long userId, OAuthProvider provider) {
        return userSocialAccountJpaRepository.findByUserIdAndProvider(userId, provider);
    }

    @Override
    public int countByUserId(Long userId) {
        return userSocialAccountJpaRepository.countByUserId(userId);
    }
}
