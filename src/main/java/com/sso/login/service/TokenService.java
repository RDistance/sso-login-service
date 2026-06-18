package com.sso.login.service;

import com.sso.login.dto.TokenResponse;
import com.sso.login.dto.UserInfo;
import com.sso.login.entity.SsoCode;
import com.sso.login.entity.SsoUser;
import com.sso.login.repository.SsoCodeRepository;
import com.sso.login.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class TokenService {

    private final SsoCodeRepository codeRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AppService appService;

    public TokenService(SsoCodeRepository codeRepository, JwtUtil jwtUtil,
                        UserService userService, AppService appService) {
        this.codeRepository = codeRepository;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.appService = appService;
    }

    /**
     * 用授权码换取 Token（子应用后端调用）
     */
    public TokenResponse exchangeCode(String code, String appId, String appSecret) {
        // 1. 验证 appId + appSecret
        appService.validate(appId, appSecret);

        // 2. 查找授权码
        Optional<SsoCode> codeOpt = codeRepository.findByCode(code);
        if (codeOpt.isEmpty()) {
            throw new RuntimeException("授权码不存在");
        }

        SsoCode ssoCode = codeOpt.get();

        // 3. 检查授权码是否已使用
        if (ssoCode.getUsed()) {
            throw new RuntimeException("授权码已使用");
        }

        // 4. 检查授权码是否过期
        if (Instant.now().isAfter(ssoCode.getExpiresAt())) {
            throw new RuntimeException("授权码已过期");
        }

        // 5. 检查授权码的 appId 是否匹配
        if (!ssoCode.getAppId().equals(appId)) {
            throw new RuntimeException("授权码与应用不匹配");
        }

        // 6. 标记授权码已使用（一次性）
        ssoCode.setUsed(true);
        codeRepository.save(ssoCode);

        // 7. 生成 Token
        SsoUser user = userService.findById(ssoCode.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), appId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), appId);

        // 8. 构建响应
        TokenResponse response = new TokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getAccessTokenExpiresInSeconds());
        response.setUserInfo(userService.toUserInfo(user));
        return response;
    }

    /**
     * 刷新 accessToken
     */
    public TokenResponse refreshToken(String refreshTokenStr, String appId) {
        // 1. 解析 refreshToken
        if (!jwtUtil.validateToken(refreshTokenStr)) {
            throw new RuntimeException("refreshToken 无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshTokenStr);
        SsoUser user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 生成新的 accessToken
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), appId);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), appId);

        // 3. 构建响应
        TokenResponse response = new TokenResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(jwtUtil.getAccessTokenExpiresInSeconds());
        response.setUserInfo(userService.toUserInfo(user));
        return response;
    }

    /**
     * 校验 accessToken 有效性（子应用调用）
     */
    public UserInfo verifyAccessToken(String accessToken) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("accessToken 无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        SsoUser user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return userService.toUserInfo(user);
    }
}
