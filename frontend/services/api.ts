import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios';
import Cookies from 'js-cookie';
import { APP_CONFIG, STORAGE_KEYS } from '@/constants/app';
import type { ApiError, PaginatedResponse } from '@/types';

const BASE_URL = APP_CONFIG.apiBaseUrl;

export const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (typeof window !== 'undefined') {
    const token = Cookies.get(STORAGE_KEYS.accessToken);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

let isRefreshing = false;
let queue: Array<(token: string | null) => void> = [];

function flushQueue(token: string | null) {
  queue.forEach((cb) => cb(token));
  queue = [];
}

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError<ApiError>) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    const status = error.response?.status;

    if (status === 401 && original && !original._retry && typeof window !== 'undefined') {
      original._retry = true;
      const refreshToken = Cookies.get(STORAGE_KEYS.refreshToken);

      if (!refreshToken) {
        clearAuth();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          queue.push((token) => {
            if (!token) {
              reject(error);
              return;
            }
            if (original.headers) original.headers.Authorization = `Bearer ${token}`;
            resolve(api(original));
          });
        });
      }

      isRefreshing = true;
      try {
        const { data } = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken });
        const newAccess: string = data.tokens.accessToken;
        Cookies.set(STORAGE_KEYS.accessToken, newAccess, { expires: 7 });
        if (data.tokens.refreshToken) {
          Cookies.set(STORAGE_KEYS.refreshToken, data.tokens.refreshToken, { expires: 30 });
        }
        flushQueue(newAccess);
        if (original.headers) original.headers.Authorization = `Bearer ${newAccess}`;
        return api(original);
      } catch (e) {
        flushQueue(null);
        clearAuth();
        return Promise.reject(e);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

function clearAuth() {
  if (typeof window === 'undefined') return;
  Cookies.remove(STORAGE_KEYS.accessToken);
  Cookies.remove(STORAGE_KEYS.refreshToken);
  Cookies.remove(STORAGE_KEYS.user);
  if (window.location.pathname.startsWith('/dashboard') || window.location.pathname.startsWith('/admin') || window.location.pathname.startsWith('/doctor')) {
    window.location.href = '/login';
  }
}

/**
 * Thrown when the backend responds with HTTP 2xx but the JSON body itself
 * signals a failure (e.g. `{ status: 400, error: true, message: "..." }`).
 * Some Spring @ControllerAdvice handlers forget to set the HTTP status on the
 * ResponseEntity, so axios never rejects on its own — without this check the
 * mutation would silently resolve as "success" and no error toast would ever
 * be shown to the user.
 */
export class ApiRequestError extends Error {
  status?: number;
  code?: string;
  data?: unknown;

  constructor(message: string, opts?: { status?: number; code?: string; data?: unknown }) {
    super(message);
    this.name = 'ApiRequestError';
    this.status = opts?.status;
    this.code = opts?.code;
    this.data = opts?.data;
  }
}

// Recognizes the app's error-body shape, e.g. { status: 400, error: true, message: "..." }
// or { success: false, message: "..." }. Adjust this if the backend's error envelope changes.
function isErrorBody(body: unknown): body is { message?: string; code?: string; status?: number } {
  if (!body || typeof body !== 'object') return false;
  const b = body as Record<string, unknown>;
  return b.error === true || b.success === false;
}

function assertNotErrorBody(body: unknown, fallback: string) {
  if (isErrorBody(body)) {
    throw new ApiRequestError(body.message || fallback, {
      status: body.status,
      code: (body as Record<string, unknown>).code as string | undefined,
      data: body,
    });
  }
}

export async function apiGet<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await api.get(url, config);
  assertNotErrorBody(response.data, 'Yêu cầu thất bại');
  // Handle different response structures
  // Some endpoints return { data: T }, others return T directly, others return { content: T, ... }, others return { items: T, pagination: ... }
  if (response.data && typeof response.data === 'object') {
    if ('data' in response.data) {
      const innerData = response.data.data;
      // Check if inner data has items/pagination structure
      if (innerData && typeof innerData === 'object' && 'items' in innerData) {
        return innerData as T;
      }
      return innerData as T;
    }
    if ('content' in response.data) {
      return response.data as T;
    }
    if ('items' in response.data) {
      return response.data as T;
    }
  }
  // If the response is directly the data we want
  return response.data as T;
}

export async function apiPost<T>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const { data } = await api.post<{ data: T }>(url, body, config);
  assertNotErrorBody(data, 'Yêu cầu thất bại');
  return data.data;
}

export async function apiPut<T>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const { data } = await api.put<{ data: T }>(url, body, config);
  assertNotErrorBody(data, 'Yêu cầu thất bại');
  return data.data;
}

export async function apiDelete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const { data } = await api.delete<{ data: T }>(url, config);
  assertNotErrorBody(data, 'Yêu cầu thất bại');
  return data.data;
}

export async function apiPatch<T>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const { data } = await api.patch<{ data: T }>(url, body, config);
  assertNotErrorBody(data, 'Yêu cầu thất bại');
  return data.data;
}

export function toPaginated<T>(content: T[], page = 0, size = 10): PaginatedResponse<T> {
  const start = page * size;
  const sliced = content.slice(start, start + size);
  return {
    content: sliced,
    totalElements: content.length,
    totalPages: Math.max(1, Math.ceil(content.length / size)),
    page,
    size,
  };
}

export function extractApiError(err: unknown, fallback = 'Something went wrong'): string {
  // Application-level error surfaced via a 2xx response body (see ApiRequestError above).
  if (err instanceof ApiRequestError) {
    const errorCode = err.code;
    if (errorCode && ERROR_CODE_MESSAGES[errorCode]) {
      return ERROR_CODE_MESSAGES[errorCode];
    }
    return err.message || fallback;
  }

  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ApiError | undefined;

    // Check for error field in response (backend error format)
    if (data?.error === true && data?.message) {
      return data.message;
    }

    // Check for field-specific errors in data object (backend validation format)
    if (data?.data && typeof data.data === 'object' && !Array.isArray(data.data)) {
      const fieldErrors = Object.entries(data.data)
        .map(([field, message]) => `${message}`)
        .join(', ');
      if (fieldErrors) return fieldErrors;
    }

    // Check for field-specific errors in fieldErrors property
    if (data?.fieldErrors) {
      const fieldErrors = Object.entries(data.fieldErrors)
        .map(([field, message]) => `${field}: ${message}`)
        .join(', ');
      return fieldErrors || data.message || err.message || fallback;
    }

    // Check for specific error codes and provide user-friendly messages
    const errorCode = data?.code;
    if (errorCode && ERROR_CODE_MESSAGES[errorCode]) {
      return ERROR_CODE_MESSAGES[errorCode];
    }

    return data?.message || err.message || fallback;
  }

  if (err instanceof Error) return err.message;
  return fallback;
}

const ERROR_CODE_MESSAGES: Record<string, string> = {
  AUTHENTICATION_FAILED: 'Email hoặc mật khẩu không chính xác',
  USER_NOT_FOUND: 'Không tìm thấy người dùng',
  EMAIL_ALREADY_EXISTS: 'Email này đã được sử dụng',
  INVALID_TOKEN: 'Token không hợp lệ, vui lòng đăng nhập lại',
  TOKEN_EXPIRED: 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại',
  INVALID_CREDENTIALS: 'Thông tin đăng nhập không chính xác',
  ACCOUNT_LOCKED: 'Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên',
  ACCOUNT_DISABLED: 'Tài khoản đã bị vô hiệu hóa',
  PERMISSION_DENIED: 'Bạn không có quyền thực hiện hành động này',
  RESOURCE_NOT_FOUND: 'Không tìm thấy tài nguyên yêu cầu',
  APPOINTMENT_NOT_FOUND: 'Không tìm thấy lịch hẹn',
  APPOINTMENT_ALREADY_CANCELLED: 'Lịch hẹn này đã bị hủy',
  APPOINTMENT_ALREADY_COMPLETED: 'Lịch hẹn này đã hoàn thành',
  APPOINTMENT_CANNOT_CANCEL: 'Không thể hủy lịch hẹn này',
  INVALID_DATE_TIME: 'Thời gian không hợp lệ',
  SLOT_NOT_AVAILABLE: 'Khung giờ này đã được đặt',
  DOCTOR_NOT_AVAILABLE: 'Bác sĩ không khả dụng vào thời gian này',
  INVALID_SPECIALTY: 'Chuyên khoa không hợp lệ',
  INVALID_DOCTOR: 'Bác sĩ không hợp lệ',
  MEDICAL_RECORD_NOT_FOUND: 'Không tìm thấy bệnh án',
  REVIEW_ALREADY_EXISTS: 'Bạn đã đánh giá bác sĩ này',
  INVALID_RATING: 'Đánh giá phải từ 1 đến 5 sao',
  NETWORK_ERROR: 'Lỗi kết nối mạng, vui lòng kiểm tra internet',
  SERVER_ERROR: 'Lỗi máy chủ, vui lòng thử lại sau',
  BAD_REQUEST: 'Yêu cầu không hợp lệ',
  CONFLICT: 'Dữ liệu đã tồn tại',
  FORBIDDEN: 'Bạn không có quyền truy cập',
  UNAUTHORIZED: 'Bạn cần đăng nhập để thực hiện hành động này',
};
