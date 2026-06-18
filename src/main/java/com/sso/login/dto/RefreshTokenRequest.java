package com.sso.login.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新 Token 请求体
 */
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;

    @NotBlank(message = "appId 不能为空")
    private String appId;

    // === Getters & Setters ===

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
}
