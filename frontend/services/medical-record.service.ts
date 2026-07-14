import { apiDelete, apiGet, apiPost, apiPut } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { MedicalRecord, PaginatedResponse } from '@/types';

export const medicalRecordService = {
  // Customer
  list: (params: Record<string, unknown> = {}) =>
    USE_MOCK
      ? mockApi.getMedicalRecords(params)
      : apiGet<PaginatedResponse<MedicalRecord>>('/medical-records/me', { params }),

  // Get detail
  get: (id: string) =>
    USE_MOCK
      ? mockApi.getMedicalRecord(id)
      : apiGet<MedicalRecord>(`/medical-records/${id}`),

  // Doctor
  create: (input: Partial<MedicalRecord>) =>
    USE_MOCK
      ? mockApi.createMedicalRecord(input)
      : apiPost<MedicalRecord>('/medical-records', input),

  // Doctor
  update: (id: string, input: Partial<MedicalRecord>) =>
    USE_MOCK
      ? Promise.resolve({} as MedicalRecord)
      : apiPut<MedicalRecord>(`/medical-records/${id}`, input),

  // Doctor
  remove: (id: string) =>
    USE_MOCK
      ? Promise.resolve()
      : apiDelete<void>(`/medical-records/${id}`),

  // Doctor
  getDoctorRecords: (params: Record<string, unknown> = {}) =>
    USE_MOCK
      ? mockApi.getMedicalRecords(params)
      : apiGet<PaginatedResponse<MedicalRecord>>('/medical-records/doctor', { params }),

  // Admin
  getAllRecords: (params: Record<string, unknown> = {}) =>
    USE_MOCK
      ? mockApi.getMedicalRecords(params)
      : apiGet<PaginatedResponse<MedicalRecord>>('/medical-records', { params }),
};