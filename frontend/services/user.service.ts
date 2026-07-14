import { apiGet, apiPatch } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { User, PaginatedResponse } from '@/types';

/**
 * Normalize backend PageResponse<T> (items + pagination) → PaginatedResponse<T> (content + totalElements/Pages)
 * Backend AdminUserController trả thẳng PageResponse (không bọc ApiResponse)
 * nên apiGet sẽ nhận được { items, pagination } trực tiếp.
 */
function normalizePageResponse<T>(response: any): PaginatedResponse<T> {
  // Dạng PageResponse: { items: T[], pagination: { page, size, totalElements, totalPages } }
  if (response?.items !== undefined) {
    return {
      content: response.items ?? [],
      totalElements: response.pagination?.totalElements ?? 0,
      totalPages: response.pagination?.totalPages ?? 1,
      page: response.pagination?.page ?? 0,
      size: response.pagination?.size ?? 10,
    };
  }
  // Dạng Spring Page: { content: T[], totalElements, totalPages, ... }
  if (response?.content !== undefined) {
    return response as PaginatedResponse<T>;
  }
  // Fallback: wrap array
  if (Array.isArray(response)) {
    return { content: response, totalElements: response.length, totalPages: 1, page: 0, size: response.length };
  }
  return { content: [], totalElements: 0, totalPages: 1, page: 0, size: 10 };
}

export const userService = {
  list: async (params: Record<string, unknown> = {}): Promise<PaginatedResponse<User>> => {
    if (USE_MOCK) return mockApi.getUsers(params);
    // AdminUserController trả PageResponse<UserResponse> trực tiếp (không bọc ApiResponse)
    const response = await apiGet<any>('/admin/users', { params });
    return normalizePageResponse<User>(response);
  },

  patients: async (params: Record<string, unknown> = {}): Promise<PaginatedResponse<User>> => {
    if (USE_MOCK) return mockApi.getPatients(params);
    // Map frontend search to backend keyword
    const { search, ...otherParams } = params;
    const backendParams = {
      ...otherParams,
      keyword: search,
      role: 'CUSTOMER',
    };
    const response = await apiGet<any>('/admin/users', { params: backendParams });
    return normalizePageResponse<User>(response);
  },

  get: (id: string) =>
    USE_MOCK ? mockApi.getUsers({}) : apiGet<any>(`/admin/users/${id}`),

  updateStatus: (id: string, isActive: boolean) =>
    USE_MOCK
      ? mockApi.updateUserStatus(id, isActive)
      : apiPatch<void>(`/admin/users/${id}/${isActive ? 'activate' : 'deactivate'}`),

  updateProfile: (input: Partial<User>) =>
    USE_MOCK ? mockApi.updateProfile(input) : apiPatch<User>('/users/me', input),
};
