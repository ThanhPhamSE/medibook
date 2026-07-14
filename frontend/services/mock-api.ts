import { toPaginated } from '@/services/api';
import { sleep } from '@/utils/format';
import {
  adminUsers,
  appointments,
  auditLogs,
  dashboardStats,
  specialties,
  specialtyDistribution,
  doctors,
  medicalRecords,
  notifications,
  revenueTrend,
  reviews,
  roles,
  permissions,
  schedules,
  seededUsers,
  topDoctors,
  appointmentTrend,
  allPatients,
} from '@/services/mock-data';
import type {
  Appointment,
  AppointmentStatus,
  AuthResponse,
  ChartPoint,
  Specialty,
  Doctor,
  MedicalRecord,
  Notification,
  PaginatedResponse,
  Review,
  Schedule,
  User,
  DashboardStats,
} from '@/types';

export const USE_MOCK =
  process.env.NEXT_PUBLIC_USE_MOCK === 'true';

function paginate<T>(items: T[], params: Record<string, unknown> = {}): PaginatedResponse<T> {
  const page = Number(params.page ?? 0);
  const size = Number(params.size ?? 10);
  const search = String(params.search ?? '').toLowerCase();
  const filtered = search
    ? items.filter((it) => JSON.stringify(it).toLowerCase().includes(search))
    : items;
  return toPaginated(filtered, page, size);
}

export const mockApi = {
  async login(email: string): Promise<AuthResponse> {
    await sleep(700);
    const user = seededUsers.find((u) => u.email === email) ?? seededUsers[2];
    return {
      user,
      tokens: {
        accessToken: 'mock-access-' + Date.now(),
        refreshToken: 'mock-refresh-' + Date.now(),
        expiresIn: 3600,
      },
    };
  },

  async register(input: { fullName: string; email: string; password: string; phone: string; gender: string; birthDate: string }): Promise<AuthResponse> {
    await sleep(900);
    const user: User = {
      id: Date.now(),
      email: input.email,
      fullName: input.fullName,
      phone: input.phone,
      birthDate: input.birthDate,
      gender: input.gender as 'MALE' | 'FEMALE' | 'OTHER',
      profileImage: null,
      isActive: true,
      roleId: 1,
      roleName: 'CUSTOMER',
    };
    return {
      user,
      tokens: { accessToken: 'mock-access-' + Date.now(), refreshToken: 'mock-refresh-' + Date.now(), expiresIn: 3600 },
    };
  },

  async me(): Promise<User> {
    await sleep(200);
    const raw = typeof window !== 'undefined' ? localStorage.getItem('medibook.user') : null;
    return raw ? (JSON.parse(raw) as User) : seededUsers[2];
  },

  async getSpecialties(): Promise<Specialty[]> {
    await sleep(400);
    return specialties;
  },
  async getSpecialtiesPaged(params: Record<string, unknown>): Promise<PaginatedResponse<Specialty>> {
    await sleep(400);
    return paginate(specialties, params);
  },
  async createSpecialty(input: Partial<Specialty>): Promise<Specialty> {
    await sleep(500);
    const dept: Specialty = {
      id: 'd-' + Date.now(),
      name: input.name || 'New Specialty',
      description: input.description || '',
      icon: input.icon,
      color: input.color,
      doctorCount: 0,
      createdAt: new Date().toISOString(),
    };
    specialties.unshift(dept);
    return dept;
  },
  async updateSpecialty(id: string, input: Partial<Specialty>): Promise<Specialty> {
    await sleep(500);
    const idx = specialties.findIndex((d) => d.id === id);
    if (idx === -1) throw new Error('Specialty not found');
    specialties[idx] = { ...specialties[idx], ...input };
    return specialties[idx];
  },
  async deleteSpecialty(id: string): Promise<void> {
    await sleep(400);
    const idx = specialties.findIndex((d) => d.id === id);
    if (idx !== -1) specialties.splice(idx, 1);
  },

  async getDoctors(params: Record<string, unknown> = {}): Promise<PaginatedResponse<Doctor>> {
    await sleep(500);
    let list = [...doctors];
    const search = String(params.search ?? '').toLowerCase();
    const specialtyId = params.specialtyId as string | undefined;
    if (specialtyId) list = list.filter((d) => d.specialtyId === specialtyId);
    if (search) list = list.filter((d) => `${d.fullName} ${d.specialtyName}`.toLowerCase().includes(search));
    return paginate(list, params);
  },
  async getDoctor(id: string): Promise<Doctor> {
    await sleep(300);
    const d = doctors.find((x) => x.id === id);
    if (!d) throw new Error('Doctor not found');
    return d;
  },
  async getDoctorReviews(id: string): Promise<Review[]> {
    await sleep(300);
    return reviews.filter((r) => r.doctorId === id);
  },
  async getTimeSlots(doctorId: string, _date: string): Promise<{ startTime: string; endTime: string; available: boolean }[]> {
    await sleep(400);
    const slots = ['08:00', '09:00', '10:00', '11:00', '14:00', '15:00', '16:00', '17:00'];
    return slots.map((s, i) => ({
      startTime: s,
      endTime: `${Number(s.split(':')[0]) + 1}:00`,
      available: i % 3 !== 0,
    }));
  },
  async createDoctor(input: Partial<Doctor>): Promise<Doctor> {
    await sleep(600);
    const specialty = specialties.find((d) => d.id === input.specialtyId);
    const firstName = input.firstName || 'New';
    const lastName = input.lastName || 'Doctor';
    const doc: Doctor = {
      id: 'doc-' + Date.now(),
      fullName: input.fullName || `${firstName} ${lastName}`,
      firstName,
      lastName,
      email: input.email || 'new.doctor@medibook.health',
      phone: input.phone || '',
      avatarUrl: null,
      bio: input.bio || '',
      specialtyId: input.specialtyId || 'd7',
      specialtyName: specialty?.name || 'General Medicine',
      specializations: input.specializations || [],
      yearsOfExperience: input.yearsOfExperience || 0,
      rating: 0,
      reviewCount: 0,
      consultationFee: input.consultationFee ?? 100,
      languages: input.languages || ['English'],
      education: input.education || [],
      status: input.status || 'ACTIVE',
      createdAt: new Date().toISOString(),
    };
    doctors.unshift(doc);
    return doc;
  },
  async updateDoctor(id: string, input: Partial<Doctor>): Promise<Doctor> {
    await sleep(500);
    const idx = doctors.findIndex((d) => d.id === id);
    if (idx === -1) throw new Error('Doctor not found');
    doctors[idx] = { ...doctors[idx], ...input };
    return doctors[idx];
  },
  async deleteDoctor(id: string): Promise<void> {
    await sleep(400);
    const idx = doctors.findIndex((d) => d.id === id);
    if (idx !== -1) doctors.splice(idx, 1);
  },

  async getAppointments(params: Record<string, unknown> = {}): Promise<PaginatedResponse<Appointment>> {
    await sleep(500);
    let list = [...appointments];
    const userId = params.userId as string | undefined;
    const role = params.role as string | undefined;
    const status = params.status as AppointmentStatus | undefined;
    if (userId && role === 'CUSTOMER') list = list.filter((a) => a.patientId === userId);
    if (userId && role === 'DOCTOR') list = list.filter((a) => a.doctorId === userId);
    if (status) list = list.filter((a) => a.status === status);
    return paginate(list, params);
  },
  async getAppointment(id: string): Promise<Appointment> {
    await sleep(300);
    const a = appointments.find((x) => x.id === id);
    if (!a) throw new Error('Appointment not found');
    return a;
  },
  async createAppointment(input: Partial<Appointment> & { doctorId: string; date: string; startTime: string }): Promise<Appointment> {
    await sleep(700);
    const doc = doctors.find((d) => d.id === input.doctorId)!;
    const patient = seededUsers[2];
    const idx = appointments.length + 1;
    const apt: Appointment = {
      id: 'apt-' + Date.now(),
      bookingCode: `MB-${String(20240000 + idx).padStart(8, '0')}`,
      patientId: String(patient.id),
      patientName: patient.fullName,
      doctorId: doc.id,
      doctorName: doc.fullName,
      specialtyName: doc.specialtyName,
      date: input.date,
      startDatetime: input.date,
      endDatetime: `${input.date.split('T')[0]}T${Number(input.startTime.split(':')[0]) + 1}:00`,
      startTime: input.startTime,
      endTime: `${Number(input.startTime.split(':')[0]) + 1}:00`,
      status: 'PENDING',
      reason: input.reason || 'General consultation',
      note: input.notes || null,
      type: input.type || 'IN_PERSON',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    appointments.unshift(apt);
    return apt;
  },
  async updateAppointmentStatus(id: string, status: AppointmentStatus): Promise<Appointment> {
    await sleep(400);
    const a = appointments.find((x) => x.id === id);
    if (!a) throw new Error('Appointment not found');
    a.status = status;
    a.updatedAt = new Date().toISOString();
    return a;
  },
  async rescheduleAppointment(id: string, date: string, startTime: string): Promise<Appointment> {
    await sleep(500);
    const a = appointments.find((x) => x.id === id);
    if (!a) throw new Error('Appointment not found');
    a.date = date;
    a.startTime = startTime;
    a.endTime = `${Number(startTime.split(':')[0]) + 1}:00`;
    a.updatedAt = new Date().toISOString();
    return a;
  },

  async getMedicalRecords(params: Record<string, unknown> = {}): Promise<PaginatedResponse<MedicalRecord>> {
    await sleep(500);
    let list = [...medicalRecords];
    const patientId = params.patientId as string | undefined;
    if (patientId) list = list.filter((r) => r.patientId === patientId);
    return paginate(list, params);
  },
  async getMedicalRecord(id: string): Promise<MedicalRecord> {
    await sleep(300);
    const r = medicalRecords.find((x) => x.id === id);
    if (!r) throw new Error('Record not found');
    return r;
  },
  async createMedicalRecord(input: Partial<MedicalRecord>): Promise<MedicalRecord> {
    await sleep(600);
    const rec: MedicalRecord = {
      id: 'mr-' + Date.now(),
      patientId: input.patientId || 'pat-1',
      patientName: input.patientName || 'Jordan Reyes',
      doctorId: input.doctorId || 'doc-1',
      doctorName: input.doctorName || 'Dr. Sarah Chen',
      date: new Date().toISOString(),
      diagnosis: input.diagnosis || '',
      symptoms: input.symptoms || '',
      prescription: input.prescription || '',
      notes: input.notes || '',
      attachments: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    medicalRecords.unshift(rec);
    return rec;
  },

  async getSchedules(doctorId?: string): Promise<Schedule[]> {
    await sleep(400);
    return doctorId ? schedules.filter((s) => s.doctorId === doctorId) : schedules;
  },
  async createSchedule(input: Partial<Schedule>): Promise<Schedule> {
    await sleep(400);
    const sch: Schedule = {
      id: 'sch-' + Date.now(),
      doctorId: input.doctorId || 'doc-1',
      doctorName: input.doctorName || 'Dr. Sarah Chen',
      dayOfWeek: input.dayOfWeek || 'MONDAY',
      startTime: input.startTime || '09:00',
      endTime: input.endTime || '17:00',
      isAvailable: input.isAvailable ?? true,
    };
    schedules.push(sch);
    return sch;
  },
  async deleteSchedule(id: string): Promise<void> {
    await sleep(300);
    const idx = schedules.findIndex((s) => s.id === id);
    if (idx !== -1) schedules.splice(idx, 1);
  },

  async getNotifications(): Promise<Notification[]> {
    await sleep(300);
    return notifications;
  },
  async markNotificationRead(id: string): Promise<void> {
    await sleep(200);
    const n = notifications.find((x) => x.id === id);
    if (n) n.read = true;
  },
  async markAllNotificationsRead(): Promise<void> {
    await sleep(300);
    notifications.forEach((n) => (n.read = true));
  },

  async getPatients(params: Record<string, unknown> = {}): Promise<PaginatedResponse<User>> {
    await sleep(500);
    return paginate(allPatients, params);
  },
  async getUsers(params: Record<string, unknown> = {}): Promise<PaginatedResponse<User>> {
    await sleep(500);
    return paginate(adminUsers, params);
  },
  async updateUserStatus(id: string, isActive: boolean): Promise<User> {
    await sleep(400);
    const u = adminUsers.find((x) => String(x.id) === id);
    if (!u) throw new Error('User not found');
    u.isActive = isActive;
    return u;
  },

  async getDashboardStats(role: string): Promise<DashboardStats> {
    await sleep(400);
    if (role === 'DOCTOR') {
      return {
        ...dashboardStats,
        totalPatients: appointments.filter((a) => a.doctorId === 'doc-1').length,
        totalAppointments: appointments.filter((a) => a.doctorId === 'doc-1').length,
        totalRevenue: appointments.filter((a) => a.doctorId === 'doc-1').length * 120,
        appointmentsToday: 4,
      };
    }
    if (role === 'CUSTOMER') {
      return {
        ...dashboardStats,
        totalAppointments: appointments.filter((a) => a.patientId === 'pat-1').length,
        appointmentsToday: 1,
        pendingAppointments: appointments.filter((a) => a.patientId === 'pat-1' && a.status === 'PENDING').length,
        totalRevenue: 0,
      };
    }
    return dashboardStats;
  },
  async getAppointmentTrend(): Promise<ChartPoint[]> {
    await sleep(300);
    return appointmentTrend;
  },
  async getRevenueTrend(): Promise<ChartPoint[]> {
    await sleep(300);
    return revenueTrend;
  },
  async getSpecialtyDistribution(): Promise<ChartPoint[]> {
    await sleep(300);
    return specialtyDistribution;
  },
  async getTopDoctors(): Promise<ChartPoint[]> {
    await sleep(300);
    return topDoctors;
  },

  async getAuditLogs(params: Record<string, unknown> = {}): Promise<PaginatedResponse<(typeof auditLogs)[number]>> {
    await sleep(400);
    return paginate(auditLogs, params);
  },
  async getRoles() {
    await sleep(300);
    return roles;
  },
  async getPermissions() {
    await sleep(300);
    return permissions;
  },

  async updateProfile(input: Partial<User>): Promise<User> {
    await sleep(500);
    const raw = typeof window !== 'undefined' ? localStorage.getItem('medibook.user') : null;
    const current = raw ? (JSON.parse(raw) as User) : seededUsers[2];
    const updated = { ...current, ...input };
    if (typeof window !== 'undefined') localStorage.setItem('medibook.user', JSON.stringify(updated));
    return updated;
  },
  async changePassword(): Promise<void> {
    await sleep(600);
  },
  async forgotPassword(): Promise<void> {
    await sleep(600);
  },
  async resetPassword(): Promise<void> {
    await sleep(600);
  },
  async verifyEmail(): Promise<void> {
    await sleep(600);
  },
  async contactMessage(): Promise<void> {
    await sleep(600);
  },
};

export type { Review };
