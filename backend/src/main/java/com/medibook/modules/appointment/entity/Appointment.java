package com.medibook.modules.appointment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.medibook.common.base.VersionedEntity;
import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointments", indexes = {
                @Index(name = "idx_appt_patient_time", columnList = "patient_id, start_datetime"),
                @Index(name = "idx_appt_status", columnList = "status"),
                @Index(name = "idx_appt_doctor_time_status", columnList = "doctor_id,start_datetime,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment extends VersionedEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "booking_code", nullable = false, unique = true)
        private String bookingCode;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "doctor_id", nullable = false)
        private Doctor doctor;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "patient_id", nullable = false)
        private User patient;

        @Column(name = "consultation_fee", nullable = false)
        private BigDecimal consultationFee;

        @Column(name = "start_datetime", nullable = false)
        private LocalDateTime startDatetime;

        @Column(name = "end_datetime", nullable = false)
        private LocalDateTime endDatetime;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private AppointmentStatus status = AppointmentStatus.PENDING;

        private String note;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "cancelled_by")
        private User cancelledBy;

        @Column(name = "cancelled_reason")
        private String cancelledReason;

        @Column(name = "active_slot_key", insertable = false, updatable = false)
        private String activeSlotKey;
}
