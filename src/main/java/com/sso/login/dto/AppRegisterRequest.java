package com.sso.login.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 子应用注册请求体
 */
public class AppRegisterRequest {

    @NotBlank(message = "appId 不能为空")
    private String appId;

    @NotBlank(message = "appName 不能为空")
    private String appName;

    @NotBlank(message = "callbackUrl 不能为空")
    private String callbackUrl;

    // === Getters & Setters ===

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
}
