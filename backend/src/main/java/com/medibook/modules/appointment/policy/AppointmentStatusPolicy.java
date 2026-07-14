package com.medibook.modules.appointment.policy;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;

@Component
public class AppointmentStatusPolicy {

    private static final Set<AppointmentStatus> VALID_CANCEL_FROM = Set.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED
    );

    private static final Set<AppointmentStatus> VALID_CONFIRM_FROM = Set.of(
            AppointmentStatus.PENDING
    );

    private static final Set<AppointmentStatus> VALID_COMPLETE_FROM = Set.of(
            AppointmentStatus.CONFIRMED
    );

    private static final Set<AppointmentStatus> VALID_NO_SHOW_FROM = Set.of(
            AppointmentStatus.CONFIRMED
    );

    private static final Set<AppointmentStatus> VALID_RESCHEDULE_FROM = Set.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED
    );

    public void validateCancelTransition(AppointmentStatus currentStatus) {
        if (!VALID_CANCEL_FROM.contains(currentStatus)) {
            throw new BadRequestException(
                    "Cannot cancel appointment with status: " + currentStatus);
        }
    }

    public void validateConfirmTransition(AppointmentStatus currentStatus) {
        if (!VALID_CONFIRM_FROM.contains(currentStatus)) {
            throw new BadRequestException(
                    "Cannot confirm appointment with status: " + currentStatus);
        }
    }

    public void validateCompleteTransition(AppointmentStatus currentStatus) {
        if (!VALID_COMPLETE_FROM.contains(currentStatus)) {
            throw new BadRequestException(
                    "Cannot complete appointment with status: " + currentStatus);
        }
    }

    public void validateNoShowTransition(AppointmentStatus currentStatus) {
        if (!VALID_NO_SHOW_FROM.contains(currentStatus)) {
            throw new BadRequestException(
                    "Cannot mark as no-show appointment with status: " + currentStatus);
        }
    }

    public void validateRescheduleTransition(AppointmentStatus currentStatus) {
        if (!VALID_RESCHEDULE_FROM.contains(currentStatus)) {
            throw new BadRequestException(
                    "Cannot reschedule appointment with status: " + currentStatus);
        }
    }

    public boolean isValidTransition(AppointmentStatus from, AppointmentStatus to) {
        return switch (to) {
            case CANCELLED -> VALID_CANCEL_FROM.contains(from);
            case CONFIRMED -> VALID_CONFIRM_FROM.contains(from);
            case COMPLETED -> VALID_COMPLETE_FROM.contains(from);
            case NO_SHOW -> VALID_NO_SHOW_FROM.contains(from);
            default -> false;
        };
    }
}
