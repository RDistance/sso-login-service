package com.sso.login.util;

import com.sso.login.config.SsoProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 工具类 — 生成/解析 accessToken 和 refreshToken
 */
@Component
public class JwtUtil {

    private final SsoProperties ssoProperties;
    private final SecretKey signingKey;

    public JwtUtil(SsoProperties ssoProperties) {
        this.ssoProperties = ssoProperties;
        // 密钥长度需 >= 256 bits (32 bytes) for HS256
        this.signingKey = Keys.hmacShaKeyFor(
            ssoProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 生成 accessToken
     */
    public String generateAccessToken(Long userId, String username, String appId) {
        long expirationMs = ssoProperties.getJwt().getAccessTokenExpiration();
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("appId", appId)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 生成 refreshToken
     */
    public String generateRefreshToken(Long userId, String appId) {
        long expirationMs = ssoProperties.getJwt().getRefreshTokenExpiration();
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("appId", appId)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 解析 Token，返回 Claims；失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中提取 userId
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * accessToken 过期时间（秒），用于响应体
     */
    public long getAccessTokenExpiresInSeconds() {
        return ssoProperties.getJwt().getAccessTokenExpiration() / 1000;
    }
}
