import {
  LayoutDashboard,
  Calendar,
  Users,
  Stethoscope,
  Building2,
  ClipboardList,
  BarChart3,
  Settings,
  FileText,
  Bell,
  User,
  Clock,
  HeartPulse,
  CalendarDays,
  ShieldCheck,
  ScrollText,
  DollarSign,
  History,
  type LucideIcon,
} from 'lucide-react';
import type { UserRole } from '@/types';

export interface NavItem {
  title: string;
  href: string;
  icon: LucideIcon;
  badge?: string;
}

export interface NavSection {
  title?: string;
  items: NavItem[];
}

export const CUSTOMER_NAV: NavSection[] = [
  {
    items: [
      { title: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
      { title: 'Book Appointment', href: '/doctors', icon: Stethoscope },
      { title: 'My Appointments', href: '/appointments', icon: CalendarDays },
    ],
  },
  {
    title: 'Personal',
    items: [
      { title: 'Medical History', href: '/medical-records', icon: History },
      { title: 'Profile', href: '/profile', icon: User },
    ],
  },
];

export const DOCTOR_NAV: NavSection[] = [
  {
    items: [
      { title: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
      { title: "Today's Schedule", href: '/doctor/schedule/today', icon: Clock },
      { title: 'My Appointments', href: '/doctor/appointments', icon: Calendar },
      { title: 'Patients', href: '/doctor/patients', icon: Users },
    ],
  },
  {
    title: 'Practice',
    items: [
      { title: 'Medical Records', href: '/doctor/medical-records', icon: FileText },
      { title: 'Working Schedule', href: '/doctor/working-schedule', icon: CalendarDays },
      { title: 'Profile', href: '/profile', icon: User },
    ],
  },
];

export const ADMIN_NAV: NavSection[] = [
  {
    items: [
      { title: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
      { title: 'Analytics', href: '/admin/analytics', icon: BarChart3 },
    ],
  },
  {
    title: 'Management',
    items: [
      { title: 'Doctors', href: '/admin/doctors', icon: Stethoscope },
      { title: 'Patients', href: '/admin/patients', icon: Users },
      { title: 'Specialties', href: '/admin/speiclaties', icon: Building2 },
      { title: 'Bookings', href: '/admin/bookings', icon: ClipboardList },
      { title: 'Medical Records', href: '/admin/medical-records', icon: FileText },
    ],
  },
  {
    title: 'Insights & System',
    items: [
      { title: 'Reports', href: '/admin/reports', icon: BarChart3 },
      { title: 'Revenue', href: '/admin/revenue', icon: DollarSign },
      { title: 'Audit Logs', href: '/admin/audit-logs', icon: ScrollText },
      { title: 'Roles & Permissions', href: '/admin/roles', icon: ShieldCheck },
    ],
  },
];

export function getNavForRole(role: string): NavSection[] {
  switch (role) {
    case 'DOCTOR':
      return DOCTOR_NAV;
    case 'ADMIN':
      return ADMIN_NAV;
    default:
      return CUSTOMER_NAV;
  }
}

export { HeartPulse };
