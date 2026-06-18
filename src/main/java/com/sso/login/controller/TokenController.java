package com.sso.login.controller;

import com.sso.login.dto.RefreshTokenRequest;
import com.sso.login.dto.TokenRequest;
import com.sso.login.dto.TokenResponse;
import com.sso.login.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Token 管理 — code 换 Token / 刷新 Token / 校验 Token
 */
@RestController
@RequestMapping("/sso/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * 子应用后端用 code 换取 accessToken + refreshToken
     * POST /sso/token/exchange  body: { code, appId, appSecret }
     */
    @PostMapping("/exchange")
    public ResponseEntity<?> exchange(@Valid @RequestBody TokenRequest request) {
        try {
            TokenResponse response = tokenService.exchangeCode(
                    request.getCode(),
                    request.getAppId(),
                    request.getAppSecret()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * 刷新 accessToken
     * POST /sso/token/refresh  body: { refreshToken, appId }
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            TokenResponse response = tokenService.refreshToken(
                    request.getRefreshToken(),
                    request.getAppId()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * 校验 accessToken 有效性（子应用调用）
     * GET /sso/token/verify?accessToken=xxx
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String accessToken) {
        try {
            var userInfo = tokenService.verifyAccessToken(accessToken);
            return ResponseEntity.ok(userInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * 简单的错误响应体
     */
    private record ErrorResponse(String message) {}
}