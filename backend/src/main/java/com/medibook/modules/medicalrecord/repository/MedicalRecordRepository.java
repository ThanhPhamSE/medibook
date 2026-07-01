package com.medibook.modules.medicalrecord.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.modules.medicalrecord.entity.MedicalRecord;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<MedicalRecord> findByIdAndDeletedAtIsNull(Long id);

    Optional<MedicalRecord> findByAppointmentIdAndDeletedAtIsNull(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    boolean existsByAppointmentIdAndDeletedAtIsNull(long appointmentId);

    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.doctor",
            "appointment.doctor.user",
            "appointment.patient"
    })
    Page<MedicalRecord> findByAppointmentPatientIdAndDeletedAtIsNull(Long patientId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.doctor",
            "appointment.doctor.user",
            "appointment.patient"
    })
    Page<MedicalRecord> findByAppointmentDoctorUserIdAndDeletedAtIsNull(Long doctorUserId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.doctor",
            "appointment.doctor.user",
            "appointment.patient"
    })
    Page<MedicalRecord> findByDeletedAtIsNull(Pageable pageable);
}

