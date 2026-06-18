package com.sso.login.dto;

import com.sso.login.dto.UserInfo;

/**
 * Token 响应体 — code换取Token 或 刷新Token 的返回结果
 */
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;       // accessToken 过期时间（秒）
    private UserInfo userInfo;    // 用户基本信息

    // === Getters & Setters ===

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }
}
