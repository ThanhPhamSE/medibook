package com.medibook.modules.specialty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.modules.specialty.entity.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    List<Specialty> findAllByDeletedAtIsNull();

    Optional<Specialty> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByNameAndDeletedAtIsNull(String name);
}
