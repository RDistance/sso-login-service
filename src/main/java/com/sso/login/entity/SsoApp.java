package com.sso.login.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sso_app", uniqueConstraints = {
    @UniqueConstraint(columnNames = "app_id")
})
public class SsoApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64, unique = true)
    private String appId;

    @Column(name = "app_secret", nullable = false, length = 128)
    private String appSecret;

    @Column(name = "app_name", nullable = false, length = 128)
    private String appName;

    @Column(name = "callback_url", nullable = false, length = 256)
    private String callbackUrl;

    @Column(nullable = false)
    private Integer status = 1; // 0-禁用 1-正常

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
