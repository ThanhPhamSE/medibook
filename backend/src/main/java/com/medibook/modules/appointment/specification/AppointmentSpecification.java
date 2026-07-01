package com.medibook.modules.appointment.specification;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.entity.Appointment;

public class AppointmentSpecification {

    public static Specification<Appointment> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Appointment> hasDoctorId(Long doctorId) {
        return (root, query, cb) -> doctorId == null ? null : cb.equal(root.get("doctor").get("id"), doctorId);
    }

    public static Specification<Appointment> hasPatientId(Long patientId) {
        return (root, query, cb) -> patientId == null ? null : cb.equal(root.get("patient").get("id"), patientId);
    }

    public static Specification<Appointment> hasStatus(AppointmentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Appointment> hasBookingCodeLike(String code) {
        return (root, query, cb) -> (code == null || code.isBlank())
                ? null
                : cb.like(root.get("bookingCode"), "%" + code + "%");
    }

    public static Specification<Appointment> startAfter(LocalDateTime from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("startDatetime"), from);
    }

    public static Specification<Appointment> startBefore(LocalDateTime to) {
        return (root, query, cb) -> to == null ? null : cb.lessThan(root.get("startDatetime"), to);
    }
}