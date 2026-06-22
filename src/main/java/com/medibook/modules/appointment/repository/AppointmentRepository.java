package com.medibook.modules.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medibook.modules.appointment.entity.Appointment;

import jakarta.persistence.LockModeType;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctorIdAndStartDatetimeAndDeletedAtIsNull(Long doctorId, LocalDateTime strarDateTime);

    boolean existsByPatientIdAndStartDatetimeAndDeletedAtIsNull(Long patientId, LocalDateTime startDateTime);

    List<Appointment> findByPatientIdAndDeletedAtIsNull(Long patientId);

    Page<Appointment> findByPatientIdAndDeletedAtIsNull(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorIdAndDeletedAtIsNull(Long doctorId, Pageable pageable);

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
}
