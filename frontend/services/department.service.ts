import { apiDelete, apiGet, apiPost, apiPut } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { Specialty, PaginatedResponse } from '@/types';

export const specialtyService = {
  list: () => (USE_MOCK ? mockApi.getSpecialties() : apiGet<Specialty[]>('/api/v1/specialties')),
  listPaged: (params: Record<string, unknown> = {}) =>
    USE_MOCK ? mockApi.getSpecialtiesPaged(params) : apiGet<PaginatedResponse<Specialty>>('/api/v1/specialties', { params }),
  create: (input: Partial<Specialty>) =>
    USE_MOCK ? mockApi.createSpecialty(input) : apiPost<Specialty>('/api/v1/admin/specialties', input),
  update: (id: string, input: Partial<Specialty>) =>
    USE_MOCK ? mockApi.updateSpecialty(id, input) : apiPut<Specialty>(`/api/v1/admin/specialties/${id}`, input),
  remove: (id: string) =>
    USE_MOCK ? mockApi.deleteSpecialty(id) : apiDelete<void>(`/api/v1/admin/specialties/${id}`),
};
