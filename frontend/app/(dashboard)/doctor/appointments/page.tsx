'use client';

import * as React from 'react';
import Link from 'next/link';
import { useTodayAppointments, useWeekAppointments, useAppointments } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent } from '@/components/ui/card';
import { AppointmentStatusBadge } from '@/components/appointment-status-badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { EmptyState } from '@/components/empty-state';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { CalendarDays, Clock, Video, MapPin, Hash, Wallet, FileText, ArrowRight } from 'lucide-react';
import { formatTime, initials } from '@/utils/format';
import type { Appointment, AppointmentStatus } from '@/types';

type RangeFilter = 'today' | 'week' | 'month';

function formatCurrency(v?: number) {
  if (v === undefined || v === null) return '—';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
}

function formatFullDate(dateStr: string) {
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' });
}

function groupByDate(items: Appointment[]) {
  const groups = new Map<string, Appointment[]>();
  for (const a of items) {
    const key = a.date;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key)!.push(a);
  }
  return Array.from(groups.entries())
    .sort(([a], [b]) => new Date(a).getTime() - new Date(b).getTime())
    .map(([date, appts]) => ({
      date,
      appts: appts.sort((x, y) => new Date(x.startTime).getTime() - new Date(y.startTime).getTime()),
    }));
}

function isInCurrentMonth(dateStr: string) {
  const d = new Date(dateStr);
  const now = new Date();
  return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth();
}

function AppointmentDetailCard({ a }: { a: Appointment }) {
  return (
    <Card className="transition-all hover:shadow-soft">
      <CardContent className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center">
        <div className="flex flex-col items-center rounded-lg bg-primary/10 px-3 py-2 text-primary shrink-0">
          <span className="text-sm font-semibold">{formatTime(a.startTime)}</span>
          <span className="text-[10px] text-primary/70">{formatTime(a.endTime)}</span>
        </div>

        <Avatar className="h-10 w-10 border bg-accent/10 shrink-0">
          <AvatarFallback className="bg-transparent text-xs font-semibold text-accent">
            {initials(a.patientName.split(' ')[0], a.patientName.split(' ')[1] || '')}
          </AvatarFallback>
        </Avatar>

        <div className="min-w-0 flex-1 space-y-1">
          <div className="flex flex-wrap items-center gap-2">
            <p className="truncate text-sm font-medium">{a.patientName}</p>
            <AppointmentStatusBadge status={a.status} />
          </div>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              {a.type === 'VIDEO' ? <Video className="h-3 w-3" /> : <MapPin className="h-3 w-3" />}
              {a.type === 'VIDEO' ? 'Video call' : 'Khám trực tiếp'}
            </span>
            {a.specialtyName && <span>{a.specialtyName}</span>}
            <span className="flex items-center gap-1">
              <Hash className="h-3 w-3" />
              {a.bookingCode}
            </span>
            <span className="flex items-center gap-1">
              <Wallet className="h-3 w-3" />
              {formatCurrency(a.consultationFee)}
            </span>
          </div>
          {(a.notes || a.note) && (
            <p className="flex items-start gap-1 text-xs text-muted-foreground">
              <FileText className="mt-0.5 h-3 w-3 shrink-0" />
              <span className="truncate">{a.notes ?? a.note}</span>
            </p>
          )}
        </div>

        <Button asChild size="sm" variant="outline" className="shrink-0 self-end sm:self-center">
          <Link href={`/appointments/${a.id}`}>
            Xem chi tiết
            <ArrowRight className="ml-1.5 h-3.5 w-3.5" />
          </Link>
        </Button>
      </CardContent>
    </Card>
  );
}

function ScheduleCalendar() {
  const [range, setRange] = React.useState<RangeFilter>('today');
  const [status, setStatus] = React.useState<'all' | AppointmentStatus>('all');

  const { data: todayData, isLoading: todayLoading } = useTodayAppointments({ size: 100 });
  const { data: weekData, isLoading: weekLoading } = useWeekAppointments({ size: 100 });
  const { data: monthData, isLoading: monthLoading } = useAppointments({ size: 100 });

  const raw: Appointment[] =
    range === 'today'
      ? todayData?.content ?? []
      : range === 'week'
      ? weekData?.content ?? []
      : (monthData?.content ?? []).filter((a) => isInCurrentMonth(a.date));

  const isLoading = range === 'today' ? todayLoading : range === 'week' ? weekLoading : monthLoading;

  const filtered = raw.filter((a) => status === 'all' || a.status === status);
  const grouped = groupByDate(filtered);

  const rangeOptions: { value: RangeFilter; label: string }[] = [
    { value: 'today', label: 'Today' },
    { value: 'week', label: 'Week' },
    { value: 'month', label: 'Month' },
  ];

  return (
    <PageContainer>
      <PageHeader
        title="Lịch khám"
        description="Xem lịch hẹn của bạn theo ngày, tuần hoặc tháng."
        breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Lịch khám' }]}
      />

      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-1 rounded-lg border bg-muted/40 p-1">
          {rangeOptions.map((opt) => (
            <Button
              key={opt.value}
              size="sm"
              variant={range === opt.value ? 'default' : 'ghost'}
              className="h-8 px-3"
              onClick={() => setRange(opt.value)}
            >
              {opt.label}
            </Button>
          ))}
        </div>

        <Select value={status} onValueChange={(v) => setStatus(v as 'all' | AppointmentStatus)}>
          <SelectTrigger className="w-44">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả trạng thái</SelectItem>
            <SelectItem value="PENDING">Pending</SelectItem>
            <SelectItem value="CONFIRMED">Confirmed</SelectItem>
            <SelectItem value="COMPLETED">Completed</SelectItem>
            <SelectItem value="CANCELLED">Cancelled</SelectItem>
            <SelectItem value="NO_SHOW">No Show</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-20 animate-pulse rounded-xl bg-muted" />
          ))}
        </div>
      ) : grouped.length === 0 ? (
        <Card>
          <CardContent className="p-0">
            <EmptyState
              icon={<CalendarDays className="h-7 w-7" />}
              title="Không có lịch hẹn"
              description="Không tìm thấy lịch hẹn nào phù hợp với bộ lọc hiện tại."
            />
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-6">
          {grouped.map(({ date, appts }) => (
            <div key={date} className="space-y-3">
              <h3 className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
                <Clock className="h-4 w-4" />
                {formatFullDate(date)}
                <span className="text-xs font-normal">({appts.length} lịch hẹn)</span>
              </h3>
              <div className="space-y-3">
                {appts.map((a) => (
                  <AppointmentDetailCard key={a.id} a={a} />
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </PageContainer>
  );
}

export default function Page() {
  return (
    <RoleGuard roles={['DOCTOR']}>
      <ScheduleCalendar />
    </RoleGuard>
  );
}
