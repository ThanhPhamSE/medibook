'use client';

import Link from 'next/link';
import { Calendar, Users, Clock, CheckCircle2, XCircle, ArrowRight, Video, MapPin } from 'lucide-react';
import { PageContainer, PageHeader } from '@/components/page-header';
import { StatCard } from '@/components/stat-card';
import { useDashboardStats, useTodayAppointments } from '@/hooks/use-api';
import { useAuth } from '@/contexts/auth-context';
import { AppointmentStatusBadge } from '@/components/appointment-status-badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { formatTime, initials } from '@/utils/format';
import { EmptyState } from '@/components/empty-state';
import type { Appointment } from '@/types';

function toISODate(d: Date) {
  return d.toISOString().slice(0, 10);
}

export function DoctorDashboard() {
  const { user } = useAuth();

  const { data: todayAppts, isLoading: apptsLoading } = useTodayAppointments({ size: 100 });

  const todays: Appointment[] = (todayAppts?.content ?? [])
    .slice()
    .sort((a: Appointment, b: Appointment) => new Date(a.date).getTime() - new Date(b.date).getTime());

  const todaysCount = todays.length;
  const todaysPatientCount = new Set(todays.map((a) => a.patientId)).size;
  const waitingCount = todays.filter((a) => a.status === 'CONFIRMED' || a.status === 'PENDING').length;
  const completedCount = todays.filter((a) => a.status === 'COMPLETED').length;
  const noShowCount = todays.filter((a) => a.status === 'NO_SHOW').length;

  return (
    <PageContainer>
      <PageHeader
        title={`Welcome, Dr. ${user?.fullName}`}
        description="Here's your practice overview for today."
        actions={<Button asChild variant="outline"><Link href="/doctor/working-schedule">Manage schedule</Link></Button>}
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <StatCard title="Lịch hôm nay" value={todaysCount} icon={Calendar} accent="primary" loading={apptsLoading} />
        <StatCard title="Bệnh nhân hôm nay" value={todaysPatientCount} icon={Users} accent="accent" loading={apptsLoading} />
        <StatCard title="Chờ khám" value={waitingCount} icon={Clock} accent="warning" loading={apptsLoading} />
        <StatCard title="Đã khám" value={completedCount} icon={CheckCircle2} accent="success" loading={apptsLoading} />
        <StatCard title="No Show" value={noShowCount} icon={XCircle} accent="destructive" loading={apptsLoading} />
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <CardTitle className="text-sm font-semibold">Lịch hôm nay</CardTitle>
          <Link href="/doctor/schedule/today" className="text-xs font-medium text-primary hover:underline">
            Xem tất cả
          </Link>
        </CardHeader>
        <CardContent className="space-y-3">
          {todays.length === 0 ? (
            <EmptyState
              icon={<Calendar className="h-7 w-7" />}
              title="Không có lịch hẹn hôm nay"
              description="Hãy tận hưởng ngày rảnh hoặc kiểm tra lịch sắp tới."
            />
          ) : (
            todays.map((a: Appointment) => (
              <Link
                key={a.id}
                href={`/doctor/appointments/${a.id}`}
                className="flex items-center gap-3 rounded-lg border p-3 transition-colors hover:bg-muted/50"
              >
                <Avatar className="h-10 w-10 border bg-accent/10">
                  <AvatarFallback className="bg-transparent text-xs font-semibold text-accent">
                    {initials(a.patientName.split(' ')[0], a.patientName.split(' ')[1] || '')}
                  </AvatarFallback>
                </Avatar>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{a.patientName}</p>
                  <p className="flex items-center gap-1 truncate text-xs text-muted-foreground">
                    {a.type === 'VIDEO' ? <Video className="h-3 w-3" /> : <MapPin className="h-3 w-3" />}
                    {formatTime(a.date)} · {a.specialtyName}
                  </p>
                </div>
                <AppointmentStatusBadge status={a.status} />
              </Link>
            ))
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-semibold">Quick actions</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3 sm:grid-cols-2">
          <Link
            href="/doctor/appointments"
            className="flex items-center justify-between rounded-lg border p-4 transition-all hover:border-primary hover:shadow-card"
          >
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <Calendar className="h-5 w-5" />
              </div>
              <span className="text-sm font-medium">My appointments</span>
            </div>
            <ArrowRight className="h-4 w-4 text-muted-foreground" />
          </Link>
          <Link
            href="/doctor/patients"
            className="flex items-center justify-between rounded-lg border p-4 transition-all hover:border-primary hover:shadow-card"
          >
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10 text-accent">
                <Users className="h-5 w-5" />
              </div>
              <span className="text-sm font-medium">My patients</span>
            </div>
            <ArrowRight className="h-4 w-4 text-muted-foreground" />
          </Link>
        </CardContent>
      </Card>
    </PageContainer>
  );
}