export type UserRole = 'CUSTOMER' | 'DOCTOR' | 'ADMIN';

export interface User {
  id: number;
  email: string;
  fullName: string;

  phone?: string | null;

  gender?:
    | 'MALE'
    | 'FEMALE'
    | 'OTHER'
    | null;

  birthDate?: string | null;

  profileImage?: string | null;

  isActive?: boolean;

  roleId?: number;

  roleName?: UserRole;

  doctorId?: number | string;
}


export interface LoginResponse {
  userId: number;

  email: string;

  fullName: string;

  role: UserRole;


  accessToken: string;

  refreshToken: string;

  tokenType: string;

  accessTokenExpiresAt: number;

  refreshTokenExpiresAt: number;

  issuedAt: number;
}

export interface AuthResponse {
  user: User;
  tokens: {
    accessToken: string;
    refreshToken: string;
    expiresIn: number;
  };
}

export interface AuthTokens {
  accessToken: string;

  refreshToken: string;

  expiresIn: number;
}

export interface Specialty {
  id: string;
  name: string;
  description: string;
  icon?: string;
  doctorCount: number;
  color?: string;
  createdAt: string;
}

export interface Specialization {
  id: string;
  name: string;
  specialtyId: string;
  description?: string;
}

export interface Doctor {
  id: string;
  fullName: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  avatarUrl?: string | null;
  bio: string;
  biography?: string;
  specialtyId: string;
  specialtyName: string;
  specializations: string[];
  degree?: string;
  yearsOfExperience: number;
  rating: number;
  reviewCount: number;
  consultationFee: number;
  languages: string[];
  education: string[];
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
}

export type AppointmentStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW';

export interface TimeSlot {
  startTime: string;
  endTime: string;
  available: boolean;
}

export interface Appointment {
  id: string;
  bookingCode: string;
  patientId: string;
  patientName: string;
  patientAvatarUrl?: string | null;
  doctorId: string;
  doctorName: string;
  doctorAvatarUrl?: string | null;
  specialtyName?: string;
  consultationFee?: number;
  startDatetime: string; // Backend field
  endDatetime: string; // Backend field
  date: string; // Computed from startDatetime
  startTime: string; // Computed from startDatetime
  endTime: string; // Computed from endDatetime
  status: AppointmentStatus;
  reason?: string;
  note?: string | null; // Backend field
  notes?: string | null; // Frontend alias for note
  type?: 'IN_PERSON' | 'VIDEO';
  createdAt?: string;
  updatedAt?: string;
}

export interface Schedule {
  id: string;
  doctorId: string;
  doctorName: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string;
  endTime: string;
  isAvailable: boolean;
}

export interface MedicalRecord {
  id: string;
  patientId: string;
  patientName: string;
  doctorId: string;
  doctorName: string;
  appointmentId?: string;
  bookingCode?: string;
  date: string;
  diagnosis: string;
  symptoms: string;
  prescription: string;
  note?: string;
  attachments?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface Review {
  id: string;
  doctorId: string;
  patientName: string;
  patientAvatarUrl?: string | null;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface DoctorRatingResponse {
  averageRating: number;
  totalReviews: number;
  ratingDistribution: Record<number, number>;
}

export interface AppointmentStatisticResponse {
  // Backend field names from AppointmentStatisticResponse.java
  total: number;
  pending: number;
  confirmed: number;
  completed: number;
  cancelled: number;
  noShow: number;
}

export interface AppointmentStatsResponse {
  total: number;
  completed: number;
  pending: number;
  cancelled: number;
}


export interface RevenueStatisticResponse {
  // Backend field names from RevenueStatisticResponse.java
  totalAppointments: number;
  totalRevenue: number;
}

export interface DoctorPerformanceResponse {
  // Backend field names from DoctorPerformanceResponse.java
  doctorId: number;
  completedAppointments: number;
  revenue: number;
  averageRating: number;
}

export interface AdminDashboardResponse {
  totalPatients: number;
  totalDoctors: number;
  totalAppointments: number;
  totalRevenue: number;
  appointmentTrend: AppointmentTrendResponse[];
  revenueTrend: RevenueTrendResponse[];
  specialtyDistribution: ChartPoint[];
  topDoctors: ChartPoint[];
}

export interface Notification {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: 'BOOKING' | 'SYSTEM' | 'MEDICAL' | 'REMINDER';
  read: boolean;
  createdAt: string;
  link?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
  [key: string]: unknown;
}

export interface DashboardStats {
  // Derived from AppointmentStatisticResponse + RevenueStatisticResponse
  totalPatients: number;
  totalDoctors: number;
  totalAppointments: number;
  totalRevenue: number;
  appointmentsToday: number;
  pendingAppointments: number;
  completedAppointments: number;
  cancelledAppointments: number;
  confirmedAppointments: number;
  noShowAppointments: number;
  revenueChange: number;
  appointmentsChange: number;
  patientsChange: number;
}

export interface ChartPoint {
    label: string;
    value: number;
}

export interface ApiError {
  status?: number;
  error?: boolean;
  message: string;
  code?: string;
  fieldErrors?: Record<string, string>;
  data?: Record<string, string>;
}

export interface AppointmentTrendResponse {
    date: string;
    total: number;
}

export interface RevenueTrendResponse {
  date: string;
  total: number;
}

export interface DashboardStatsResponse {
  totalPatients: number;
  totalDoctors: number;
  totalAppointments: number;
  totalRevenue: number;
}

