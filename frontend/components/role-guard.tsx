'use client';

import * as React from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/auth-context';
import type { UserRole } from '@/types';
import { Loader2 } from 'lucide-react';

export function RoleGuard({ children, roles }: { children: React.ReactNode; roles: UserRole[] }) {
  const { user, loading, hasRole } = useAuth();
  const router = useRouter();

  React.useEffect(() => {
    if (!loading && !user) router.replace('/login');
    else if (!loading && user && !hasRole(...roles)) router.replace('/forbidden');
  }, [user, loading, hasRole, roles, router]);

  if (loading || !user) {
    return <div className="flex min-h-[60vh] items-center justify-center"><Loader2 className="h-8 w-8 animate-spin text-primary" /></div>;
  }
  if (!hasRole(...roles)) {
    return <div className="flex min-h-[60vh] items-center justify-center"><Loader2 className="h-8 w-8 animate-spin text-primary" /></div>;
  }
  return <>{children}</>;
}
