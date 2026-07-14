import { apiGet, apiPost } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type {
  AppointmentStatisticResponse,
  AppointmentTrendResponse,
  DashboardStatsResponse,
  RevenueTrendResponse,
  RevenueStatisticResponse,
  ChartPoint,
  DashboardStats,
  Notification,
  AdminDashboardResponse,
} from '@/types';

export const dashboardService = {
  stats: async (role: string, from: string, to: string): Promise<DashboardStats> => {
    if (USE_MOCK) return mockApi.getDashboardStats(role) as any;

    try {

      const [dashStats, apptStats, revStats] = await Promise.allSettled([
        apiGet<DashboardStatsResponse>('/admin/reports/dashboard-stats', { params: { from, to } }),
        // /appointments/range trả về thống kê theo mọi trạng thái cho khoảng from→to
        apiGet<AppointmentStatisticResponse>('/admin/reports/appointments/range', { params: { from, to } }),
        apiGet<RevenueStatisticResponse>('/admin/reports/revenue', { params: { from, to } }),
      ]);

      const ds: DashboardStatsResponse =
        dashStats.status === 'fulfilled'
          ? dashStats.value
          : { totalPatients: 0, totalDoctors: 0, totalAppointments: 0, totalRevenue: 0 };

      const appt: AppointmentStatisticResponse =
        apptStats.status === 'fulfilled'
          ? apptStats.value
          : { total: 0, pending: 0, confirmed: 0, completed: 0, cancelled: 0, noShow: 0 };

      const rev: RevenueStatisticResponse =
        revStats.status === 'fulfilled'
          ? revStats.value
          : { totalAppointments: 0, totalRevenue: 0 };

      // totalRevenue: ưu tiên dashboard-stats (nếu > 0), fallback /revenue
      const totalRevenue =
        ds.totalRevenue && Number(ds.totalRevenue) > 0
          ? Number(ds.totalRevenue)
          : Number(rev.totalRevenue ?? 0);

      return {
        totalPatients:          ds.totalPatients ?? 0,
        totalDoctors:           ds.totalDoctors  ?? 0,
        // Stat card "Appointments" = tổng appointment trong kỳ (mọi trạng thái)
        totalAppointments:      appt.total ?? 0,
        totalRevenue,
        appointmentsToday:      appt.total       ?? 0,
        pendingAppointments:    appt.pending      ?? 0,
        completedAppointments:  appt.completed    ?? 0,
        cancelledAppointments:  appt.cancelled    ?? 0,
        confirmedAppointments:  appt.confirmed    ?? 0,
        noShowAppointments:     appt.noShow       ?? 0,
        revenueChange:          0,
        appointmentsChange:     0,
        patientsChange:         0,
      };
    } catch {
      return {
        totalPatients: 0, totalDoctors: 0, totalAppointments: 0, totalRevenue: 0,
        appointmentsToday: 0, pendingAppointments: 0, completedAppointments: 0,
        cancelledAppointments: 0, confirmedAppointments: 0, noShowAppointments: 0,
        revenueChange: 0, appointmentsChange: 0, patientsChange: 0,
      };
    }
  },

  /**
   * Appointment trend: GET /admin/reports/appointments/trend?from&to
   * List<AppointmentTrendResponse> { date: LocalDate, total: long }
   */
  appointmentTrend: async (from: string, to: string): Promise<ChartPoint[]> => {
    if (USE_MOCK) return mockApi.getAppointmentTrend() as any;

    const data = await apiGet<AppointmentTrendResponse[]>('/admin/reports/appointments/trend', {
      params: { from, to },
    });

    if (!Array.isArray(data)) return [];
    return data.map((d) => ({
      label: d.date,
      value: Number(d.total ?? 0),
    }));
  },

  /**
   * Revenue trend: GET /admin/reports/revenue/trend?from&to
   * List<RevenueTrendResponse> { date: LocalDate, total: BigDecimal }
   */
  revenueTrend: async (from: string, to: string): Promise<ChartPoint[]> => {
    if (USE_MOCK) return mockApi.getRevenueTrend() as any;

    const data = await apiGet<RevenueTrendResponse[]>('/admin/reports/revenue/trend', {
      params: { from, to },
    });

    if (!Array.isArray(data)) return [];
    return data.map((d) => ({
      label: d.date,
      value: Number(d.total ?? 0),
    }));
  },

  specialtyDistribution: async (): Promise<ChartPoint[]> => {
    if (USE_MOCK) return mockApi.getSpecialtyDistribution() as any;
    const data = await apiGet<any>('/specialties', { params: { size: 50 } });
    const items: any[] = data?.items ?? data?.content ?? (Array.isArray(data) ? data : []);
    return items.map((s: any) => ({ label: s.name ?? '', value: s.doctorCount ?? 0 }));
  },

  topDoctors: async (): Promise<ChartPoint[]> => {
    if (USE_MOCK) return mockApi.getTopDoctors() as any;
    const data = await apiGet<any>('/doctors', { params: { size: 10 } });
    const items: any[] = data?.items ?? data?.content ?? (Array.isArray(data) ? data : []);
    return items.map((d: any) => ({ label: d.fullName ?? '', value: Number(d.totalReviews ?? 0) }));
  },

  adminDashboard: async (from: string, to: string): Promise<AdminDashboardResponse> => {
    if (USE_MOCK) {
      // Mock fallback - combine existing mock data
      const [dashStats, apptTrend, revTrend, specDist, topDocs] = await Promise.all([
        mockApi.getDashboardStats('ADMIN'),
        mockApi.getAppointmentTrend(),
        mockApi.getRevenueTrend(),
        mockApi.getSpecialtyDistribution(),
        mockApi.getTopDoctors(),
      ]);
      return {
        totalPatients: dashStats.totalPatients,
        totalDoctors: dashStats.totalDoctors,
        totalAppointments: dashStats.totalAppointments,
        totalRevenue: dashStats.totalRevenue,
        appointmentTrend: apptTrend.map((d: any) => ({ date: d.label, total: d.value })),
        revenueTrend: revTrend.map((d: any) => ({ date: d.label, total: d.value })),
        specialtyDistribution: specDist,
        topDoctors: topDocs,
      } as any;
    }

    return apiGet<AdminDashboardResponse>('/admin/reports/admin-dashboard', { params: { from, to } });
  },
};

export const notificationService = {
  list: () => (USE_MOCK ? mockApi.getNotifications() : Promise.resolve([])), // Backend endpoint not implemented yet
  markRead: (id: string) =>
    USE_MOCK ? mockApi.markNotificationRead(id) : Promise.resolve(), // Backend endpoint not implemented yet
  markAllRead: () =>
    USE_MOCK ? mockApi.markAllNotificationsRead() : Promise.resolve(), // Backend endpoint not implemented yet
};

export const adminService = {
  auditLogs: async (params: Record<string, unknown> = {}) => {
    if (USE_MOCK) return mockApi.getAuditLogs(params);

    const res = await apiGet<any>('/admin/audit-logs', { params });

    return {
      content: res.items,
      page: res.pagination.page,
      size: res.pagination.size,
      totalElements: res.pagination.totalElements,
      totalPages: res.pagination.totalPages,
    };
  },

  roles: () =>
    USE_MOCK ? mockApi.getRoles() : apiGet<any[]>('/admin/roles'),

  permissions: () =>
    USE_MOCK ? mockApi.getPermissions() : apiGet<any[]>('/admin/permissions'),
};