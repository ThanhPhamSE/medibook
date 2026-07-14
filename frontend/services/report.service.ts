import { apiGet } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { AppointmentStatisticResponse, RevenueStatisticResponse, DoctorPerformanceResponse } from '@/types';

/**
 * Backend AppointmentStatisticResponse fields:
 *   total, pending, confirmed, completed, cancelled, noShow
 * 
 * Backend RevenueStatisticResponse fields:
 *   totalAppointments, totalRevenue
 * 
 * Backend DoctorPerformanceResponse fields:
 *   doctorId, completedAppointments, revenue, averageRating
 */
export const reportService = {
  dailyStats: (date: string) =>
    USE_MOCK
      ? Promise.resolve<AppointmentStatisticResponse>({
          total: 0, pending: 0, confirmed: 0, completed: 0, cancelled: 0, noShow: 0,
        })
      : apiGet<AppointmentStatisticResponse>('/admin/reports/appointments/daily', { params: { date } }),

  monthlyStats: (year: number, month: number) =>
    USE_MOCK
      ? Promise.resolve<AppointmentStatisticResponse>({
          total: 0, pending: 0, confirmed: 0, completed: 0, cancelled: 0, noShow: 0,
        })
      : apiGet<AppointmentStatisticResponse>('/admin/reports/appointments/monthly', {
          params: { year, month },
        }),

  revenueStats: (from: string, to: string) =>
    USE_MOCK
      ? Promise.resolve<RevenueStatisticResponse>({ totalAppointments: 0, totalRevenue: 0 })
      : apiGet<RevenueStatisticResponse>('/admin/reports/revenue', { params: { from, to } }),

  doctorPerformance: (doctorId: string) =>
    USE_MOCK
      ? Promise.resolve<DoctorPerformanceResponse>({
          doctorId: Number(doctorId), completedAppointments: 0, revenue: 0, averageRating: 0,
        })
      : apiGet<DoctorPerformanceResponse>(`/admin/reports/doctors/${doctorId}`),
};
