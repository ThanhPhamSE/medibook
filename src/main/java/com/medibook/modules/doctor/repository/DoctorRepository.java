package com.medibook.modules.doctor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.medibook.modules.doctor.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long>,
        JpaSpecificationExecutor<Doctor> {

    boolean existsByUserId(Long userId);

    Optional<Doctor> findByUserId(Long userId);

    @EntityGraph(attributePaths = { "user", "specialty" })
    Optional<Doctor> findByIdAndDeletedAtIsNull(Long id);

}
