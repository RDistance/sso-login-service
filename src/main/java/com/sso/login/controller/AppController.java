package com.sso.login.controller;

import com.sso.login.dto.AppRegisterRequest;
import com.sso.login.entity.SsoApp;
import com.sso.login.service.AppService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 子应用注册管理
 */
@RestController
@RequestMapping("/sso/app")
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    /**
     * 注册子应用
     * POST /sso/app/register  body: { appId, appName, callbackUrl }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AppRegisterRequest request) {
        try {
            SsoApp app = appService.register(request.getAppId(), request.getAppName(), request.getCallbackUrl());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "appId", app.getAppId(),
                    "appSecret", app.getAppSecret(),
                    "appName", app.getAppName(),
                    "callbackUrl", app.getCallbackUrl()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 获取所有子应用
     * GET /sso/app/list
     */
    @GetMapping("/list")
    public ResponseEntity<List<SsoApp>> list() {
        return ResponseEntity.ok(appService.findAll());
    }
}