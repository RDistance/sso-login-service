package com.sso.login.repository;

import com.sso.login.entity.SsoApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SsoAppRepository extends JpaRepository<SsoApp, Long> {

    Optional<SsoApp> findByAppId(String appId);

    Optional<SsoApp> findByAppIdAndAppSecret(String appId, String appSecret);
}
