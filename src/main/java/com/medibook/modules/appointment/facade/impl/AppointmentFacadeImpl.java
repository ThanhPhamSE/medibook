package com.medibook.modules.appointment.facade.impl;

import org.springframework.stereotype.Service;

import com.medibook.modules.appointment.dto.internal.AppointmentMedicalRecordDto;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.facade.AppointmentFacade;
import com.medibook.modules.appointment.service.AppointmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentFacadeImpl implements AppointmentFacade {

    private final AppointmentService appointmentService;

    public AppointmentMedicalRecordDto getMedicalRecordInfo(Long appointmentId) {

        Appointment appointment = appointmentService.getAppointmentEntity(appointmentId);

        return AppointmentMedicalRecordDto.builder().appointmentId(appointment.getId()).status(appointment.getStatus())
                .doctorUserId(appointment.getDoctor().getUser().getId())
                .patientUserId(appointment.getPatient().getId()).bookingCode(appointment.getBookingCode()).build();

    }

    @Override
    public Appointment getAppointmentEntity(Long appointmentId) {
        return appointmentService.getAppointmentEntity(appointmentId);
    }
}
