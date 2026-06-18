package com.sso.login.controller;

import com.sso.login.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息接口
 */
@RestController
@RequestMapping("/sso/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 根据 userId 获取用户信息
     * GET /sso/user/info/1
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(userService.toUserInfo(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}