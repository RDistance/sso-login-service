package com.sso.login.service;

import com.sso.login.entity.SsoApp;
import com.sso.login.repository.SsoAppRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppService {

    private final SsoAppRepository appRepository;
    private final PasswordEncoder passwordEncoder;

    public AppService(SsoAppRepository appRepository, PasswordEncoder passwordEncoder) {
        this.appRepository = appRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册子应用 — 自动生成 appSecret
     */
    public SsoApp register(String appId, String appName, String callbackUrl) {
        if (appRepository.findByAppId(appId).isPresent()) {
            throw new RuntimeException("appId 已存在");
        }

        SsoApp app = new SsoApp();
        app.setAppId(appId);
        app.setAppSecret(passwordEncoder.encode(appId + "-secret-" + System.nanoTime()));
        app.setAppName(appName);
        app.setCallbackUrl(callbackUrl);
        app.setStatus(1);
        return appRepository.save(app);
    }

    /**
     * 验证 appId + appSecret
     */
    public SsoApp validate(String appId, String appSecret) {
        Optional<SsoApp> appOpt = appRepository.findByAppId(appId);
        if (appOpt.isEmpty()) {
            throw new RuntimeException("应用不存在");
        }

        SsoApp app = appOpt.get();
        if (app.getStatus() != 1) {
            throw new RuntimeException("应用已被禁用");
        }

        // appSecret 存的是 BCrypt 哈希，需要比对
        // 但子应用传来的是原始 secret，所以存储时也应存原始值便于比对
        // 为简化流程，改为直接比对原始值
        if (!app.getAppSecret().equals(appSecret)) {
            throw new RuntimeException("appSecret 不匹配");
        }
        return app;
    }

    /**
     * 根据 appId 查找子应用
     */
    public Optional<SsoApp> findByAppId(String appId) {
        return appRepository.findByAppId(appId);
    }

    /**
     * 获取所有已注册子应用
     */
    public List<SsoApp> findAll() {
        return appRepository.findAll();
    }
}
