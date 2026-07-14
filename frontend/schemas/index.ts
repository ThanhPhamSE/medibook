import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  remember: z.boolean().optional().default(false),
});
export type LoginInput = z.infer<typeof loginSchema>;

export const registerSchema = z
  .object({
    fullName: z.string().min(1, 'Full name is required').max(100).regex(/^[a-zA-Z\s]+$/, 'Full name must contain only letters and spaces'),
    email: z.string().min(1, 'Email is required').email('Enter a valid email'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .regex(/[A-Z]/, 'Include at least one uppercase letter')
      .regex(/[a-z]/, 'Include at least one lowercase letter')
      .regex(/[0-9]/, 'Include at least one number')
      .regex(/[^A-Za-z0-9]/, 'Include at least one special character'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
    phone: z
      .string()
      .min(10, 'Enter a valid phone number')
      .max(15)
      .regex(/^[0-9]+$/, 'Phone must contain only numbers'),
    gender: z.enum(['MALE', 'FEMALE', 'OTHER'], { required_error: 'Gender is required' }),
    birthDate: z.string().min(1, 'Date of birth is required'),
    acceptTerms: z.boolean().refine((v) => v === true, {
      message: 'You must accept the terms to continue',
    }),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });
export type RegisterInput = z.infer<typeof registerSchema>;

export const forgotPasswordSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email'),
});
export type ForgotPasswordInput = z.infer<typeof forgotPasswordSchema>;

export const resetPasswordSchema = z
  .object({
    token: z.string().min(1, 'Reset token is required'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .regex(/[A-Z]/, 'Include at least one uppercase letter')
      .regex(/[0-9]/, 'Include at least one number'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });
export type ResetPasswordInput = z.infer<typeof resetPasswordSchema>;

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .regex(/[A-Z]/, 'Include at least one uppercase letter')
      .regex(/[0-9]/, 'Include at least one number'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
  })
  .refine((d) => d.newPassword === d.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });
export type ChangePasswordInput = z.infer<typeof changePasswordSchema>;

export const verifyEmailSchema = z.object({
  token: z
    .string()
    .min(6, 'Enter the 6-digit code')
    .max(6, 'Enter the 6-digit code'),
});
export type VerifyEmailInput = z.infer<typeof verifyEmailSchema>;

export const bookingSchema = z.object({
  doctorId: z.string().min(1, 'Choose a doctor'),
  date: z.string().min(1, 'Choose a date'),
  startTime: z.string().min(1, 'Choose a time slot'),
  type: z.enum(['IN_PERSON', 'VIDEO']),
  reason: z.string().min(5, 'Tell us briefly why you need this visit').max(500).optional().or(z.literal('')),
  notes: z.string().max(500).optional().or(z.literal('')),
});
export type BookingInput = z.infer<typeof bookingSchema>;

export const profileSchema = z.object({
  fullName: z.string().min(1, 'Full name is required').max(100),
  email: z.string().email('Enter a valid email').optional().or(z.literal('')),
  phone: z.string().max(20).optional().or(z.literal('')),
  birthDate: z.string().optional().or(z.literal('')),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER']).optional().or(z.literal('')),
  profileImage: z.string().url().optional().or(z.literal('')).or(z.null()),
});
export type ProfileInput = z.infer<typeof profileSchema>;

export const medicalRecordSchema = z.object({
  patientId: z.string().min(1, 'Patient is required'),
  diagnosis: z.string().min(2, 'Diagnosis is required').max(300),
  symptoms: z.string().min(2, 'Symptoms are required').max(500),
  prescription: z.string().max(1000).optional().or(z.literal('')),
  notes: z.string().max(1000).optional().or(z.literal('')),
});
export type MedicalRecordInput = z.infer<typeof medicalRecordSchema>;

export const scheduleSchema = z.object({
  dayOfWeek: z.enum([
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY',
  ]),
  startTime: z.string().min(1, 'Start time is required'),
  endTime: z.string().min(1, 'End time is required'),
  isAvailable: z.boolean().default(true),
});
export type ScheduleInput = z.infer<typeof scheduleSchema>;

export const specialtySchema = z.object({
  name: z.string().min(2, 'Name is required').max(80),
  description: z.string().min(10, 'Description must be at least 10 characters').max(500),
  icon: z.string().optional().or(z.literal('')),
  color: z.string().optional().or(z.literal('')),
});
export type SpecialtyInput = z.infer<typeof specialtySchema>;

export const doctorSchema = z.object({
  userId: z.string().optional(),
  specialtyId: z.string().min(1, 'Chuyên khoa là bắt buộc'),
  degree: z.string().min(1, 'Degree is required').max(255, 'Degree cannot exceed 255 characters'),
  experienceYears: z.coerce.number().min(0, 'Experience years cannot be negative').max(70),
  consultationFee: z.coerce.number().min(0.01, 'Consultation fee must be greater than 0'),
  biography: z.string().max(5000, 'Biography cannot exceed 5000 characters'),
});
export type DoctorInput = z.infer<typeof doctorSchema>;

export const reviewSchema = z.object({
  rating: z.coerce.number().min(1, 'Pick a rating').max(5),
  comment: z.string().min(5, 'Tell us more').max(500),
});
export type ReviewInput = z.infer<typeof reviewSchema>;

export const contactSchema = z.object({
  name: z.string().min(1, 'Name is required').max(80),
  email: z.string().email('Enter a valid email'),
  subject: z.string().min(2, 'Subject is required').max(120),
  message: z.string().min(10, 'Message must be at least 10 characters').max(1000),
});
export type ContactInput = z.infer<typeof contactSchema>;
