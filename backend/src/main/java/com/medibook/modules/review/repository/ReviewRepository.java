package com.medibook.modules.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medibook.modules.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    Page<Review> findByAppointmentDoctorId(Long doctorId, Pageable pageable);

    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.appointment.doctor.id = :doctorId
            """)
    Double getAverageRating(@Param("doctorId") Long doctorId);

    Long countByAppointmentDoctorId(Long doctorId);
}
