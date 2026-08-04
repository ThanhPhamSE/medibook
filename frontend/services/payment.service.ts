import { api } from '@/services/api';
import { USE_MOCK } from '@/services/mock-api';

export const paymentService = {
  createLink: async (appointmentId: number | string) => {
    if (USE_MOCK) {
      return {
        checkoutUrl: 'https://checkout.payos.vn/payment-link-details',
      };
    }
    // Backend trả về PaymentLinkResponse trực tiếp (không có wrapper { data: ... })
    // nên dùng api.post thay vì apiPost để tránh bị double-unwrap thành undefined
    const response = await api.post<any>(`/payments/create-link/${appointmentId}`);
    return response.data;
  },

  getStatus: async (appointmentId: number | string) => {
    if (USE_MOCK) {
      return { status: 'NOT_FOUND' };
    }
    const response = await api.get<any>(`/payments/status/${appointmentId}`);
    return response.data;
  },
};
