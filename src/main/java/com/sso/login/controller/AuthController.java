package com.sso.login.controller;

import com.sso.login.config.SsoProperties;
import com.sso.login.dto.LoginRequest;
import com.sso.login.dto.LoginResponse;
import com.sso.login.dto.RegisterRequest;
import com.sso.login.service.AuthService;
import com.sso.login.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证入口 — SSO 登录/注册/注销
 */
@RestController
@RequestMapping("/sso/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final SsoProperties ssoProperties;

    public AuthController(AuthService authService, UserService userService,
                          SsoProperties ssoProperties) {
        this.authService = authService;
        this.userService = userService;
        this.ssoProperties = ssoProperties;
    }

    /**
     * 步骤 1: 用户访问此 URL → 检查全局 Session
     * - 已登录 → 生成 code 直接跳回子应用
     * - 未登录 → 重定向到 SSO 前端登录页
     */
    @GetMapping("/login")
    public Object loginRedirect(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String appId,
            @CookieValue(value = "SSO_SESSION", required = false) String sessionId) {

        // 检查全局 Session
        var session = authService.findValidSession(sessionId);
        if (session != null) {
            // 已登录 — 生成授权码，直接跳转
            String redirectUrl = authService.generateCodeForSession(sessionId, service, appId);
            return ResponseEntity.status(302)
                    .header("Location", redirectUrl)
                    .build();
        }

        // 未登录 — 重定向到 SSO 前端登录页，携带参数
        String frontendUrl = ssoProperties.getFrontend().getUrl();
        String redirectToFrontend = frontendUrl + "/login";
        if (service != null) {
            redirectToFrontend += "?service=" + service;
            if (appId != null) {
                redirectToFrontend += "&appId=" + appId;
            }
        } else if (appId != null) {
            redirectToFrontend += "?appId=" + appId;
        }

        return ResponseEntity.status(302)
                .header("Location", redirectToFrontend)
                .build();
    }

    /**
     * 步骤 2: SSO 前端提交登录凭据
     * POST /sso/auth/login  body: { identifier, password, service, appId }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = new LoginResponse();
        try {
            String redirectUrl = authService.login(
                    request.getIdentifier(),
                    request.getPassword(),
                    request.getService(),
                    request.getAppId()
            );
            response.setSuccess(true);
            response.setMessage("登录成功");
            response.setRedirectUrl(redirectUrl);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = new LoginResponse();
        try {
            var user = userService.register(request.getUsername(), request.getEmail(), request.getPassword());
            response.setSuccess(true);
            response.setMessage("注册成功");
            response.setUserInfo(userService.toUserInfo(user));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 全局注销
     */
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
            @CookieValue(value = "SSO_SESSION", required = false) String sessionId) {
        authService.logout(sessionId);
        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setMessage("已注销");
        return ResponseEntity.ok(response);
    }
}