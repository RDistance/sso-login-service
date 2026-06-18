package com.sso.login.dto;

/**
 * 用户基本信息（嵌入 TokenResponse 或单独返回）
 */
public class UserInfo {

    private Long id;
    private String username;
    private String email;

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
