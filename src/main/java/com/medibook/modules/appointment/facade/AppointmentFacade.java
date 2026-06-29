package com.medibook.modules.appointment.facade;

import com.medibook.modules.appointment.dto.internal.AppointmentMedicalRecordDto;
import com.medibook.modules.appointment.entity.Appointment;

public interface AppointmentFacade {

    AppointmentMedicalRecordDto getMedicalRecordInfo(Long appointmentId);

    Appointment getAppointmentEntity(Long appointmentId);
}
