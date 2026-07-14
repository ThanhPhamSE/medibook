import { apiDelete, apiGet, apiPost, apiPut } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { Doctor, PaginatedResponse, Review } from '@/types';

/**
 * Map DoctorSummaryResponse / DoctorResponse từ backend sang Doctor type của FE.
 * Backend fields:
 *   - fullName (không có firstName/lastName riêng)
 *   - experienceYears (FE dùng yearsOfExperience)
 *   - averageRating (FE dùng rating)
 *   - totalReviews (FE dùng reviewCount)
 *   - active: boolean (FE dùng status: 'ACTIVE'|'INACTIVE')
 *   - biography (FE dùng bio)
 *   - degree → education array
 *   - profileImage (FE dùng avatarUrl)
 */
function mapDoctorItem(item: any): Doctor {
  return {
    ...item,
    id: String(item.id),
    specialtyId: String(item.specialtyId ?? ''),
    yearsOfExperience: item.experienceYears ?? item.yearsOfExperience ?? 0,
    rating: Number(item.averageRating ?? item.rating ?? 0),
    reviewCount: item.totalReviews ?? item.reviewCount ?? 0,
    bio: item.biography ?? item.bio ?? '',
    education: item.degree ? [item.degree] : (item.education ?? []),
    specializations: item.specializations ?? [],
    languages: item.languages ?? [],
    status: item.active === true ? 'ACTIVE' : item.active === false ? 'INACTIVE' : (item.status ?? 'ACTIVE'),
    avatarUrl: item.profileImage ?? item.avatarUrl ?? null,
    consultationFee: Number(item.consultationFee ?? 0),
    // fullName được giữ lại để FE dùng khi cần split
    fullName: item.fullName ?? `${item.firstName ?? ''} ${item.lastName ?? ''}`.trim(),
    // Provide defaults for missing required fields
    firstName: item.firstName ?? '',
    lastName: item.lastName ?? '',
    email: item.email ?? '',
    phone: item.phone ?? undefined,
    createdAt: item.createdAt ?? new Date().toISOString(),
    // Map backend degree to frontend degree field for form
    degree: item.degree ?? '',
    // Map backend biography to frontend biography field for form
    biography: item.biography ?? '',
  } as Doctor;
}

/**
 * Normalize backend PageResponse<T> (items + pagination) → PaginatedResponse<Doctor>
 */
function normalizePageResponse(response: any): PaginatedResponse<Doctor> {
  let items: any[] = [];
  let pagination: any = {};

  if (response?.items !== undefined) {
    // PageResponse { items: [], pagination: { page, size, totalElements, totalPages } }
    items = response.items ?? [];
    pagination = response.pagination ?? {};
  } else if (response?.content !== undefined) {
    // Spring Page { content: [], totalElements, totalPages, pageable: { pageNumber } }
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
    content: items.map(mapDoctorItem),
    totalElements: pagination.totalElements ?? 0,
    totalPages: pagination.totalPages ?? 1,
    page: pagination.page ?? 0,
    size: pagination.size ?? 10,
  };
}

export const doctorService = {
  list: async (params: Record<string, unknown> = {}): Promise<PaginatedResponse<Doctor>> => {
    // Map frontend parameter names to backend parameter names
    const { search, ...otherParams } = params;
    const backendParams = {
      ...otherParams,
      keyword: search,
      specialtyId: params.specialtyId,
      minExperience: params.minExperience,
      maxExperience: params.maxExperience,
      minFee: params.minFee,
      maxFee: params.maxFee,
      minRating: params.minRating,
      page: params.page,
      size: params.size,
      sort: params.sort,
    };

    if (USE_MOCK) return mockApi.getDoctors(params) as any;

    const response = await apiGet<any>('/doctors', { params: backendParams });
    return normalizePageResponse(response);
  },

  get: async (id: string): Promise<Doctor> => {
    if (USE_MOCK) return mockApi.getDoctor(id) as any;
    const response = await apiGet<any>(`/doctors/${id}`);
    return mapDoctorItem(response);
  },

  reviews: async (id: string): Promise<Review[]> => {
    if (USE_MOCK) return mockApi.getDoctorReviews(id) as any;
    const response = await apiGet<any>(`/reviews/doctor/${id}`);
    // Backend trả ApiResponse<Page<ReviewResponse>>
    // apiGet đã unwrap data → nhận được Page hoặc PageResponse
    if (response?.items) return response.items;
    if (response?.content) return response.content;
    if (Array.isArray(response)) return response;
    return [];
  },

  timeSlots: async (doctorId: string, date: string) => {
    if (USE_MOCK) return mockApi.getTimeSlots(doctorId, date);
    const response = await apiPost<any>('/schedules/slots', { doctorId: Number(doctorId), date });
    let slots: any[] = [];
    if (Array.isArray(response)) {
      slots = response;
    } else if (response?.content) {
      slots = response.content;
    } else if (response?.items) {
      slots = response.items;
    } else {
      slots = response ?? [];
    }
    return slots.map((slot: any) => ({
      startTime: slot.start ?? slot.startTime,
      endTime: slot.end ?? slot.endTime,
      available: slot.available,
    }));
  },

  create: (input: any) => {
    // Map frontend input to backend CreateDoctorRequest
    const backendInput = {
      userId: Number(input.userId),
      specialtyId: Number(input.specialtyId),
      degree: input.degree,
      experienceYears: input.experienceYears,
      consultationFee: input.consultationFee,
      biography: input.biography,
    };
    if (USE_MOCK) return mockApi.createDoctor(input);
    return apiPost<any>('/doctors', backendInput);
  },

  update: (id: string, input: any) => {
    // Map frontend input to backend UpdateDoctorRequest
    const backendInput = {
      specialtyId: Number(input.specialtyId),
      degree: input.degree,
      experienceYears: input.experienceYears,
      consultationFee: input.consultationFee,
      biography: input.biography,
    };
    if (USE_MOCK) return mockApi.updateDoctor(id, input);
    return apiPut<any>(`/doctors/${id}`, backendInput);
  },

  remove: (id: string) =>
    USE_MOCK ? mockApi.deleteDoctor(id) : apiDelete<void>(`/doctors/${id}`),
};
