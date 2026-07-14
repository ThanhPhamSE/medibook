import { apiDelete, apiGet, apiPost, apiPut } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { Schedule } from '@/types';

export const scheduleService = {
  list: async (doctorId?: string): Promise<Schedule[]> => {
    if (!doctorId) return [];
    if (USE_MOCK) return mockApi.getSchedules(doctorId);
    const response = await apiGet<any>(`/schedules/doctor/${doctorId}`);
    const patterns = response?.workingPartterns || response?.workingPatterns || [];
    return patterns.map((p: any) => ({
      id: String(p.id),
      doctorId: String(p.doctorId),
      doctorName: '',
      dayOfWeek: p.dayOfWeek,
      startTime: p.startTime ? p.startTime.substring(0, 5) : '',
      endTime: p.endTime ? p.endTime.substring(0, 5) : '',
      isAvailable: true,
    }));
  },

  create: (input: any) =>
    USE_MOCK ? mockApi.createSchedule(input) : apiPost<any>('/schedules/working-patterns', input),

  remove: (id: string) =>
    USE_MOCK ? Promise.resolve() : apiDelete<void>(`/schedules/working-patterns/${id}`),

  createWorkingPattern: (input: any) =>
    USE_MOCK ? mockApi.createSchedule(input) : apiPost<any>('/schedules/working-patterns', input),

  updateWorkingPattern: (id: string, input: any) =>
    USE_MOCK ? Promise.resolve({} as Schedule) : apiPut<any>(`/schedules/working-patterns/${id}`, input),

  deleteWorkingPattern: (id: string) =>
    USE_MOCK ? Promise.resolve() : apiDelete<void>(`/schedules/working-patterns/${id}`),

  createTimeOff: (input: any) =>
    USE_MOCK ? Promise.resolve({} as any) : apiPost<any>('/schedules/time-offs', input),

  updateTimeOff: (id: string, input: any) =>
    USE_MOCK ? Promise.resolve({} as any) : apiPut<any>(`/schedules/time-offs/${id}`, input),

  deleteTimeOff: (id: string) =>
    USE_MOCK ? Promise.resolve() : apiDelete<void>(`/schedules/time-offs/${id}`),

  generateSlots: (input: any) =>
    USE_MOCK ? Promise.resolve([]) : apiPost<any>('/schedules/slots', input),

  getDoctorSchedule: (doctorId: string) =>
    USE_MOCK ? mockApi.getSchedules(doctorId) : apiGet<any>(`/schedules/doctor/${doctorId}`),
};
