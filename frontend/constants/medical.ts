import {
  Activity,
  Brain,
  Bone,
  Eye,
  Heart,
  Baby,
  Stethoscope,
  Microscope,
  Pill,
  Syringe,
  type LucideIcon,
} from 'lucide-react';

export const SPECIALTY_ICONS: Record<string, LucideIcon> = {
  Cardiology: Heart,
  Neurology: Brain,
  Orthopedics: Bone,
  Ophthalmology: Eye,
  Pediatrics: Baby,
  'General Medicine': Stethoscope,
  Dermatology: Microscope,
  Psychiatry: Brain,
  Oncology: Pill,
  'Emergency Medicine': Activity,
};

export const DEFAULT_SPECIALTY_ICON = Syringe;

export const GENDERS = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
] as const;

export const APPOINTMENT_STATUSES = [
  'PENDING',
  'CONFIRMED',
  'COMPLETED',
  'CANCELLED',
  'NO_SHOW',
] as const;

export const APPOINTMENT_TYPES = [
  { value: 'IN_PERSON', label: 'In-person visit' },
  { value: 'VIDEO', label: 'Video consultation' },
] as const;

export const DAYS_OF_WEEK = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
] as const;

export const TIME_SLOTS = [
  '08:00', '08:30', '09:00', '09:30', '10:00', '10:30',
  '11:00', '11:30', '14:00', '14:30', '15:00', '15:30',
  '16:00', '16:30', '17:00',
] as const;
