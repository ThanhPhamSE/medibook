package com.medibook.modules.schedule.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.modules.doctor.entity.DoctorWorkingPattern;

public interface DoctorWorkingPatternRepository extends JpaRepository<DoctorWorkingPattern, Long> {

    Optional<DoctorWorkingPattern> findByDoctorIdAndDayOfWeekAndDeletedAtIsNull(Long doctorId, DayOfWeekEnum dayOfWeek);

    boolean existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(Long doctorId, DayOfWeekEnum dayOfWeek);

    Optional<DoctorWorkingPattern> findByIdAndDeletedAtIsNull(Long id);

    List<DoctorWorkingPattern> findByDoctorIdAndDeletedAtIsNull(Long doctorId);
}
