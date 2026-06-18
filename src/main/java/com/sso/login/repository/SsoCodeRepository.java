package com.sso.login.repository;

import com.sso.login.entity.SsoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SsoCodeRepository extends JpaRepository<SsoCode, Long> {

    Optional<SsoCode> findByCode(String code);
}
