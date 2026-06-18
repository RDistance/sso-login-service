package com.sso.login.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SSO 自定义配置项 — 对应 application.yaml 中的 sso.* 前缀
 */
@Component
@ConfigurationProperties(prefix = "sso")
public class SsoProperties {

    private Jwt jwt = new Jwt();
    private Code code = new Code();
    private Session session = new Session();
    private Frontend frontend = new Frontend();

    // === 子配置类 ===

    public static class Jwt {
        private String secret;
        private long accessTokenExpiration = 7200000;   // 2h
        private long refreshTokenExpiration = 604800000; // 7d

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getAccessTokenExpiration() { return accessTokenExpiration; }
        public void setAccessTokenExpiration(long accessTokenExpiration) { this.accessTokenExpiration = accessTokenExpiration; }
        public long getRefreshTokenExpiration() { return refreshTokenExpiration; }
        public void setRefreshTokenExpiration(long refreshTokenExpiration) { this.refreshTokenExpiration = refreshTokenExpiration; }
    }

    public static class Code {
        private long expiration = 300000; // 5min

        public long getExpiration() { return expiration; }
        public void setExpiration(long expiration) { this.expiration = expiration; }
    }

    public static class Session {
        private long expiration = 86400000; // 24h

        public long getExpiration() { return expiration; }
        public void setExpiration(long expiration) { this.expiration = expiration; }
    }

    public static class Frontend {
        private String url = "http://localhost:5173";

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    // === Getters & Setters ===

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public Code getCode() { return code; }
    public void setCode(Code code) { this.code = code; }
    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }
    public Frontend getFrontend() { return frontend; }
    public void setFrontend(Frontend frontend) { this.frontend = frontend; }
}
