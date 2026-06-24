package com.medibook.modules.specialty.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medibook.modules.specialty.entity.Specialty;

import jakarta.persistence.LockModeType;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    Optional<Specialty> findByIdAndDeletedAtIsNull(Long id);

    Optional<Specialty> findByNameIgnoreCaseAndDeletedAtIsNotNull(String name);

    boolean existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull(String name, Long id);

    Page<Specialty> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name, Pageable pageable);

    Page<Specialty> findByDeletedAtIsNull(Pageable pageable);

    Page<Specialty> findByDeletedAtIsNotNull(Pageable pageable);

    Optional<Specialty> findByIdAndDeletedAtIsNotNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT s FROM Specialty s
                WHERE LOWER(s.name) = LOWER(:name)
            """)
    Optional<Specialty> findForUpdateByName(@Param("name") String name);
}
