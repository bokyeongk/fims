package com.hubilon.google.modules.user.domain.model;

import com.hubilon.google.common.converter.TokenEncryptedStringConverter;
import com.hubilon.google.common.entity.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_social_accounts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserSocialAccount extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(nullable = true, length = 255)
    private String email;

    @Convert(converter = TokenEncryptedStringConverter.class)
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Convert(converter = TokenEncryptedStringConverter.class)
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static UserSocialAccount of(User user, OAuthProvider provider, String providerId, String email) {
        return UserSocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .build();
    }
}
