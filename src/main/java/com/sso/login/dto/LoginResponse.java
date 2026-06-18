package com.sso.login.dto;

/**
 * 登录成功响应体 — 返回给 SSO 前端，包含跳转地址
 */
public class LoginResponse {

    private Boolean success;
    private String message;
    private String redirectUrl;  // 跳转回子应用的 URL（带 code 参数）
    private UserInfo userInfo;

    // === Getters & Setters ===

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }

    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }
}
