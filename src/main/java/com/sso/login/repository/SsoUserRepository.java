package com.sso.login.repository;

import com.sso.login.entity.SsoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SsoUserRepository extends JpaRepository<SsoUser, Long> {

    Optional<SsoUser> findByUsername(String username);

    Optional<SsoUser> findByEmail(String email);

    // 登录时 identifier 可以是 username 或 email
    Optional<SsoUser> findByUsernameOrEmail(String username, String email);
}
