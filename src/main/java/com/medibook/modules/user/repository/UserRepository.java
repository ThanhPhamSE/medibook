package com.medibook.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.modules.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
