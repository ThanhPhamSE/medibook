'use client';

import { useAuth } from '@/contexts/auth-context';
import { PatientDashboard } from '@/components/dashboards/patient-dashboard';
import { DoctorDashboard } from '@/components/dashboards/doctor-dashboard';
import { AdminDashboard } from '@/components/dashboards/admin-dashboard';
import { PageContainer } from '@/components/page-header';
import { Skeleton } from '@/components/skeletons';

export default function DashboardPage() {
  const { user } = useAuth();

  if (!user) {
    return (
      <PageContainer>
        <Skeleton className="h-96 w-full rounded-xl" />
      </PageContainer>
    );
  }

  if (user.roleName === 'ADMIN') return <AdminDashboard />;
  if (user.roleName === 'DOCTOR') return <DoctorDashboard />;
  return <PatientDashboard />;
}
