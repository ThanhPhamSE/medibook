export const APP_CONFIG = {
  name: 'MediBook',
  fullName: 'MediBook Health Systems',
  tagline: 'Your health, one tap away.',
  description:
    'Book appointments with verified doctors, manage your medical records, and access quality healthcare from anywhere.',
  supportEmail: 'support@medibook.health',
  supportPhone: '+1 (800) 555-0199',
  address: '1200 Wellness Blvd, Suite 400, San Francisco, CA 94103',
  apiBaseUrl: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1',
  version: '1.0.0',
} as const;

export const STORAGE_KEYS = {
  accessToken: 'medibook.access_token',
  refreshToken: 'medibook.refresh_token',
  user: 'medibook.user',
  theme: 'medibook.theme',
} as const;

export const NAV_ROLES = ['CUSTOMER', 'DOCTOR', 'ADMIN'] as const;
