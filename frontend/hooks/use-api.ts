'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { appointmentService } from '@/services/appointment.service';
import { doctorService } from '@/services/doctor.service';
import { specialtyService } from '@/services/specialty.service';
import { reviewService } from '@/services/review.service';
import { reportService } from '@/services/report.service';
import { dashboardService, notificationService, adminService } from '@/services/index';
import { medicalRecordService } from '@/services/medical-record.service';
import { scheduleService } from '@/services/schedule.service';
import { userService } from '@/services/user.service';
import { paymentService } from '@/services/payment.service';
import { toast } from 'sonner';
import { extractApiError } from '@/services/api';
import type {
  Appointment,
  AppointmentStatus,
  AppointmentStatsResponse,
  AppointmentStatisticResponse,
  AdminDashboardResponse,

  ChartPoint,
  DashboardStats,
  Specialty,
  DoctorPerformanceResponse,
  Doctor,
  MedicalRecord,
  Notification,
  PaginatedResponse,
  RevenueStatisticResponse,
  Review,
  Schedule,
  User,
} from '@/types';

export const qk = {
  doctors: (params: any) => ['doctors', params] as const,
  doctor: (id: string) => ['doctor', id] as const,
  doctorReviews: (id: string) => ['doctor', id, 'reviews'] as const,
  doctorRating: (id: string) => ['doctor', id, 'rating'] as const,
  timeSlots: (doctorId: string, date: string) => ['time-slots', doctorId, date] as const,
  appointments: (params: any) => ['appointments', params] as const,
  appointmentStats: ['appointments', 'stats'] as const,
  appointment: (id: string) => ['appointment', id] as const,

  specialties: ['specialties'] as const,
  specialtiesPaged: (params: any) => ['specialties', params] as const,
  specialty: (id: string) => ['specialty', id] as const,

  dashboardStats: (role: string, from: string, to: string) => ['dashboard', 'stats', role, from, to] as const,
  appointmentTrend: (from: string, to: string) => ['dashboard', 'appointment-trend', from, to] as const,
  revenueTrend: (from: string, to: string) => ['dashboard', 'revenue-trend', from, to] as const,

  specialtyDist: ['dashboard', 'specialty-dist'] as const,
  topDoctors: ['dashboard', 'top-doctors'] as const,
  notifications: ['notifications'] as const,
  patients: (params: any) => ['patients', params] as const,
  users: (params: any) => ['users', params] as const,
  medicalRecords: (params: any) => ['medical-records', params] as const,
  medicalRecord: (id: string) => ['medical-record', id] as const,
  schedules: (doctorId?: string) => ['schedules', doctorId] as const,
  auditLogs: (params: any) => ['audit-logs', params] as const,
  roles: ['roles'] as const,
  permissions: ['permissions'] as const,
  reports: {
    daily: (date: string) => ['reports', 'daily', date] as const,
    monthly: (year: number, month: number) => ['reports', 'monthly', year, month] as const,
    revenue: (from: string, to: string) => ['reports', 'revenue', from, to] as const,
    doctor: (id: string) => ['reports', 'doctor', id] as const,
  } as const,
};

export function useDoctors(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<Doctor>>({
    queryKey: qk.doctors(params),
    queryFn: () => doctorService.list(params),
  });
}

export function useDoctor(id: string) {
  return useQuery<Doctor>({
    queryKey: qk.doctor(id),
    queryFn: () => doctorService.get(id),
    enabled: !!id,
  });
}

export function useDoctorReviews(id: string) {
  return useQuery<Review[]>({
    queryKey: qk.doctorReviews(id),
    queryFn: () => doctorService.reviews(id),
    enabled: !!id,
  });
}

export function useTimeSlots(doctorId: string, date: string) {
  return useQuery<{ startTime: string; endTime: string; available: boolean }[]>({
    queryKey: qk.timeSlots(doctorId, date),
    queryFn: () => doctorService.timeSlots(doctorId, date),
    enabled: !!doctorId && !!date,
  });
}

// export function useAppointments(params: Record<string, unknown> = {}) {
//   return useQuery<PaginatedResponse<Appointment>>({
//     queryKey: qk.appointments(params),
//     queryFn: () => appointmentService.list(params),
//   });
// }

// Hook riêng cho admin: gọi /appointments/admin/bookings
// export function useAdminBookings(params: Record<string, unknown> = {}) {
//   return useQuery<PaginatedResponse<Appointment>>({
//     queryKey: ['admin-bookings', params],
//     queryFn: () => appointmentService.getAllBookings(params),
//   });
// }

export function useAppointment(id: string) {
  return useQuery<Appointment>({
    queryKey: qk.appointment(id),
    queryFn: () => appointmentService.get(id),
    enabled: !!id,
  });
}

export function useCreateAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: any) => appointmentService.create(input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['appointments'] });
      toast.success('Đặt lịch hẹn thành công!');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể đặt lịch hẹn')),
  });
}

export function useConfirmAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => appointmentService.confirm(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['appointments'] });
      qc.invalidateQueries({ queryKey: ['appointment'] });
      toast.success('Xác nhận lịch hẹn thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể xác nhận lịch hẹn')),
  });
}

export function useCompleteAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => appointmentService.complete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['appointments'] });
      qc.invalidateQueries({ queryKey: ['appointment'] });
      toast.success('Hoàn thành lịch hẹn thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể hoàn thành lịch hẹn')),
  });
}

export function useNoShowAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => appointmentService.noShow(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['appointments'] });
      qc.invalidateQueries({ queryKey: ['appointment'] });
      toast.success('Đã đánh dấu là không đến');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể đánh dấu không đến')),
  });
}

export function useRescheduleAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, date, startTime }: { id: string; date: string; startTime: string }) =>
      appointmentService.reschedule(id, { newStartDatetime: `${date}T${startTime}` }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['appointments'] });
      toast.success('Đổi lịch hẹn thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể đổi lịch hẹn')),
  });
}

export function useCancelAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) => appointmentService.cancel(id, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['appointments'] });
      toast.success('Hủy lịch hẹn thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể hủy lịch hẹn')),
  });
}

export function useSpecialties() {
  return useQuery<PaginatedResponse<Specialty>>({ queryKey: qk.specialties, queryFn: () => specialtyService.list() });
}

export function useSpecialtiesPaged(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<Specialty>>({ queryKey: qk.specialtiesPaged(params), queryFn: () => specialtyService.list(params) });
}

export function useSaveSpecialty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id?: string; input: any }) =>
      id ? specialtyService.update(id, input) : specialtyService.create(input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['specialties'] });
      toast.success('Lưu chuyên khoa thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể lưu chuyên khoa')),
  });
}

export function useDeleteSpecialty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => specialtyService.remove(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['specialties'] });
      toast.success('Xóa chuyên khoa thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể xóa chuyên khoa')),
  });
}

export function useSaveDoctor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id?: string; input: any }) =>
      id ? doctorService.update(id, input) : doctorService.create(input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctors'] });
      toast.success('Lưu bác sĩ thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể lưu bác sĩ')),
  });
}

export function useDeleteDoctor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => doctorService.remove(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctors'] });
      toast.success('Xóa bác sĩ thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể xóa bác sĩ')),
  });
}

export function useDashboardStats(role: string, from: string, to: string) {
  return useQuery<DashboardStats>({
    queryKey: qk.dashboardStats(role, from, to),
    queryFn: () => dashboardService.stats(role, from, to),
  });
}
export function useAppointmentTrend(from: string, to: string) {
  return useQuery<ChartPoint[]>({
    queryKey: qk.appointmentTrend(from, to),
    queryFn: () => dashboardService.appointmentTrend(from, to),
  });
}
export function useRevenueTrend(from: string, to: string) {
  return useQuery<ChartPoint[]>({
    queryKey: qk.revenueTrend(from, to),
    queryFn: () => dashboardService.revenueTrend(from, to),
  });
}
export function useSpecialtyDistribution() {
  return useQuery<ChartPoint[]>({ queryKey: qk.specialtyDist, queryFn: () => dashboardService.specialtyDistribution() });
}
export function useTopDoctors() {
  return useQuery<ChartPoint[]>({ queryKey: qk.topDoctors, queryFn: () => dashboardService.topDoctors() });
}

export function useAdminDashboard(from: string, to: string) {
  return useQuery<AdminDashboardResponse>({
    queryKey: ['admin-dashboard', from, to],
    queryFn: () => dashboardService.adminDashboard(from, to),
  });
}

export function useNotifications() {
  return useQuery<Notification[]>({ queryKey: qk.notifications, queryFn: () => notificationService.list() });
}
export function useMarkNotificationRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => notificationService.markRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.notifications }),
  });
}
export function useMarkAllNotificationsRead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => notificationService.markAllRead(),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: qk.notifications });
      toast.success('Đã đánh dấu tất cả thông báo là đã đọc');
    },
  });
}

export function usePatients(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<User>>({ queryKey: qk.patients(params), queryFn: () => userService.patients(params) });
}
export function useUsers(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<User>>({ queryKey: qk.users(params), queryFn: () => userService.list(params) });
}
export function useUpdateUserStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, isActive }: { id: string; isActive: boolean }) => userService.updateStatus(id, isActive).then(() => ({} as User)),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['users'] });
      qc.invalidateQueries({ queryKey: ['patients'] });
      toast.success('Cập nhật trạng thái người dùng thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể cập nhật trạng thái người dùng')),
  });
}

export function useUpdateProfile() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: Partial<User>) => userService.updateProfile(input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['auth'] });
      toast.success('Cập nhật hồ sơ thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể cập nhật hồ sơ')),
  });
}

export function useMedicalRecords(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<MedicalRecord>>({ queryKey: qk.medicalRecords(params), queryFn: () => medicalRecordService.list(params) });
}
export function useMedicalRecord(id: string) {
  return useQuery<MedicalRecord>({ queryKey: qk.medicalRecord(id), queryFn: () => medicalRecordService.get(id), enabled: !!id });
}

export function useAllMedicalRecords(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<MedicalRecord>>({
    queryKey: ['medical-records', 'admin', params],
    queryFn: () => medicalRecordService.getAllRecords(params),
  });
}

export function useDoctorMedicalRecords(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<MedicalRecord>>({
    queryKey: ['medical-records', 'doctor', params],
    queryFn: () => medicalRecordService.getDoctorRecords(params),
  });
}

export function useCreateMedicalRecord() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: any) => medicalRecordService.create(input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['medical-records'] });
      toast.success('Tạo bệnh án thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể tạo bệnh án')),
  });
}

export function useSchedules(doctorId?: string) {
  return useQuery<Schedule[]>({
    queryKey: qk.schedules(doctorId),
    queryFn: () => scheduleService.list(doctorId),
    enabled: !!doctorId,
  });
}
export function useCreateSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: any) => scheduleService.create(input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['schedules'] });
      toast.success('Thêm lịch làm việc thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể thêm lịch làm việc')),
  });
}
export function useDeleteSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => scheduleService.remove(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['schedules'] });
      toast.success('Xóa lịch làm việc thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể xóa lịch làm việc')),
  });
}

export function useAuditLogs(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<any>>({ queryKey: qk.auditLogs(params), queryFn: () => adminService.auditLogs(params) });
}
export function useRoles() {
  return useQuery({ queryKey: qk.roles, queryFn: () => adminService.roles() });
}
export function usePermissions() {
  return useQuery({ queryKey: qk.permissions, queryFn: () => adminService.permissions() });
}

export function useDoctorRating(id: string) {
  return useQuery({
    queryKey: qk.doctorRating(id),
    queryFn: () => reviewService.getDoctorRating(id),
    enabled: !!id,
  });
}

export function useCreateReview() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: any) => reviewService.create(input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctor', 'reviews'] });
      qc.invalidateQueries({ queryKey: ['doctor', 'rating'] });
      toast.success('Gửi đánh giá thành công');
    },
    onError: (e) => toast.error(extractApiError(e, 'Không thể gửi đánh giá')),
  });
}

export function useDailyReport(date: string) {
  return useQuery<AppointmentStatisticResponse>({
    queryKey: qk.reports.daily(date),
    queryFn: () => reportService.dailyStats(date),
    enabled: !!date,
  });
}

export function useMonthlyReport(year: number, month: number) {
  return useQuery<AppointmentStatisticResponse>({
    queryKey: qk.reports.monthly(year, month),
    queryFn: () => reportService.monthlyStats(year, month),
  });
}

export function useRevenueReport(from: string, to: string) {
  return useQuery<RevenueStatisticResponse>({
    queryKey: qk.reports.revenue(from, to),
    queryFn: () => reportService.revenueStats(from, to),
  });
}

export function useDoctorPerformanceReport(doctorId: string) {
  return useQuery({
    queryKey: qk.reports.doctor(doctorId),
    queryFn: () => reportService.doctorPerformance(doctorId),
    enabled: !!doctorId,
  });
}

export function useAppointments(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<Appointment>>({
    queryKey: qk.appointments(params),
    queryFn: () => appointmentService.list(params),
  });
}

export function useAppointmentStats() {
  return useQuery<AppointmentStatsResponse>({
    queryKey: qk.appointmentStats,
    queryFn: () => appointmentService.stats(),
  });
}


// Hook cho DOCTOR/ADMIN: gọi /appointments/doctor/today
export function useTodayAppointments(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<Appointment>>({
    queryKey: ['appointments', 'doctor', 'today', params],
    queryFn: () => appointmentService.today(params),
  });
}

// Hook cho DOCTOR/ADMIN: gọi /appointments/doctor/week
export function useWeekAppointments(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<Appointment>>({
    queryKey: ['appointments', 'doctor', 'week', params],
    queryFn: () => appointmentService.week(params),
  });
}

//Hook riêng cho admin: gọi /appointments/admin/bookings
export function useAdminBookings(params: Record<string, unknown> = {}) {
  return useQuery<PaginatedResponse<Appointment>>({
    queryKey: ['admin-bookings', params],
    queryFn: () => appointmentService.getAllBookings(params),
  });
}

export function usePaymentStatus(appointmentId: number | string | undefined) {
  return useQuery<{ status: string; checkoutUrl?: string; amount?: number; orderCode?: number } | null>({
    queryKey: ['payment-status', appointmentId],
    queryFn: () => paymentService.getStatus(appointmentId!),
    enabled: !!appointmentId,
    staleTime: 10_000,
    refetchInterval: (query) => {
      // Dừng polling khi đã PAID hoặc CANCELLED
      const status = query.state.data?.status;
      if (status === 'PAID' || status === 'CANCELLED' || status === 'NOT_FOUND') return false;
      return 15_000; // poll mỗi 15s khi đang PENDING
    },
  });
}
