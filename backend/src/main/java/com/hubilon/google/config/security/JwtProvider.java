package com.hubilon.google.config.security;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String subject, String role) {
        return buildToken(subject, role, jwtProperties.getAccessTokenExpiry());
    }

    public String generateRefreshToken(String subject) {
        return buildToken(subject, null, jwtProperties.getRefreshTokenExpiry());
    }

    private String buildToken(String subject, String role, long expiry) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expiry);

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey());

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new ServiceException(ErrorCode.JWT_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ServiceException(ErrorCode.JWT_INVALID);
        }
    }

    public String getSubject(String token) {
        return validateToken(token).getSubject();
    }

    public String getRole(String token) {
        return validateToken(token).get("role", String.class);
    }
}
