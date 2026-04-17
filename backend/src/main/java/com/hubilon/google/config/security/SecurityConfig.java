package com.hubilon.google.config.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2Properties oAuth2Properties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // oauth2Login이 활성화되면 Spring Security가 LoginUrlAuthenticationEntryPoint를
                // 기본 AuthenticationEntryPoint로 등록한다. 이를 override하지 않으면 미인증 API
                // 요청이 localhost:8080/login으로 리다이렉트되어 CORS가 발생한다.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("[Security] 미인증 요청 차단: uri={}, message={}",
                                    request.getRequestURI(), authException.getMessage());
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                // IF_REQUIRED: OAuth2 state 파라미터 검증을 위해 세션을 허용한다.
                // STATELESS로 설정하면 콜백 시 state를 세션에서 꺼낼 수 없어 인증 실패가 발생한다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/login/oauth2/code/**",
                                "/oauth2/authorization/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/users/register",
                                "/api/v1/users/login",
                                "/api/v1/auth/logout"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/quotes",
                                "/api/v1/auth/me"
                        ).permitAll()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("[OAuth2] 로그인 실패: uri={}, exceptionClass={}, message={}",
                                    request.getRequestURI(),
                                    exception.getClass().getSimpleName(),
                                    exception.getMessage(),
                                    exception);
                            String frontendOrigin = oAuth2Properties.getAllowedOriginsList().isEmpty()
                                    ? "http://localhost:5173"
                                    : oAuth2Properties.getAllowedOriginsList().get(0);
                            response.sendRedirect(frontendOrigin + "/login?error");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
