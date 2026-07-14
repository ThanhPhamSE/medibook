'use client';

import { useAuth } from '@/contexts/auth-context';

export function useCurrentDoctorId(): string | undefined {
  const { user } = useAuth();
  if (user?.doctorId == null) return undefined;
  return String(user.doctorId);
}
