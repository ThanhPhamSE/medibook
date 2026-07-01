package com.medibook.modules.token.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.modules.token.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    boolean existsByTokenHash(String tokenHash);

}
