import type {
  Appointment,
  Specialty,
  Doctor,
  MedicalRecord,
  Notification,
  Review,
  Schedule,
  User,
  DashboardStats,
  ChartPoint,
} from '@/types';

const today = new Date();
const iso = (d: Date) => d.toISOString();
const dayOffset = (n: number) => {
  const d = new Date(today);
  d.setDate(d.getDate() + n);
  return iso(d);
};
const atTime = (n: number, h: string) => {
  const d = new Date(today);
  d.setDate(d.getDate() + n);
  d.setHours(Number(h.split(':')[0]), Number(h.split(':')[1]), 0, 0);
  return iso(d);
};

export const specialties: Specialty[] = [
  { id: 'd1', name: 'Cardiology', description: 'Heart and vascular care with state-of-the-art diagnostics.', icon: 'Heart', color: '#ef4444', doctorCount: 6, createdAt: dayOffset(-400) },
  { id: 'd2', name: 'Neurology', description: 'Comprehensive treatment for disorders of the brain and nervous system.', icon: 'Brain', color: '#8b5cf6', doctorCount: 5, createdAt: dayOffset(-380) },
  { id: 'd3', name: 'Orthopedics', description: 'Bone, joint, and musculoskeletal care from specialists.', icon: 'Bone', color: '#f59e0b', doctorCount: 7, createdAt: dayOffset(-360) },
  { id: 'd4', name: 'Pediatrics', description: 'Compassionate healthcare for infants, children, and teens.', icon: 'Baby', color: '#10b981', doctorCount: 8, createdAt: dayOffset(-350) },
  { id: 'd5', name: 'Dermatology', description: 'Skin, hair, and nail care for every age.', icon: 'Microscope', color: '#06b6d4', doctorCount: 4, createdAt: dayOffset(-300) },
  { id: 'd6', name: 'Ophthalmology', description: 'Complete eye care and vision correction services.', icon: 'Eye', color: '#3b82f6', doctorCount: 3, createdAt: dayOffset(-280) },
  { id: 'd7', name: 'General Medicine', description: 'Primary care and preventive health for the whole family.', icon: 'Stethoscope', color: '#14b8a6', doctorCount: 10, createdAt: dayOffset(-260) },
  { id: 'd8', name: 'Psychiatry', description: 'Mental health support and therapy in a safe space.', icon: 'Brain', color: '#ec4899', doctorCount: 5, createdAt: dayOffset(-240) },
];

const docs: Doctor[] = [
  ['d1', 'Sarah', 'Chen', 'Cardiologist with 15 years treating arrhythmias and heart failure. Harvard Medical School alumna.'],
  ['d1', 'Michael', 'Torres', 'Interventional cardiologist specializing in catheter-based procedures.'],
  ['d2', 'Emily', 'Patel', 'Neurologist focused on epilepsy, migraines, and movement disorders.'],
  ['d2', 'James', 'Okonkwo', 'Neurologist and stroke specialist with research in neuroprotection.'],
  ['d3', 'Robert', 'Lindgren', 'Orthopedic surgeon — joint replacement and sports medicine.'],
  ['d4', 'Maria', 'Santos', 'Pediatrician passionate about early childhood development and nutrition.'],
  ['d5', 'Aisha', 'Khan', 'Dermatologist treating acne, eczema, and cosmetic skin concerns.'],
  ['d6', 'David', 'Kim', 'Ophthalmologist — cataract surgery and refractive correction.'],
  ['d7', 'Olivia', 'Brown', 'Family medicine physician focused on preventive care and chronic disease.'],
  ['d7', 'Daniel', 'Foster', 'Internal medicine specialist with a focus on diabetes management.'],
].map((row, i) => {
  const [specialtyId, firstName, lastName, bio] = row as [string, string, string, string];
  const specialty = specialties.find((d) => d.id === specialtyId)!;
  return {
    id: `doc-${i + 1}`,
    fullName: `${firstName} ${lastName}`,
    firstName,
    lastName,
    email: `${firstName.toLowerCase()}.${lastName.toLowerCase()}@medibook.health`,
    phone: `+1 (415) 555-01${10 + i}`,
    avatarUrl: null,
    bio,
    specialtyId,
    specialtyName: specialty.name,
    specializations: [specialty.name, i % 3 === 0 ? 'Preventive Care' : 'Advanced Diagnostics'],
    yearsOfExperience: 5 + (i % 12),
    rating: 4.2 + (i % 8) / 10,
    reviewCount: 40 + i * 17,
    consultationFee: 80 + (i % 5) * 30,
    languages: ['English', i % 2 === 0 ? 'Spanish' : 'Mandarin'],
    education: ['MD, Johns Hopkins University', 'Residency, UCSF Medical Center'],
    status: 'ACTIVE' as const,
    createdAt: dayOffset(-(200 - i * 10)),
  };
});

export const doctors: Doctor[] = docs;

const patients: User[] = Array.from({ length: 24 }).map((_, i) => {
  const firstName = ['Alex', 'Jordan', 'Taylor', 'Casey', 'Riley', 'Morgan', 'Sam', 'Jamie'][i % 8];
  const lastName = ['Reyes', 'Nguyen', 'Walsh', 'Patel', 'Garcia', 'Brooks', 'Lee', 'Diaz'][i % 8];
  return {
    id: i + 1,
    email: `patient${i + 1}@example.com`,
    fullName: `${firstName} ${lastName}`,
    phone: `+1 (415) 555-02${20 + i}`,
    birthDate: `199${i % 9}-0${(i % 9) + 1}-1${i % 9}`,
    gender: (i % 2 === 0 ? 'MALE' : 'FEMALE') as 'MALE' | 'FEMALE',
    profileImage: null,
    isActive: true,
    roleId: 1,
    roleName: 'CUSTOMER' as const,
  };
});

// Helper to get name parts from fullName
const getNameParts = (fullName: string) => {
  const parts = fullName.split(' ');
  return { firstName: parts[0] || '', lastName: parts.slice(1).join(' ') || '' };
};

export const allPatients = patients;

const statuses = ['CONFIRMED', 'PENDING', 'COMPLETED', 'CANCELLED', 'NO_SHOW'] as const;
export const appointments: Appointment[] = Array.from({ length: 48 }).map((_, i) => {
  const doc = doctors[i % doctors.length];
  const pat = patients[i % patients.length];
  const offset = (i % 21) - 10;
  const slot = ['09:00', '10:00', '11:00', '14:00', '15:30', '16:00'][i % 6];
  const dateStr = atTime(offset, slot);
  const docNameParts = getNameParts(doc.fullName);
  return {
    id: `apt-${i + 1}`,
    bookingCode: `MB-${String(20240000 + i).padStart(8, '0')}`,
    patientId: String(pat.id),
    patientName: pat.fullName,
    doctorId: doc.id,
    doctorName: `Dr. ${docNameParts.firstName} ${docNameParts.lastName}`,
    specialtyName: doc.specialtyName,
    date: dateStr,
    startDatetime: dateStr,
    endDatetime: `${dateStr.split('T')[0]}T${Number(slot.split(':')[0]) + 1}:00`,
    startTime: slot,
    endTime: `${Number(slot.split(':')[0]) + 1}:00`,
    status: i < 6 ? 'CONFIRMED' : statuses[i % statuses.length],
    reason: ['Annual checkup', 'Follow-up consultation', 'New symptoms', 'Test results review', 'Prescription renewal'][i % 5],
    note: i % 3 === 0 ? 'Patient reported mild discomfort.' : null,
    type: i % 4 === 0 ? 'VIDEO' : 'IN_PERSON',
    createdAt: dayOffset(-(20 - (i % 20))),
    updatedAt: dayOffset(-(10 - (i % 10))),
  };
});

export const schedules: Schedule[] = doctors.slice(0, 6).flatMap((doc, di) =>
  ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'].map((day, i) => ({
    id: `sch-${di}-${i}`,
    doctorId: doc.id,
    doctorName: `Dr. ${doc.firstName} ${doc.lastName}`,
    dayOfWeek: day as Schedule['dayOfWeek'],
    startTime: i % 2 === 0 ? '08:00' : '10:00',
    endTime: i % 2 === 0 ? '12:00' : '17:00',
    isAvailable: !(di === 2 && i === 3),
  }))
);

export const medicalRecords: MedicalRecord[] = appointments
  .filter((a) => a.status === 'COMPLETED')
  .map((a, i) => ({
    id: `mr-${i + 1}`,
    patientId: a.patientId,
    patientName: a.patientName,
    doctorId: a.doctorId,
    doctorName: a.doctorName,
    appointmentId: a.id,
    date: a.date,
    diagnosis: ['Hypertension', 'Migraine', 'Type 2 Diabetes', 'Asthma', 'Anxiety'][i % 5],
    symptoms: ['Headache and fatigue', 'Chest pain on exertion', 'Persistent cough', 'Joint stiffness', 'Sleep disturbance'][i % 5],
    prescription: 'Take 1 tablet daily after meals. Follow up in 2 weeks.',
    notes: 'Patient advised lifestyle modifications.',
    attachments: [],
    createdAt: a.date,
    updatedAt: a.date,
  }));

export const reviews: Review[] = Array.from({ length: 18 }).map((_, i) => {
  const pat = patients[i % patients.length];
  const patNameParts = getNameParts(pat.fullName);
  return {
    id: `rev-${i + 1}`,
    doctorId: doctors[i % doctors.length].id,
    patientName: pat.fullName,
    patientAvatarUrl: null,
    rating: Math.round(3.5 + (i % 3) * 0.5),
    comment: [
      'Excellent bedside manner and clear explanations.',
      'Very thorough and took the time to answer all my questions.',
      'The doctor was kind and the staff were professional.',
      'Quick appointment and accurate diagnosis. Highly recommend.',
      'Great experience overall. Felt heard and cared for.',
    ][i % 5],
    date: dayOffset(-(i * 4)),
  };
});

export const notifications: Notification[] = Array.from({ length: 8 }).map((_, i) => ({
  id: `ntf-${i + 1}`,
  userId: 'pat-1',
  title: [
    'Appointment confirmed',
    'Upcoming reminder',
    'Lab results available',
    'Prescription ready',
    'Schedule change',
    'Welcome to MediBook',
    'New doctor joined',
    'Annual checkup due',
  ][i],
  message: [
    'Your appointment with Dr. Chen has been confirmed for tomorrow at 10:00 AM.',
    'You have an appointment in 2 hours. Please arrive 15 minutes early.',
    'Your recent lab results are now available in your medical records.',
    'Your prescription is ready for pickup at the pharmacy.',
    'Dr. Patel has updated their working schedule for next week.',
    'Welcome aboard! Complete your profile to get the most out of MediBook.',
    'Dr. James Okonkwo has joined the Neurology specialty.',
    'It has been over a year since your last checkup. Book one today.',
  ][i],
  type: (['BOOKING', 'REMINDER', 'MEDICAL', 'MEDICAL', 'BOOKING', 'SYSTEM', 'SYSTEM', 'REMINDER'] as const)[i],
  read: i > 3,
  createdAt: dayOffset(-(i)),
  link: i % 2 === 0 ? '/appointments' : '/medical-records',
}));

export const dashboardStats: DashboardStats = {
  totalPatients: 1284,
  totalDoctors: 56,
  totalAppointments: 9821,
  totalRevenue: 842300,
  appointmentsToday: 24,
  pendingAppointments: 18,
  completedAppointments: 7240,
  cancelledAppointments: 612,
  revenueChange: 12.4,
  appointmentsChange: 8.2,
  patientsChange: 5.1,
};

export const appointmentTrend: ChartPoint[] = [
  { label: 'Jan', value: 620, secondary: 540 },
  { label: 'Feb', value: 710, secondary: 600 },
  { label: 'Mar', value: 680, secondary: 650 },
  { label: 'Apr', value: 820, secondary: 700 },
  { label: 'May', value: 900, secondary: 760 },
  { label: 'Jun', value: 870, secondary: 800 },
  { label: 'Jul', value: 1020, secondary: 880 },
  { label: 'Aug', value: 980, secondary: 910 },
  { label: 'Sep', value: 1100, secondary: 950 },
  { label: 'Oct', value: 1050, secondary: 990 },
  { label: 'Nov', value: 1180, secondary: 1020 },
  { label: 'Dec', value: 980, secondary: 900 },
];

export const revenueTrend: ChartPoint[] = [
  { label: 'Jan', value: 58000 },
  { label: 'Feb', value: 64000 },
  { label: 'Mar', value: 61000 },
  { label: 'Apr', value: 72000 },
  { label: 'May', value: 79000 },
  { label: 'Jun', value: 76000 },
  { label: 'Jul', value: 88000 },
  { label: 'Aug', value: 84000 },
  { label: 'Sep', value: 92000 },
  { label: 'Oct', value: 88000 },
  { label: 'Nov', value: 96000 },
  { label: 'Dec', value: 84000 },
];

export const specialtyDistribution: ChartPoint[] = specialties.map((d) => ({
  label: d.name,
  value: appointments.filter((a) => a.specialtyName === d.name).length,
}));

export const topDoctors: ChartPoint[] = doctors
  .slice(0, 6)
  .map((d) => ({ label: `Dr. ${d.fullName}`, value: d.reviewCount }));

export const adminUsers: User[] = [
  {
    id: 999,
    email: 'admin@medibook.health',
    fullName: 'Admin Manager',
    phone: '+1 (415) 555-0100',
    birthDate: '1980-01-01',
    gender: 'OTHER' as const,
    profileImage: null,
    isActive: true,
    roleId: 3,
    roleName: 'ADMIN',
  },
  ...doctors.map((d) => ({
    id: parseInt(d.id.replace('doc-', '')),
    email: d.email,
    fullName: d.fullName,
    phone: d.phone,
    birthDate: '1980-01-01',
    gender: 'OTHER' as const,
    profileImage: d.avatarUrl,
    isActive: true,
    roleId: 2,
    roleName: 'DOCTOR' as const,
  })),
  ...patients,
];

export const auditLogs = [
  { id: 'log-1', actor: 'admin@medibook.health', action: 'LOGIN', target: 'auth', ip: '203.0.113.4', timestamp: dayOffset(0) },
  { id: 'log-2', actor: 'admin@medibook.health', action: 'CREATE_DOCTOR', target: 'doc-11', ip: '203.0.113.4', timestamp: dayOffset(-1) },
  { id: 'log-3', actor: 'sarah.chen@medibook.health', action: 'UPDATE_SCHEDULE', target: 'sch-3', ip: '198.51.100.7', timestamp: dayOffset(-1) },
  { id: 'log-4', actor: 'admin@medibook.health', action: 'DELETE_SPECIALTY', target: 'd-old', ip: '203.0.113.4', timestamp: dayOffset(-2) },
  { id: 'log-5', actor: 'patient1@example.com', action: 'BOOK_APPOINTMENT', target: 'apt-49', ip: '192.0.2.99', timestamp: dayOffset(-2) },
  { id: 'log-6', actor: 'admin@medibook.health', action: 'UPDATE_ROLE', target: 'usr-12', ip: '203.0.113.4', timestamp: dayOffset(-3) },
  { id: 'log-7', actor: 'system', action: 'BACKUP', target: 'database', ip: '127.0.0.1', timestamp: dayOffset(-3) },
];

export const roles = [
  { id: 'role-1', name: 'Admin', description: 'Full system access', usersCount: 1, permissions: 42 },
  { id: 'role-2', name: 'Doctor', description: 'Manage appointments and patients', usersCount: 56, permissions: 18 },
  { id: 'role-3', name: 'Patient', description: 'Book appointments and view records', usersCount: 1284, permissions: 8 },
  { id: 'role-4', name: 'Receptionist', description: 'Manage bookings and schedules', usersCount: 12, permissions: 14 },
];

export const permissions = [
  { id: 'p-1', module: 'Appointments', action: 'view', roles: ['Admin', 'Doctor', 'Patient', 'Receptionist'] },
  { id: 'p-2', module: 'Appointments', action: 'create', roles: ['Admin', 'Doctor', 'Patient', 'Receptionist'] },
  { id: 'p-3', module: 'Appointments', action: 'cancel', roles: ['Admin', 'Doctor', 'Patient'] },
  { id: 'p-4', module: 'Medical Records', action: 'view', roles: ['Admin', 'Doctor'] },
  { id: 'p-5', module: 'Medical Records', action: 'create', roles: ['Admin', 'Doctor'] },
  { id: 'p-6', module: 'Users', action: 'manage', roles: ['Admin'] },
  { id: 'p-7', module: 'Reports', action: 'view', roles: ['Admin'] },
  { id: 'p-8', module: 'Settings', action: 'manage', roles: ['Admin'] },
  { id: 'p-9', module: 'Audit Logs', action: 'view', roles: ['Admin'] },
];

export const seededUsers: User[] = [
  {
    id: 999,
    email: 'admin@medibook.health',
    fullName: 'Alex Manager',
    phone: '+1 (415) 555-0100',
    birthDate: '1980-01-01',
    gender: 'OTHER' as const,
    profileImage: null,
    isActive: true,
    roleId: 3,
    roleName: 'ADMIN' as const,
  },
  {
    id: 1,
    email: 'sarah.chen@medibook.health',
    fullName: 'Sarah Chen',
    phone: '+1 (415) 555-0110',
    birthDate: '1980-01-01',
    gender: 'FEMALE' as const,
    profileImage: null,
    isActive: true,
    roleId: 2,
    roleName: 'DOCTOR',
    doctorId: 'doc-1',
  },
  {
    id: 2,
    email: 'patient@example.com',
    fullName: 'Jordan Reyes',
    phone: '+1 (415) 555-0120',
    birthDate: '1990-04-12',
    gender: 'MALE' as const,
    profileImage: null,
    isActive: true,
    roleId: 1,
    roleName: 'CUSTOMER' as const,
  },
];
