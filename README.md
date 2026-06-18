# SSO 单点登录服务

基于 CAS 协议的跳转认证 + 授权码换 Token + JWT 双 Token 的单点登录中心。

## 目录

- [技术栈](#技术栈)
- [架构流程](#架构流程)
- [数据库](#数据库)
- [接口文档](#接口文档)
- [目录结构](#目录结构)
- [启动方式](#启动方式)
- [子应用接入指南](#子应用接入指南)

---

## 技术栈

| 组件 | 技术 |
|------|------|
| 框架 | Spring Boot 3.5.15 |
| 语言 | Java 21 |
| 数据库 | MySQL 8.4 (Docker) |
| ORM | Spring Data JPA + Hibernate |
| 安全 | Spring Security + BCrypt |
| Token | JWT (jjwt 0.12.6) |
| 前端 | Vue 3 + Vite 8 |
| 构建 | Maven 3.9.16 |

## 架构流程

### 认证流程图

```
┌─────────────────────────────────────────────┐
│               SSO 认证中心                   │
│     sso-login-service (8081)                │
│     sso-login (5173)                        │
└──────────────────────┬──────────────────────┘
                       │
          ┌────────────┴────────────┐
          │                        │
    ┌─────▼─────┐           ┌─────▼─────┐
    │ task-mgmt │           │   blog    │
    │ nexvo.me  │           │blog.nexvo │
    └───────────┘           └───────────┘
```

### 完整登录流程（6步）

```
步骤 1: 用户访问子应用受保护页面
        子应用检测无本地 Token
        → 重定向到 SSO: /sso/auth/login?appId=task-mgmt

步骤 2: SSO 检查全局 Session Cookie
        ✅ 已登录 → 直接生成授权码跳回子应用
        ❌ 未登录 → 重定向到 SSO 前端登录页 (http://localhost:5173)

步骤 3: 用户在 SSO 前端输入账号密码
        → POST /sso/auth/login
          { identifier: "admin", password: "123456", appId: "task-mgmt" }

步骤 4: SSO 后端验证凭据
        ✅ 成功 → 创建全局 Session + 生成一次性授权码(code)
        → 返回 { redirectUrl: "https://nexvo.me/?code=xxx&appId=task-mgmt" }
        ❌ 失败 → 返回错误信息

步骤 5: 浏览器跳转到子应用回调地址
        子应用后端收到 code
        → 调用 SSO: POST /sso/token/exchange
          { code: "xxx", appId: "task-mgmt", appSecret: "task-mgmt-secret-001" }

步骤 6: SSO 验证 code + appSecret
        ✅ → 返回 accessToken + refreshToken + 用户信息
        → 子应用存储 Token，用户登录完成

━━━━━ 用户再访问另一个子应用 ━━━━━

步骤 7: 用户访问 blog
        → 重定向到 SSO
        → SSO 检查全局 Session → 已登录!
        → 直接生成新 code 跳回 blog → 无需再次输密码
```

### 全局注销流程

```
用户点击"退出登录"
  → POST /sso/auth/logout
  → SSO 删除全局 Session
  → 清除前端 Cookie
  → 后续访问任何子应用都需要重新登录
```

## 数据库

### 表结构

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `sso_user` | 用户表 | username, email, password(BCrypt), status |
| `sso_app` | 子应用注册表 | app_id, app_secret, app_name, callback_url |
| `sso_code` | 授权码表（一次性，5分钟有效） | code, user_id, app_id, used, expires_at |
| `sso_global_session` | 全局会话表（24小时有效） | session_id, user_id, expires_at |

### 建表

```bash
docker exec -i mysql8 mysql -u root -p123456 < sql/init.sql
```

### 种子数据

```sql
-- 测试用户（密码: 123456）
INSERT INTO sso_user (username, email, password) VALUES
('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- 已注册子应用
INSERT INTO sso_app (app_id, app_secret, app_name, callback_url) VALUES
('task-mgmt', 'task-mgmt-secret-001', '任务管理系统', 'https://nexvo.me/'),
('blog',      'blog-secret-001',      '博客站点',     'https://blog.nexvo.me/');
```

## 接口文档

所有接口前缀 `/sso`，端口 `8081`。

### 认证接口

| 方法 | 路径 | 说明 | 调用方 |
|------|------|------|--------|
| GET | `/sso/auth/login?service=&appId=` | SSO登录入口，检查全局Session | 浏览器/子应用 |
| POST | `/sso/auth/login` | 提交登录凭据 | SSO前端 |
| POST | `/sso/auth/register` | 注册新用户 | SSO前端 |
| POST | `/sso/auth/logout` | 全局注销 | SSO前端 |

#### POST /sso/auth/login

```
Request:
  { "identifier": "admin", "password": "123456", "service": "", "appId": "task-mgmt" }

Response (成功):
  { "success": true, "message": "登录成功", "redirectUrl": "https://nexvo.me/?code=xxx&appId=task-mgmt" }

Response (失败):
  { "success": false, "message": "密码错误" }
```

#### POST /sso/auth/register

```
Request:
  { "username": "newuser", "email": "new@example.com", "password": "123456" }

Response (成功):
  { "success": true, "message": "注册成功", "userInfo": { "id": 2, "username": "newuser", "email": "new@example.com" } }
```

### Token 接口（子应用后端调用）

| 方法 | 路径 | 说明 | 调用方 |
|------|------|------|--------|
| POST | `/sso/token/exchange` | code 换 accessToken + refreshToken | 子应用后端 |
| POST | `/sso/token/refresh` | 刷新 accessToken | 子应用后端 |
| GET | `/sso/token/verify?accessToken=` | 校验 accessToken 有效性 | 子应用后端 |

#### POST /sso/token/exchange

```
Request:
  { "code": "xxx", "appId": "task-mgmt", "appSecret": "task-mgmt-secret-001" }

Response:
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200,
    "userInfo": { "id": 1, "username": "admin", "email": "admin@example.com" }
  }
```

#### GET /sso/token/verify

```
Request:
  GET /sso/token/verify?accessToken=eyJhbGciOiJIUzI1NiJ9...

Response (有效):
  { "id": 1, "username": "admin", "email": "admin@example.com" }

Response (无效):
  { "message": "accessToken 无效或已过期" }   ← HTTP 401
```

### 子应用管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/sso/app/register` | 注册新子应用 |
| GET | `/sso/app/list` | 获取所有已注册子应用 |

## 目录结构

```
sso-login-service/
├── pom.xml                              ← Maven 依赖
├── sql/
│   └── init.sql                         ← 建表 + 种子数据
└── src/main/java/com/sso/login/
    ├── LoginApplication.java            ← 启动类
    ├── config/
    │   ├── CorsConfig.java              ← CORS 跨域配置
    │   ├── SecurityConfig.java          ← Spring Security (放行 /sso/**)
    │   └── SsoProperties.java           ← 自定义配置绑定
    ├── controller/
    │   ├── AuthController.java          ← 登录/注册/注销
    │   ├── TokenController.java         ← code 换 Token/刷新/校验
    │   ├── UserController.java          ← 用户信息查询
    │   └── AppController.java           ← 子应用注册管理
    ├── dto/
    │   ├── AppRegisterRequest.java
    │   ├── LoginRequest.java            ← { identifier, password, service, appId }
    │   ├── LoginResponse.java           ← { success, message, redirectUrl }
    │   ├── RefreshTokenRequest.java
    │   ├── RegisterRequest.java         ← { username, email, password }
    │   ├── TokenRequest.java            ← { code, appId, appSecret }
    │   ├── TokenResponse.java           ← { accessToken, refreshToken, userInfo }
    │   └── UserInfo.java               ← { id, username, email }
    ├── entity/
    │   ├── SsoUser.java                ← 用户表实体
    │   ├── SsoApp.java                 ← 子应用表实体
    │   ├── SsoCode.java                ← 授权码表实体
    │   └── SsoGlobalSession.java       ← 全局会话表实体
    ├── repository/                     ← JPA Repository
    ├── service/
    │   ├── AppService.java             ← 子应用注册/验证
    │   ├── AuthService.java            ← 登录/全局Session/授权码生成
    │   ├── TokenService.java           ← code 换 Token/刷新/校验
    │   └── UserService.java            ← 用户注册/登录验证
    └── util/
        └── JwtUtil.java                ← JWT 生成/解析
```

## 启动方式

### 1. 启动 MySQL（Docker）

```bash
# 如果 MySQL 容器未启动
docker run -d --name mysql8 -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_CHARACTER_SET_SERVER=utf8mb4 \
  mysql:8.4
```

### 2. 建库建表

```bash
docker exec -i mysql8 mysql -u root -p123456 < sql/init.sql
```

### 3. 启动后端

```bash
./mvnw spring-boot:run
```

服务启动在 `http://localhost:8081`

### 4. 启动前端（sso-login）

```bash
cd ../sso-login
npm install
npm run dev
```

前端启动在 `http://localhost:5173`

### 5. 测试登录

直接打开以下地址模拟子应用跳转登录：

```
http://localhost:5173/?appId=task-mgmt&service=https://nexvo.me/
```

输入 `admin / 123456` 登录成功后，浏览器会跳转到 `https://nexvo.me/?code=xxx&appId=task-mgmt`。

## 子应用接入指南

子应用接入 SSO 需要做以下三件事：

### 1. 后端添加回调接口

```java
// 伪代码 — 子应用后端
@GetMapping("/sso/callback")
public String ssoCallback(@RequestParam String code, @RequestParam String appId) {
    // 调用 SSO 换 Token
    TokenResponse token = restTemplate.postForObject(
        "http://localhost:8081/sso/token/exchange",
        new TokenRequest(code, appId, "task-mgmt-secret-001"),
        TokenResponse.class
    );
    // 存储 Token 到本地 Session
    session.setAttribute("accessToken", token.getAccessToken());
    session.setAttribute("userInfo", token.getUserInfo());
    // 重定向到前端首页
    return "redirect:/";
}
```

### 2. 后端添加认证拦截器

```java
// 伪代码 — 每次请求校验 Token
public boolean preHandle(HttpServletRequest request) {
    String token = session.getAttribute("accessToken");
    if (token == null) {
        response.sendRedirect("http://localhost:8081/sso/auth/login?appId=task-mgmt");
        return false;
    }
    // 可选: 调 SSO 校验 Token 有效性
    // GET /sso/token/verify?accessToken=xxx
    return true;
}
```

### 3. 前端添加退出按钮

```html
<button @click="logout">退出登录</button>
```

```js
async function logout() {
  await fetch("http://localhost:8081/sso/auth/logout", {
    method: "POST",
    credentials: "include"  // 携带 SSO_SESSION Cookie
  });
  // 清除本地 Token
  window.location.href = "/";
}
```

## 配置说明

### application.yaml 关键配置

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sso_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  jpa:
    hibernate:
      ddl-auto: validate    # 校验表结构，不自动建表

sso:
  jwt:
    secret: sso-login-service-jwt-secret-key-change-in-production-2026
    access-token-expiration: 7200000       # 2小时
    refresh-token-expiration: 604800000    # 7天
  code:
    expiration: 300000                     # 授权码5分钟有效
  session:
    expiration: 86400000                   # 全局Session 24小时
  frontend:
    url: http://localhost:5173             # SSO前端地址
```

## 安全要点

| 要点 | 措施 |
|------|------|
| 授权码一次性 | code 换 Token 后立即标记 `used=true` |
| 授权码短时效 | 有效期 5 分钟 |
| 密码加密 | BCrypt 哈希，不存明文 |
| appSecret 保护 | 仅子应用后端和 SSO 知道，不经过浏览器 |
| 权限校验 | 子应用每次请求可调用 `/sso/token/verify` 远程校验 |
| Token 刷新 | refreshToken 刷新时自动轮转 |
| CORS | 目前放开所有来源，生产环境需限定域名 |