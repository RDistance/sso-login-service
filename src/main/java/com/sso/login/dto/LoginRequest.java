package com.sso.login.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求体 — 对齐前端 login.vue 的 identifier + password 字段
 */
public class LoginRequest {

    @NotBlank(message = "identifier 不能为空")
    private String identifier; // 用户名或邮箱

    @NotBlank(message = "密码不能为空")
    private String password;

    // SSO 跳转参数
    private String service;   // 回调地址
    private String appId;     // 子应用标识

    // === Getters & Setters ===

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
}
