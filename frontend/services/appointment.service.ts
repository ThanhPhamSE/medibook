import { apiDelete, apiGet, apiPost, apiPut, api } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { Appointment, AppointmentStatus, AppointmentStatsResponse, PaginatedResponse } from '@/types';

function mapPageResponse(response: any, cleanParams: Record<string, unknown> = {}) {
  let items: any[] = [];
  let totalPages = 1;
  let totalElements = 0;
  let currentPage = 0;

  const page = response?.data ?? response;
  if (page?.content !== undefined) {
    items = page.content ?? [];
    totalPages = page.totalPages ?? 1;
    totalElements = page.totalElements ?? items.length;
    currentPage = page.pageable?.pageNumber ?? page.number ?? 0;
  } else if (Array.isArray(response)) {
    items = response;
    totalElements = items.length;
  }

  const mappedContent = items.map((apt: any) => ({
    ...apt,
    date: apt.startDatetime?.split('T')[0] || '',
    startTime: apt.startDatetime || '',
    endTime: apt.endDatetime || '',
    notes: apt.note,
  }));

  return {
    content: mappedContent,
    totalPages,
    totalElements,
    page: currentPage,
    size: (cleanParams.size as number) ?? 10,
  };
}

export const appointmentService = {
  list: async (params: Record<string, unknown> = {}) => {
    if (USE_MOCK) {
      return mockApi.getAppointments(params);
    }
    // Backend gets current user from context, so we only send pagination and filter params
    const { userId, role, ...apiParams } = params as any;
    const cleanParams = Object.fromEntries(
      Object.entries(apiParams).filter(([_, v]) => v !== undefined && v !== null && v !== '')
    );
    const response = await apiGet<any>('/appointments/me', { params: cleanParams });
    return mapPageResponse(response, cleanParams);
  },

  stats: async (): Promise<AppointmentStatsResponse> => {
    if (USE_MOCK) {
      return { total: 11, completed: 2, pending: 5, cancelled: 3 };
    }
    const response = await apiGet<any>('/appointments/me/stats');
    return response?.data ?? response;
  },


  // GET /appointments/doctor/today — chỉ dành cho DOCTOR/ADMIN
  today: async (params: Record<string, unknown> = {}) => {
    if (USE_MOCK) {
      return mockApi.getAppointments(params);
    }
    const cleanParams = Object.fromEntries(
      Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== '')
    );
    const response = await apiGet<any>('/appointments/doctor/today', { params: cleanParams });
    return mapPageResponse(response, cleanParams);
  },

  // GET /appointments/doctor/week — dành cho DOCTOR/ADMIN
  week: async (params: Record<string, unknown> = {}) => {
    if (USE_MOCK) {
      return mockApi.getAppointments(params);
    }
    const cleanParams = Object.fromEntries(
      Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== '')
    );
    const response = await apiGet<any>('/appointments/doctor/week', { params: cleanParams });
    return mapPageResponse(response, cleanParams);
  },

  get: (id: string) =>
    USE_MOCK ? mockApi.getAppointment(id) : apiGet<any>(`/appointments/${id}`).then(response => {
      const data = response?.data || response;
      return {
        ...data,
        date: data.startDatetime?.split('T')[0] || '',
        startTime: data.startDatetime || '',
        endTime: data.endDatetime || '',
        notes: data.note,
      };
    }),

  create: (input: Partial<Appointment> & { doctorId: string; date: string; startTime: string }) => {
    if (USE_MOCK) {
      return mockApi.createAppointment(input);
    }
    const backendInput = {
      doctorId: Number(input.doctorId),
      startDateTime: input.startTime,
      note: input.notes,
    };
    return apiPost<any>('/appointments', backendInput);
  },

  confirm: (id: string) =>
    USE_MOCK ? Promise.resolve({} as Appointment) : api.patch(`/appointments/${id}/confirm`).then(() => ({} as Appointment)),

  complete: (id: string) =>
    USE_MOCK ? Promise.resolve({} as Appointment) : api.patch(`/appointments/${id}/complete`).then(() => ({} as Appointment)),

  noShow: (id: string) =>
    USE_MOCK ? Promise.resolve({} as Appointment) : api.patch(`/appointments/${id}/no-show`).then(() => ({} as Appointment)),

  reschedule: (id: string, input: { newStartDatetime: string }) =>
    USE_MOCK ? mockApi.rescheduleAppointment(id, '', '') : apiPut<any>(`/appointments/${id}/reschedule`, input),

  cancel: (id: string, reason?: string) =>
    USE_MOCK ? Promise.resolve({} as Appointment) : api.put(`/appointments/${id}/cancel`, null, { params: { reason } }).then(() => ({} as Appointment)),

  getBookedSlots: (doctorId: string, date: string) =>
    USE_MOCK ? Promise.resolve([]) : apiGet<any>(`/appointments/doctor/${doctorId}/booked`, { params: { date } }),

  getAllBookings: async (params: Record<string, unknown> = {}) => {
    if (USE_MOCK) return mockApi.getAppointments(params);
    const response = await apiGet<any>('/appointments/admin/bookings', { params });
    let items: any[] = [];
    let pagination: any = {};

    if (response?.items !== undefined) {
      items = response.items ?? [];
      pagination = response.pagination ?? {};
    } else if (response?.content !== undefined) {
      items = response.content ?? [];
      pagination = {
        page: response.pageable?.pageNumber ?? response.page ?? 0,
        size: response.size ?? 10,
        totalElements: response.totalElements ?? 0,
        totalPages: response.totalPages ?? 1,
      };
    } else if (Array.isArray(response)) {
      items = response;
      pagination = { page: 0, size: items.length, totalElements: items.length, totalPages: 1 };
    }

    const mappedContent = items.map((apt: any) => ({
      ...apt,
      id: String(apt.id),
      patientId: String(apt.patientId),
      doctorId: String(apt.doctorId),
      date: apt.startDatetime ? apt.startDatetime.split('T')[0] : '',
      startTime: apt.startDatetime ?? '',
      endTime: apt.endDatetime ?? '',
      notes: apt.note,
      consultationFee: apt.consultationFee ? Number(apt.consultationFee) : undefined,
    }));

    return {
      content: mappedContent,
      totalElements: pagination.totalElements ?? 0,
      totalPages: pagination.totalPages ?? 1,
      page: pagination.page ?? 0,
      size: pagination.size ?? 10,
    };
  },
};