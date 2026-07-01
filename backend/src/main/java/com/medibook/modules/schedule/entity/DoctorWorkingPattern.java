package com.medibook.modules.schedule.entity;

import java.time.LocalTime;

import com.medibook.common.base.SoftDeleteEntity;
import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.modules.doctor.entity.Doctor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor_working_patterns", uniqueConstraints = {
        @UniqueConstraint(name = "uq_work_pattern", columnNames = { "doctor_id", "day_of_week", "start_time",
                "end_time" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWorkingPattern extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeekEnum dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration;

    @Column(name = "buffer_duration")
    private Integer bufferDuration = 0;
}
