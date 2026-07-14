import { apiGet, apiPost, apiPut } from '@/services/api';
import { mockApi, USE_MOCK } from '@/services/mock-api';
import type { LoginResponse, User } from '@/types';

export const authService = {
  login: (email: string, password: string) =>
    USE_MOCK
      ? mockApi.login(email)
      : apiPost<LoginResponse>('/auth/login', { email, password }),

  register: (input: {
    fullName: string;
    email: string;
    password: string;
    phone: string;
    gender: string;
    birthDate: string;
  }) =>
    USE_MOCK
      ? mockApi.register(input)
      : apiPost<User>('/auth/register', input),

  me: () => 
    USE_MOCK 
      ? mockApi.me() 
      : apiGet<User>('/auth/me'),

  updateProfile: (input: {
    fullName?: string;
    email?: string;
    phone?: string;
    gender?: string;
    birthDate?: string;
    profileImage?: string;
  }) =>
    USE_MOCK
      ? mockApi.me() // Lưu ý: Chỗ này mock đang trả về .me(), nếu có mockApi.updateProfile(input) thì nên đổi lại nhé
      : apiPut<User>('/users/me', input),

  refresh: (refreshToken: string) =>
    USE_MOCK
      ? mockApi.login('admin@medibook.health')
      : apiPost<LoginResponse>('/auth/refresh-token', { refreshToken }),

  logout: (refreshToken: string) =>
    USE_MOCK
      ? Promise.resolve()
      : apiPost<void>('/auth/logout', { refreshToken }),

  forgotPassword: (email: string) =>
    USE_MOCK
      ? mockApi.forgotPassword()
      : apiPost<void>('/auth/forgot-password', { email }),

  resetPassword: (resetToken: string, newPassword: string, confirmPassword: string) =>
    USE_MOCK
      ? Promise.resolve()
      : apiPost<void>('/auth/reset-password', { resetToken, newPassword, confirmPassword }),

  verifyEmail: (token: string) =>
    USE_MOCK
      ? mockApi.verifyEmail()
      : apiPost<void>('/auth/verify-email', null, { params: { token } }),

  changePassword: (input: { currentPassword: string; newPassword: string; confirmPassword: string; }) =>
    USE_MOCK
      ? mockApi.changePassword()
      : apiPost<void>('/auth/change-password', input),

  logoutAll: () =>
    USE_MOCK 
      ? Promise.resolve() 
      : apiPost<void>('/auth/logout-all'),

  resendVerification: (email: string) =>
    USE_MOCK
      ? Promise.resolve()
      : apiPost<void>('/auth/resend-verification', { email }),
};