package com.sso.login.service;

import com.sso.login.dto.UserInfo;
import com.sso.login.entity.SsoUser;
import com.sso.login.repository.SsoUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final SsoUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(SsoUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册新用户
     */
    public SsoUser register(String username, String email, String rawPassword) {
        // 检查用户名和邮箱是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("邮箱已存在");
        }

        SsoUser user = new SsoUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setStatus(1);
        return userRepository.save(user);
    }

    /**
     * 登录验证 — identifier 可以是 username 或 email
     */
    public SsoUser authenticate(String identifier, String rawPassword) {
        Optional<SsoUser> userOpt = userRepository.findByUsernameOrEmail(identifier, identifier);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        SsoUser user = userOpt.get();
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    /**
     * 根据 ID 查找用户
     */
    public Optional<SsoUser> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 转换为 UserInfo DTO
     */
    public UserInfo toUserInfo(SsoUser user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setEmail(user.getEmail());
        return info;
    }
}
