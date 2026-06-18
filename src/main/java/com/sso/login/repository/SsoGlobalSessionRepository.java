package com.sso.login.repository;

import com.sso.login.entity.SsoGlobalSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SsoGlobalSessionRepository extends JpaRepository<SsoGlobalSession, Long> {

    Optional<SsoGlobalSession> findBySessionId(String sessionId);

    List<SsoGlobalSession> findByUserId(Long userId);
}
