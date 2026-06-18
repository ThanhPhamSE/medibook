package com.medibook.modules.specialty.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.modules.specialty.entity.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    List<Specialty> findAllByDeletedAtIsNull();

    Optional<Specialty> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndDeletedAtIsNullAndIdNot(String name, Long id);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String name, Long id);

    Page<Specialty> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Specialty> findByDeletedAtIsNullAndNameContainingIgnoreCase(String keyword, Pageable pageable);
}
