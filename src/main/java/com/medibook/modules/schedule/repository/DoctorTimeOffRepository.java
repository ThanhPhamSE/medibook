package com.medibook.modules.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medibook.modules.doctor.entity.DoctorTimeOff;

public interface DoctorTimeOffRepository extends JpaRepository<DoctorTimeOff, Long> {

        @Query("""
                        SELECT t FROM DoctorTimeOff t
                        WHERE t.doctor.id = :doctorId
                        AND t.deletedAt IS NULL
                        AND (
                                (t.startDatetime <= :end and t.endDatetime >= :start)
                        )
                        """)
        List<DoctorTimeOff> findOverlapping(@Param("doctorId") Long doctorId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        Optional<DoctorTimeOff> findByIdAndDeletedAtIsNull(Long id);

        @Query("""
                        SELECT t
                        FROM DoctorTimeOff t
                        WHERE t.doctor.id = :doctorId
                        AND t.deletedAt IS NULL
                        AND t.startDatetime < :dayEnd
                        AND t.endDatetime > :dayStart
                        """)
        List<DoctorTimeOff> findByDoctorAndDate(@Param("doctorId") Long doctorId,
                        @Param("dayStart") LocalDateTime dayStart,
                        @Param("dayEnd") LocalDateTime dayEnd);

        List<DoctorTimeOff> findByDoctorIdAndDeletedAtIsNull(Long doctorId);

        @Query("""
                        SELECT t
                        FROM DoctorTimeOff t
                        WHERE t.deletedAt IS NULL
                        AND t.doctor.id = :doctorId
                        AND t.startDatetime < :dayEnd
                        AND t.endDatetime > :dayStart
                        """)
        List<DoctorTimeOff> findOverlappingDate(
                        @Param("doctorId") Long doctorId,
                        @Param("dayStart") LocalDateTime dayStart,
                        @Param("dayEnd") LocalDateTime dayEnd);
}
