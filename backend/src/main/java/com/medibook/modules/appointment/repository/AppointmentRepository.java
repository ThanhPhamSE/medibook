package com.medibook.modules.appointment.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.entity.Appointment;

import jakarta.persistence.LockModeType;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

        boolean existsByDoctorIdAndStartDatetimeAndDeletedAtIsNull(Long doctorId, LocalDateTime strarDateTime);

        boolean existsByPatientIdAndStartDatetimeAndDeletedAtIsNull(Long patientId, LocalDateTime startDateTime);

        List<Appointment> findByPatientIdAndDeletedAtIsNull(Long patientId);

        @EntityGraph(attributePaths = { "doctor", "doctor.user", "patient" })
        Page<Appointment> findByPatientIdAndDeletedAtIsNull(Long patientId, Pageable pageable);

        @EntityGraph(attributePaths = { "doctor", "doctor.user", "patient" })
        Page<Appointment> findByDoctorIdAndDeletedAtIsNull(Long doctorId, Pageable pageable);

        @EntityGraph(attributePaths = { "doctor", "doctor.user", "patient" })
        Optional<Appointment> findByIdAndDeletedAtIsNull(Long id);

        @EntityGraph(attributePaths = { "doctor", "doctor.user", "patient" })
        Page<Appointment> findByDeletedAtIsNull(Pageable pageable);

        @EntityGraph(attributePaths = { "doctor", "doctor.user", "patient" })
        Page<Appointment> findAll(Specification<Appointment> spec, Pageable pageable);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT a
                        FROM Appointment a
                        WHERE a.doctor.id = :doctorId
                        AND a.startDatetime = :startDatetime
                        AND a.deletedAt IS NULL
                        AND a.status IN (
                        com.medibook.common.enums.AppointmentStatus.PENDING,
                        com.medibook.common.enums.AppointmentStatus.CONFIRMED
                        )
                        """)
        List<Appointment> findLockedAppointment(@Param("doctorId") Long doctorId,
                        @Param("startDatetime") LocalDateTime startDatetime);

        boolean existsByBookingCode(String bookingCode);

        @Query("""
                        SELECT COUNT(a) > 0
                        FROM Appointment a
                        WHERE a.doctor.id = :doctorId
                        AND a.startDatetime = :startDatetime
                        AND a.deletedAt IS NULL
                        AND a.status IN (
                            com.medibook.common.enums.AppointmentStatus.PENDING,
                            com.medibook.common.enums.AppointmentStatus.CONFIRMED
                        )
                        """)
        boolean existsActiveAppointment(@Param("doctorId") Long doctorId,
                        @Param("startDatetime") LocalDateTime startDatetime);

        @Query("""
                        SELECT a
                        FROM Appointment a
                        WHERE a.doctor.id = :doctorId
                        AND a.deletedAt IS NULL
                        AND a.status IN (
                            com.medibook.common.enums.AppointmentStatus.PENDING,
                            com.medibook.common.enums.AppointmentStatus.CONFIRMED
                        )
                        AND a.startDatetime >= :startOfDay
                        AND a.startDatetime < :endOfDay
                        """)
        List<Appointment> findActiveAppointmentsByDate(@Param("doctorId") Long doctorId,
                        @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

        @Query("""
                            SELECT COUNT(a) > 0
                            FROM Appointment a
                            WHERE a.patient.id = :patientId
                            AND (:appointmentId IS NULL OR a.id <> :appointmentId)
                            AND a.deletedAt IS NULL
                            AND a.status IN (
                                com.medibook.common.enums.AppointmentStatus.PENDING,
                                com.medibook.common.enums.AppointmentStatus.CONFIRMED
                            )
                            AND a.startDatetime < :endTime
                            AND a.endDatetime > :startTime
                        """)
        boolean existsPatientOverlap(@Param("patientId") Long patientId, @Param("appointmentId") Long appointmentId,
                        @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

        @Query("""
                        SELECT COUNT(a) > 0
                        FROM Appointment a
                        WHERE a.doctor.id = :doctorId
                        AND a.startDatetime = :now
                        AND a.deletedAt IS NULL
                        AND a.status IN (
                                com.medibook.common.enums.AppointmentStatus.PENDING,
                                com.medibook.common.enums.AppointmentStatus.CONFIRMED
                        )
                                """)
        boolean existsFutureAppointment(Long doctorId, LocalDateTime now);

        @EntityGraph(attributePaths = { "doctor", "doctor.user", "patient" })
        @Query("""
                            SELECT a
                            FROM Appointment a
                            WHERE a.doctor.id = :doctorId
                            AND a.deletedAt IS NULL
                            AND (:status IS NULL OR a.status = :status)
                            AND a.startDatetime BETWEEN :start AND :end
                        """)
        Page<Appointment> findDoctorAppointments(Long doctorId, AppointmentStatus status, LocalDateTime start,
                        LocalDateTime end, Pageable pageable);

        long countByDeletedAtIsNullAndStartDatetimeBetween(LocalDateTime from, LocalDateTime to);

        long countByDeletedAtIsNullAndStatusAndStartDatetimeBetween(AppointmentStatus status, LocalDateTime from,
                        LocalDateTime to);

        long countByDeletedAtIsNullAndDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

        @Query("""
                        SELECT COALESCE(SUM(a.consultationFee),0)
                        FROM Appointment a
                        WHERE a.deletedAt IS NULL
                        AND a.status = :status
                        AND a.startDatetime BETWEEN :from AND :to
                        """)
        BigDecimal sumRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                        @Param("status") AppointmentStatus status);

        @Query("""
                        SELECT COALESCE(SUM(a.consultationFee),0)
                        FROM Appointment a
                        WHERE a.deletedAt IS NULL
                        AND a.status = :status
                        AND a.doctor.id = :doctorId
                        """)
        BigDecimal sumDoctorRevenue(@Param("doctorId") Long doctorId, @Param("status") AppointmentStatus status);

        @Query("""
                        SELECT a.status AS status, COUNT(a) AS cnt
                        FROM Appointment a
                        WHERE a.deletedAt IS NULL
                        AND a.startDatetime >= :from
                        AND a.startDatetime < :to
                        GROUP BY a.status
                        """)
        List<StatusCountProjection> countGroupByStatus(@Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to);
}
