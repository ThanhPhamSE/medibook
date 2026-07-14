package com.medibook.modules.appointment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.entity.AppointmentStatusHistory;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.repository.AppointmentStatusHistoryRepository;
import com.medibook.modules.notification.dto.AppointmentEmailData;
import com.medibook.modules.notification.service.EmailService;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job tự động hủy các lịch hẹn PENDING trong vòng 6 giờ tới
 * nếu bác sĩ không thực hiện hành động nào (xác nhận hoặc hủy).
 * Job chạy mỗi 15 phút để đảm bảo độ trễ tối đa không vượt quá 15 phút.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentSchedulerService {

    private static final int PENDING_EXPIRY_HOURS = 6;
    private static final String AUTO_CANCEL_REASON =
            "Tự động hủy: Bác sĩ không xác nhận lịch hẹn trong vòng 6 giờ trước khi khám.";

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository historyRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Chạy mỗi 15 phút.
     * Tìm tất cả lịch hẹn có status = PENDING và startDatetime <= (now + 6h),
     * tức là lịch sắp diễn ra trong 6 giờ tới mà bác sĩ chưa xác nhận,
     * sau đó tự động chuyển sang CANCELLED.
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000) // mỗi 15 phút
    @Transactional
    public void autoExpirePendingAppointments() {

        // cutoff = thời điểm 6 giờ kể từ bây giờ
        // => tìm lịch hẹn PENDING có startDatetime <= cutoff (sắp đến mà chưa confirm)
        LocalDateTime cutoff = LocalDateTime.now().plusHours(PENDING_EXPIRY_HOURS);

        List<Appointment> expired = appointmentRepository.findExpiredPendingAppointments(cutoff);

        if (expired.isEmpty()) {
            log.debug("[Scheduler] Không có lịch hẹn PENDING nào sắp diễn ra trong {}h mà chưa xác nhận.",
                    PENDING_EXPIRY_HOURS);
            return;
        }

        log.info("[Scheduler] Tìm thấy {} lịch hẹn PENDING sắp diễn ra trong {}h mà bác sĩ chưa xác nhận, bắt đầu tự động hủy...",
                expired.size(), PENDING_EXPIRY_HOURS);

        // Lấy system user (admin đầu tiên) để ghi history. Nếu không có thì bỏ qua.
        User systemUser = resolveSystemUser();

        for (Appointment appointment : expired) {
            try {
                AppointmentStatus oldStatus = appointment.getStatus();

                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointment.setCancelledReason(AUTO_CANCEL_REASON);
                // cancelledBy = null nếu không có system user (scheduler action)
                if (systemUser != null) {
                    appointment.setCancelledBy(systemUser);
                }

                appointmentRepository.save(appointment);

                // Ghi lịch sử trạng thái
                if (systemUser != null) {
                    saveHistory(appointment, oldStatus, AppointmentStatus.CANCELLED, systemUser);
                }

                // Gửi email thông báo hủy cho bệnh nhân (chạy async, không ảnh hưởng transaction)
                try {
                    AppointmentEmailData emailData = AppointmentEmailData.from(appointment);
                    emailService.sendAppointmentCancelledEmail(emailData);
                } catch (Exception emailEx) {
                    log.warn("[Scheduler] Không thể gửi email hủy cho booking {}: {}",
                            appointment.getBookingCode(), emailEx.getMessage());
                }

                log.info("[Scheduler] Đã tự động hủy lịch hẹn: bookingCode={}, doctorId={}, patientId={}",
                        appointment.getBookingCode(),
                        appointment.getDoctor() != null ? appointment.getDoctor().getId() : "N/A",
                        appointment.getPatient() != null ? appointment.getPatient().getId() : "N/A");

            } catch (Exception ex) {
                log.error("[Scheduler] Lỗi khi tự động hủy lịch hẹn id={}: {}",
                        appointment.getId(), ex.getMessage(), ex);
            }
        }

        log.info("[Scheduler] Hoàn tất tự động hủy {} lịch hẹn PENDING quá hạn.", expired.size());
    }

    /**
     * Tìm user đại diện hệ thống (admin đầu tiên) để ghi vào history.
     * Trả về null nếu không tìm thấy.
     */
    private User resolveSystemUser() {
        try {
            return userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null
                            && "ADMIN".equals(u.getRole().getName())
                            && u.getDeletedAt() == null)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.warn("[Scheduler] Không thể lấy system user: {}", e.getMessage());
            return null;
        }
    }

    private void saveHistory(Appointment appointment, AppointmentStatus oldStatus,
            AppointmentStatus newStatus, User changedBy) {

        AppointmentStatusHistory history = new AppointmentStatusHistory();
        history.setAppointment(appointment);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }
}
