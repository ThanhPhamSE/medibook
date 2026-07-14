import { apiDelete, apiGet, apiPost, apiPut } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { Specialty, PaginatedResponse } from '@/types';

/**
 * Backend SpecialtyResponse: { id, name, description, createdAt, updatedAt }
 * FE Specialty type: { id, name, description, doctorCount, color, icon, createdAt }
 * 
 * doctorCount, color, icon không có trong backend DTO → default về 0/''.
 * Cần normalize response vì backend trả ApiResponse<PageResponse<SpecialtyResponse>>
 * và apiGet đã unwrap data → nhận được PageResponse: { items, pagination }.
 */
function normalizeSpecialty(item: any): Specialty {
  return {
    id: String(item.id),
    name: item.name ?? '',
    description: item.description ?? '',
    doctorCount: item.doctorCount ?? 0,
    icon: item.icon ?? '',
    color: item.color ?? '#14b8a6',
    createdAt: item.createdAt ?? '',
  };
}

function normalizePageResponse(response: any): PaginatedResponse<Specialty> {
  let items: any[] = [];
  let pagination: any = {};

  if (response?.items !== undefined) {
    // PageResponse { items: [], pagination: { page, size, totalElements, totalPages } }
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

  return {
    content: items.map(normalizeSpecialty),
    totalElements: pagination.totalElements ?? 0,
    totalPages: pagination.totalPages ?? 1,
    page: pagination.page ?? 0,
    size: pagination.size ?? 10,
  };
}

export const specialtyService = {
  list: async (params: Record<string, unknown> = {}): Promise<PaginatedResponse<Specialty>> => {
    if (USE_MOCK) return mockApi.getSpecialtiesPaged(params) as any;
    // SpecialtyController GET /api/v1/specialties
    // apiGet unwrap data → nhận PageResponse { items, pagination }
    const response = await apiGet<any>('/specialties', {
      params: { keyword: params.search, page: params.page, size: params.size },
    });
    return normalizePageResponse(response);
  },

  get: (id: string) =>
    USE_MOCK ? mockApi.getSpecialties() : apiGet<any>(`/specialties/${id}`),

  create: (input: Partial<Specialty>) =>
    USE_MOCK ? mockApi.createSpecialty(input) : apiPost<any>('/admin/specialties', input),

  update: (id: string, input: Partial<Specialty>) =>
    USE_MOCK ? mockApi.updateSpecialty(id, input) : apiPut<any>(`/admin/specialties/${id}`, input),

  remove: (id: string) =>
    USE_MOCK ? mockApi.deleteSpecialty(id) : apiDelete<void>(`/admin/specialties/${id}`),

  restore: (id: string) =>
    USE_MOCK ? Promise.resolve({} as Specialty) : apiPost<any>(`/admin/specialties/${id}/restore`),

  getDeleted: (params: Record<string, unknown> = {}) =>
    USE_MOCK ? mockApi.getSpecialtiesPaged(params) : apiGet<any>('/admin/specialties/deleted', { params }),
};
