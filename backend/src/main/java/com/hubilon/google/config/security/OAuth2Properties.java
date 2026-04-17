package com.hubilon.google.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2Properties {

    private String allowedRedirectOrigins;

    public List<String> getAllowedOriginsList() {
        if (allowedRedirectOrigins == null || allowedRedirectOrigins.isBlank()) {
            return List.of("http://localhost:5173");
        }
        return List.of(allowedRedirectOrigins.split(","))
                .stream()
                .map(String::trim)
                .toList();
    }
}
