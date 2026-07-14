'use client';

import Link from 'next/link';
import {
  Calendar,
  CalendarPlus,
  ArrowRight,
  FileText,
  CalendarCheck,
  CheckCircle2,
  Clock,
  XCircle,
} from 'lucide-react';
import { PageContainer, PageHeader } from '@/components/page-header';
import { useAppointments, useAppointmentStats } from '@/hooks/use-api';
import { useAuth } from '@/contexts/auth-context';
import { AppointmentStatusBadge } from '@/components/appointment-status-badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { formatDate, formatTime, initials } from '@/utils/format';
import { EmptyState } from '@/components/empty-state';
import { Skeleton } from '@/components/skeletons';
import type { Appointment } from '@/types';

export function PatientDashboard() {
  const { user } = useAuth();

  // Lịch khám sắp tới (đã có sẵn, lấy tối đa 20 để hiển thị)
  const { data: appts, isLoading: loadingUpcoming } = useAppointments({
    timeFilter: 'upcoming',
    sort: 'startDatetime,asc',
    size: 20,
  });
  const upcoming: Appointment[] = appts?.content ?? [];

  // Thống kê theo status — gọi 1 API duy nhất để lấy toàn bộ các số liệu count
  const { data: statsData } = useAppointmentStats();

  const stats = [
    {
      label: 'Tổng số lịch',
      value: statsData?.total ?? 0,
      icon: Calendar,
      color: 'text-primary bg-primary/10',
    },
    {
      label: 'Đã khám',
      value: statsData?.completed ?? 0,
      icon: CheckCircle2,
      color: 'text-emerald-600 bg-emerald-100 dark:bg-emerald-950/40',
    },
    {
      label: 'Đang chờ xác nhận',
      value: statsData?.pending ?? 0,
      icon: Clock,
      color: 'text-amber-600 bg-amber-100 dark:bg-amber-950/40',
    },
    {
      label: 'Đã hủy',
      value: statsData?.cancelled ?? 0,
      icon: XCircle,
      color: 'text-red-600 bg-red-100 dark:bg-red-950/40',
    },
  ];


  return (
    <PageContainer>
      <PageHeader
        title={`Chào mừng trở lại, ${user?.fullName}`}
        description="Đây là tổng quan hành trình chăm sóc sức khỏe của bạn."
        actions={
          <Button asChild>
            <Link href="/doctors">
              <CalendarPlus className="mr-2 h-4 w-4" /> Đặt lịch khám
            </Link>
          </Button>
        }
      />

      {/* Thống kê */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((s) => (
          <Card key={s.label}>
            <CardContent className="flex items-center gap-4 p-5">
              <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${s.color}`}>
                <s.icon className="h-5 w-5" />
              </div>
              <div className="min-w-0">
                <p className="text-2xl font-bold leading-tight">{s.value}</p>
                <p className="truncate text-xs text-muted-foreground">{s.label}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between space-y-0">
            <CardTitle className="text-sm font-semibold">Lịch khám sắp tới</CardTitle>
            <Link href="/appointments" className="text-xs font-medium text-primary hover:underline">
              Xem tất cả
            </Link>
          </CardHeader>
          <CardContent className="space-y-3">
            {loadingUpcoming ? (
              <div className="space-y-3">
                <Skeleton className="h-16 w-full rounded-lg" />
                <Skeleton className="h-16 w-full rounded-lg" />
                <Skeleton className="h-16 w-full rounded-lg" />
              </div>
            ) : upcoming.length === 0 ? (
              <EmptyState
                icon={<Calendar className="h-7 w-7" />}
                title="Không có lịch khám sắp tới"
                description="Đặt lịch khám tiếp theo chỉ trong vài giây."
                action={
                  <Button asChild size="sm">
                    <Link href="/doctors">Đặt lịch ngay</Link>
                  </Button>
                }
              />
            ) : (
              upcoming.map((a: Appointment) => (
                <Link
                  key={a.id}
                  href={`/appointments/${a.id}`}
                  className="flex items-center gap-3 rounded-lg border p-3 transition-colors hover:bg-muted/50"
                >
                  <Avatar className="h-10 w-10 border bg-primary/10">
                    <AvatarFallback className="bg-transparent text-xs font-semibold text-primary">
                      {initials(
                        a.doctorName.replace('Dr. ', '').split(' ')[0],
                        a.doctorName.replace('Dr. ', '').split(' ')[1] || ''
                      )}
                    </AvatarFallback>
                  </Avatar>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">{a.doctorName}</p>
                    <p className="truncate text-xs text-muted-foreground">
                      {formatDate(a.startTime || a.startDatetime)} · {formatTime(a.startTime || a.startDatetime)}
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
            <CardTitle className="text-sm font-semibold">Thao tác nhanh</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-3">
            <Link
              href="/doctors"
              className="flex items-center justify-between rounded-lg border p-4 transition-all hover:border-primary hover:shadow-card"
            >
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                  <CalendarPlus className="h-5 w-5" />
                </div>
                <span className="text-sm font-medium">Đặt lịch khám</span>
              </div>
              <ArrowRight className="h-4 w-4 text-muted-foreground" />
            </Link>

            <Link
              href="/appointments"
              className="flex items-center justify-between rounded-lg border p-4 transition-all hover:border-primary hover:shadow-card"
            >
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-500/10 text-blue-600">
                  <CalendarCheck className="h-5 w-5" />
                </div>
                <span className="text-sm font-medium">Xem lịch khám</span>
              </div>
              <ArrowRight className="h-4 w-4 text-muted-foreground" />
            </Link>

            <Link
              href="/profile"
              className="flex items-center justify-between rounded-lg border p-4 transition-all hover:border-primary hover:shadow-card"
            >
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10 text-accent">
                  <FileText className="h-5 w-5" />
                </div>
                <span className="text-sm font-medium">Hồ sơ</span>
              </div>
              <ArrowRight className="h-4 w-4 text-muted-foreground" />
            </Link>
          </CardContent>
        </Card>
      </div>
    </PageContainer>
  );
}