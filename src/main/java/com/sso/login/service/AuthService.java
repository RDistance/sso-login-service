package com.sso.login.service;

import com.sso.login.config.SsoProperties;
import com.sso.login.entity.SsoCode;
import com.sso.login.entity.SsoGlobalSession;
import com.sso.login.entity.SsoUser;
import com.sso.login.repository.SsoCodeRepository;
import com.sso.login.repository.SsoGlobalSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final SsoGlobalSessionRepository sessionRepository;
    private final SsoCodeRepository codeRepository;
    private final UserService userService;
    private final AppService appService;
    private final SsoProperties ssoProperties;

    public AuthService(SsoGlobalSessionRepository sessionRepository,
                       SsoCodeRepository codeRepository,
                       UserService userService,
                       AppService appService,
                       SsoProperties ssoProperties) {
        this.sessionRepository = sessionRepository;
        this.codeRepository = codeRepository;
        this.userService = userService;
        this.appService = appService;
        this.ssoProperties = ssoProperties;
    }

    /**
     * 登录 — 验证凭据，创建全局 Session 和授权码，返回跳转 URL
     */
    public String login(String identifier, String password, String service, String appId) {
        // 1. 验证用户凭据
        SsoUser user = userService.authenticate(identifier, password);

        // 2. 检查 appId 和 callbackUrl
        String callbackUrl;
        if (appId != null && !appId.isEmpty()) {
            var app = appService.findByAppId(appId)
                    .orElseThrow(() -> new RuntimeException("应用不存在: " + appId));
            callbackUrl = app.getCallbackUrl();
            // 如果 service 没传，用 app 的 callbackUrl
            if (service == null || service.isEmpty()) {
                service = callbackUrl;
            }
        } else if (service != null && !service.isEmpty()) {
            // 没有 appId 但有 service，直接用 service
            callbackUrl = service;
        } else {
            throw new RuntimeException("缺少 service 或 appId 参数");
        }

        // 3. 创建全局 Session
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        SsoGlobalSession session = new SsoGlobalSession();
        session.setSessionId(sessionId);
        session.setUserId(user.getId());
        session.setExpiresAt(Instant.now().plusMillis(ssoProperties.getSession().getExpiration()));
        sessionRepository.save(session);

        // 4. 生成授权码
        String code = UUID.randomUUID().toString().replace("-", "");
        SsoCode ssoCode = new SsoCode();
        ssoCode.setCode(code);
        ssoCode.setUserId(user.getId());
        ssoCode.setAppId(appId != null ? appId : "default");
        ssoCode.setExpiresAt(Instant.now().plusMillis(ssoProperties.getCode().getExpiration()));
        ssoCode.setUsed(false);
        codeRepository.save(ssoCode);

        // 5. 构建跳转 URL: callback?code=xxx&appId=xxx
        String redirectUrl = service;
        if (service.contains("?")) {
            redirectUrl += "&code=" + code + "&appId=" + (appId != null ? appId : "default");
        } else {
            redirectUrl += "?code=" + code + "&appId=" + (appId != null ? appId : "default");
        }

        return redirectUrl;
    }

    /**
     * 检查全局 Session 是否有效
     */
    public SsoGlobalSession findValidSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return null;

        var sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) return null;

        SsoGlobalSession session = sessionOpt.get();
        if (Instant.now().isAfter(session.getExpiresAt())) return null;

        return session;
    }

    /**
     * 全局注销 — 删除全局 Session
     */
    public void logout(String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            sessionRepository.findBySessionId(sessionId)
                    .ifPresent(sessionRepository::delete);
        }
    }

    /**
     * 已登录用户直接生成授权码跳转（无需再次输入密码）
     */
    public String generateCodeForSession(String sessionId, String service, String appId) {
        SsoGlobalSession session = findValidSession(sessionId);
        if (session == null) {
            throw new RuntimeException("全局 Session 无效或已过期");
        }

        SsoUser user = userService.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String callbackUrl;
        if (appId != null && !appId.isEmpty()) {
            var app = appService.findByAppId(appId)
                    .orElseThrow(() -> new RuntimeException("应用不存在: " + appId));
            callbackUrl = app.getCallbackUrl();
            if (service == null || service.isEmpty()) {
                service = callbackUrl;
            }
        } else if (service != null && !service.isEmpty()) {
            callbackUrl = service;
        } else {
            throw new RuntimeException("缺少 service 或 appId 参数");
        }

        // 生成授权码
        String code = UUID.randomUUID().toString().replace("-", "");
        SsoCode ssoCode = new SsoCode();
        ssoCode.setCode(code);
        ssoCode.setUserId(user.getId());
        ssoCode.setAppId(appId != null ? appId : "default");
        ssoCode.setExpiresAt(Instant.now().plusMillis(ssoProperties.getCode().getExpiration()));
        ssoCode.setUsed(false);
        codeRepository.save(ssoCode);

        String redirectUrl = service;
        if (service.contains("?")) {
            redirectUrl += "&code=" + code + "&appId=" + (appId != null ? appId : "default");
        } else {
            redirectUrl += "?code=" + code + "&appId=" + (appId != null ? appId : "default");
        }

        return redirectUrl;
    }
}