'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Users, Stethoscope, Calendar, DollarSign, ArrowRight } from 'lucide-react';
import { PageContainer, PageHeader } from '@/components/page-header';
import { StatCard } from '@/components/stat-card';
import { AppointmentTrendChart, RevenueChart, SpecialtyPieChart, TopDoctorsChart } from '@/components/charts';
import {
  useAdminDashboard,
  useAppointments,
} from '@/hooks/use-api';
import { AppointmentStatusBadge } from '@/components/appointment-status-badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { formatCurrency, formatNumber, formatDate, formatTime, timeAgo, exportToCsv } from '@/utils/format';
import type { Appointment, ChartPoint } from '@/types';

// ─── helpers ────────────────────────────────────────────────────────────────

const MONTHS = [
  { value: 1,  label: 'Tháng 1' },
  { value: 2,  label: 'Tháng 2' },
  { value: 3,  label: 'Tháng 3' },
  { value: 4,  label: 'Tháng 4' },
  { value: 5,  label: 'Tháng 5' },
  { value: 6,  label: 'Tháng 6' },
  { value: 7,  label: 'Tháng 7' },
  { value: 8,  label: 'Tháng 8' },
  { value: 9,  label: 'Tháng 9' },
  { value: 10, label: 'Tháng 10' },
  { value: 11, label: 'Tháng 11' },
  { value: 12, label: 'Tháng 12' },
];

const CURRENT_YEAR = new Date().getFullYear();
const YEARS = Array.from({ length: 5 }, (_, i) => CURRENT_YEAR - i);

function isoFirst(year: number, month: number) {
  return `${year}-${String(month).padStart(2, '0')}-01`;
}
function isoLast(year: number, month: number) {
  const d = new Date(year, month, 0); // day-0 of next month = last day of this month
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

// ─── MonthYearSelect ─────────────────────────────────────────────────────────

function MonthSelect({ value, onChange, id }: { value: number; onChange: (v: number) => void; id: string }) {
  return (
    <Select value={String(value)} onValueChange={(v) => onChange(Number(v))}>
      <SelectTrigger className="w-32" id={id}>
        <SelectValue />
      </SelectTrigger>
      <SelectContent>
        {MONTHS.map((m) => (
          <SelectItem key={m.value} value={String(m.value)}>{m.label}</SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}

function YearSelect({ value, onChange, id }: { value: number; onChange: (v: number) => void; id: string }) {
  return (
    <Select value={String(value)} onValueChange={(v) => onChange(Number(v))}>
      <SelectTrigger className="w-24" id={id}>
        <SelectValue />
      </SelectTrigger>
      <SelectContent>
        {YEARS.map((y) => (
          <SelectItem key={y} value={String(y)}>{y}</SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}

// ─── AdminDashboard ───────────────────────────────────────────────────────────

export function AdminDashboard() {
  const now = new Date();

  // "Từ" — đầu tháng hiện tại
  const [fromMonth, setFromMonth] = useState(now.getMonth() + 1);
  const [fromYear,  setFromYear]  = useState(now.getFullYear());

  // "Đến" — cuối tháng hiện tại
  const [toMonth, setToMonth] = useState(now.getMonth() + 1);
  const [toYear,  setToYear]  = useState(now.getFullYear());

  const from = isoFirst(fromYear, fromMonth);
  const to   = isoLast(toYear, toMonth);

  const { data: dashboardData, isLoading } = useAdminDashboard(from, to);
  const { data: appts }    = useAppointments({ size: 6 });

  const chartData: ChartPoint[] = (dashboardData?.appointmentTrend ?? []).map(item => ({
    label: item.date,
    value: Number(item.total ?? 0),
  }));

  const revenueChartData: ChartPoint[] = (dashboardData?.revenueTrend ?? []).map(item => ({
    label: item.date,
    value: Number(item.total ?? 0),
  }));

  const recent: Appointment[] = appts?.content ?? [];

  return (
    <PageContainer>
      <PageHeader
        title="Admin Dashboard"
        description="System-wide performance and operations at a glance."
        actions={
          <div className="flex flex-wrap items-center gap-2">
            {/* ── Từ ─────────────────────────────── */}
            <span className="text-sm text-muted-foreground">Từ</span>
            <MonthSelect id="from-month" value={fromMonth} onChange={setFromMonth} />
            <YearSelect  id="from-year"  value={fromYear}  onChange={setFromYear}  />

            {/* ── Đến ────────────────────────────── */}
            <span className="text-sm text-muted-foreground">đến</span>
            <MonthSelect id="to-month" value={toMonth} onChange={setToMonth} />
            <YearSelect  id="to-year"  value={toYear}  onChange={setToYear}  />

            <Button
              variant="outline"
              onClick={() =>
                exportToCsv(
                  'recent-bookings.csv',
                  recent.map((a: Appointment) => ({
                    code: a.bookingCode,
                    patient: a.patientName,
                    doctor: a.doctorName,
                    status: a.status,
                    date: a.date,
                  })),
                )
              }
            >
              Export
            </Button>

            <Button asChild>
              <Link href="/admin/analytics">
                View analytics <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
          </div>
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Total Revenue"  value={formatCurrency(dashboardData?.totalRevenue ?? 0)}      icon={DollarSign} accent="success"  loading={isLoading} />
        <StatCard title="Appointments"   value={formatNumber(dashboardData?.totalAppointments ?? 0)}    icon={Calendar}   accent="primary"  loading={isLoading} />
        <StatCard title="Patients"       value={formatNumber(dashboardData?.totalPatients ?? 0)}         icon={Users}      accent="accent"   loading={isLoading} />
        <StatCard title="Doctors"        value={dashboardData?.totalDoctors ?? 0}                        icon={Stethoscope}                                    accent="warning"  loading={isLoading} />
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2"><AppointmentTrendChart data={chartData} loading={isLoading} /></div>
        <RevenueChart data={revenueChartData} loading={isLoading} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <SpecialtyPieChart data={dashboardData?.specialtyDistribution ?? []} loading={isLoading} />
        <TopDoctorsChart   data={dashboardData?.topDoctors ?? []} loading={isLoading} />
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <CardTitle className="text-sm font-semibold">Recent bookings</CardTitle>
          <Link href="/admin/bookings" className="text-xs font-medium text-primary hover:underline">View all</Link>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {recent.map((a: Appointment) => (
              <Link
                key={a.id}
                href={`/admin/bookings/${a.id}`}
                className="flex flex-wrap items-center gap-3 rounded-lg border p-3 transition-colors hover:bg-muted/50"
              >
                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10 text-xs font-semibold text-primary">
                  {a.bookingCode.slice(-3)}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{a.patientName} → {a.doctorName}</p>
                  <p className="truncate text-xs text-muted-foreground">
                    {a.specialtyName} · {formatDate(a.date)} {formatTime(a.date)}
                  </p>
                </div>
                <AppointmentStatusBadge status={a.status} />
                <span className="hidden text-xs text-muted-foreground sm:inline">{timeAgo(a.startDatetime)}</span>
              </Link>
            ))}
          </div>
        </CardContent>
      </Card>
    </PageContainer>
  );
}
