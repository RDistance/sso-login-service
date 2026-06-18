-- ========================================
-- SSO 单点登录系统 — 建表 SQL
-- 数据库: sso_db
-- ========================================

CREATE DATABASE IF NOT EXISTS sso_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE sso_db;

-- ----------------------------------------
-- 表1: sso_user — 用户表
-- ----------------------------------------
CREATE TABLE sso_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    email       VARCHAR(128) NOT NULL,
    password    VARCHAR(256) NOT NULL COMMENT 'BCrypt 加密',
    status      INT          NOT NULL DEFAULT 1 COMMENT '0-禁用 1-正常',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SSO 用户';

-- ----------------------------------------
-- 表2: sso_app — 子应用注册表
-- ----------------------------------------
CREATE TABLE sso_app (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    app_id       VARCHAR(64)  NOT NULL COMMENT '应用标识',
    app_secret   VARCHAR(128) NOT NULL COMMENT '应用密钥',
    app_name     VARCHAR(128) NOT NULL COMMENT '应用名称',
    callback_url VARCHAR(256) NOT NULL COMMENT '认证回调地址',
    status       INT          NOT NULL DEFAULT 1 COMMENT '0-禁用 1-正常',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_id (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SSO 子应用';

-- ----------------------------------------
-- 表3: sso_code — 授权码表（一次性，5分钟有效）
-- ----------------------------------------
CREATE TABLE sso_code (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(64)  NOT NULL COMMENT '授权码',
    user_id    BIGINT       NOT NULL COMMENT '关联用户ID',
    app_id     VARCHAR(64)  NOT NULL COMMENT '关联应用ID',
    used       BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否已使用',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP    NOT NULL COMMENT '过期时间',
    UNIQUE KEY uk_code (code),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SSO 授权码';

-- ----------------------------------------
-- 表4: sso_global_session — 全局会话表（24小时有效）
-- ----------------------------------------
CREATE TABLE sso_global_session (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(128) NOT NULL COMMENT '全局会话ID',
    user_id    BIGINT       NOT NULL COMMENT '关联用户ID',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP    NOT NULL COMMENT '过期时间',
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SSO 全局会话';

-- ----------------------------------------
-- 初始种子数据
-- ----------------------------------------

-- 测试用户（密码: 123456 的 BCrypt 哈希）
INSERT INTO sso_user (username, email, password) VALUES
('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- 测试子应用
INSERT INTO sso_app (app_id, app_secret, app_name, callback_url) VALUES
('task-mgmt', 'task-mgmt-secret-001', '任务管理系统', 'https://nexvo.me/'),
('blog',  'blog-secret-001',   '博客站点',     'https://blog.nexvo.me/');
