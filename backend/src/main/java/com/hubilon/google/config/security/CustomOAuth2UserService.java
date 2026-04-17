package com.hubilon.google.config.security;

import com.hubilon.google.modules.user.application.port.out.UserRepository;
import com.hubilon.google.modules.user.application.port.out.UserSocialAccountRepository;
import com.hubilon.google.modules.user.domain.model.OAuthProvider;
import com.hubilon.google.modules.user.domain.model.User;
import com.hubilon.google.modules.user.domain.model.UserSocialAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        OAuthProvider provider = OAuthProvider.valueOf(registrationId);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthUserInfo userInfo = extractUserInfo(provider, attributes);

        User user = userSocialAccountRepository.findByProviderAndProviderId(provider, userInfo.providerId())
                .map(UserSocialAccount::getUser)
                .orElseGet(() -> {
                    User newUser = User.ofSocial(userInfo.name(), userInfo.email());
                    User savedUser = userRepository.save(newUser);
                    UserSocialAccount socialAccount = UserSocialAccount.of(savedUser, provider, userInfo.providerId(), userInfo.email());
                    userSocialAccountRepository.save(socialAccount);
                    return savedUser;
                });

        log.info("OAuth2 login: provider={}, userId={}, email={}", provider, user.getId(), user.getEmail());

        Map<String, Object> enrichedAttributes = new HashMap<>(attributes);
        enrichedAttributes.put("_userId", user.getId());
        enrichedAttributes.put("_userEmail", user.getEmail());

        return new DefaultOAuth2User(
                List.of(new OAuth2UserAuthority("ROLE_" + user.getRole().name(), enrichedAttributes)),
                enrichedAttributes,
                nameAttributeKey
        );
    }

    @SuppressWarnings("unchecked")
    private OAuthUserInfo extractUserInfo(OAuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> {
                String providerId = (String) attributes.get("sub");
                String email = (String) attributes.get("email");
                String name = (String) attributes.get("name");
                yield new OAuthUserInfo(providerId, email, name);
            }
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                String providerId = (String) response.get("id");
                String email = (String) response.get("email");
                String name = (String) response.get("name");
                yield new OAuthUserInfo(providerId, email, name);
            }
            case KAKAO -> {
                String providerId = String.valueOf(attributes.get("id"));
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = kakaoAccount != null
                        ? (Map<String, Object>) kakaoAccount.get("profile")
                        : Map.of();
                String name = profile != null ? (String) profile.get("nickname") : "kakao_" + providerId;
                String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
                yield new OAuthUserInfo(providerId, email, name);
            }
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        };
    }

    private record OAuthUserInfo(String providerId, String email, String name) {}
}
