import { apiGet, apiPost } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { Review, PaginatedResponse, DoctorRatingResponse } from '@/types';

export const reviewService = {
  create: (input: Partial<Review>) =>
    USE_MOCK ? Promise.resolve({} as Review) : apiPost<any>('/reviews', input),

  getDoctorReviews: (doctorId: string, params: Record<string, unknown> = {}) =>
    USE_MOCK ? mockApi.getDoctorReviews(doctorId) : apiGet<any>(`/reviews/doctor/${doctorId}`, { params }),

  getDoctorRating: (doctorId: string) =>
    USE_MOCK ? Promise.resolve({ averageRating: 0, totalReviews: 0, ratingDistribution: {} } as DoctorRatingResponse) : apiGet<any>(`/reviews/doctor/${doctorId}/rating`),
};
