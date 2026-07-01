package com.medibook.modules.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.modules.appointment.entity.AppointmentStatusHistory;

public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Long> {

}
