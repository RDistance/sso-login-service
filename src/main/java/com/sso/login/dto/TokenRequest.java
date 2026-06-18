package com.sso.login.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用授权码换取 Token 的请求体（子应用后端调用）
 */
public class TokenRequest {

    @NotBlank(message = "授权码不能为空")
    private String code;

    @NotBlank(message = "appId 不能为空")
    private String appId;

    @NotBlank(message = "appSecret 不能为空")
    private String appSecret;

    // === Getters & Setters ===

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
}
