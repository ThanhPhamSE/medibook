package com.medibook.modules.doctor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medibook.modules.doctor.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long>,
                JpaSpecificationExecutor<Doctor> {

        boolean existsByUserId(Long userId);

        Optional<Doctor> findByUserId(Long userId);

        Optional<Doctor> findByUserIdAndDeletedAtIsNull(Long userId);

        @EntityGraph(attributePaths = { "user", "specialty" })
        Optional<Doctor> findByIdAndDeletedAtIsNull(Long id);

        @Override
        @EntityGraph(attributePaths = { "user", "specialty"
        })
        Page<Doctor> findAll(Specification<Doctor> spec, Pageable pageable);

        @Query("""
                            SELECT d
                            FROM Doctor d
                            WHERE d.id = :id
                            AND d.deletedAt IS NULL
                        """)
        Doctor findDoctorForBooking(@Param("id") Long id);

        @Query("""
                        SELECT d.specialty.id AS specialtyId, COUNT(d) AS cnt
                        FROM Doctor d
                        WHERE d.specialty.id IN :specialtyIds
                        GROUP BY d.specialty.id
                        """)
        List<SpecialtyDoctorCountProjection> countBySpecialtyIdIn(@Param("specialtyIds") List<Long> specialtyIds);

        @Query("""
                        UPDATE Doctor d
                        SET d.averageRating = :averageRating,
                            d.totalReviews = :totalReviews
                        WHERE d.id = :doctorId
                        """)
        void updateRating(@Param("doctorId") Long doctorId,
                        @Param("averageRating") java.math.BigDecimal averageRating,
                        @Param("totalReviews") Integer totalReviews);

}
