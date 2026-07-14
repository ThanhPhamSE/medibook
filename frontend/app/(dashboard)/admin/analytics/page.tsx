'use client';

import { useState } from 'react';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { StatCard } from '@/components/stat-card';
import { AppointmentTrendChart, RevenueChart, SpecialtyPieChart, TopDoctorsChart } from '@/components/charts';
import {
  useAdminDashboard,
} from '@/hooks/use-api';
import { Users, Stethoscope, Calendar, DollarSign } from 'lucide-react';
import { formatCurrency, formatNumber } from '@/utils/format';

function toISODate(d: Date) {
  return d.toISOString().split('T')[0];
}

function defaultRange() {
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - 29);
  return { from: toISODate(from), to: toISODate(to) };
}

function Analytics() {
  const [{ from, to }, setRange] = useState(defaultRange());

  const { data: dashboardData, isLoading } = useAdminDashboard(from, to);

  const chartData = (dashboardData?.appointmentTrend ?? []).map(item => ({
    label: item.date,
    value: Number(item.total ?? 0),
  }));

  const revenueChartData = (dashboardData?.revenueTrend ?? []).map(item => ({
    label: item.date,
    value: Number(item.total ?? 0),
  }));

  const handleFromChange = (value: string) => {
    // Không cho chọn from > to
    setRange((prev) => ({ from: value, to: value > prev.to ? value : prev.to }));
  };

  const handleToChange = (value: string) => {
    setRange((prev) => ({ from: value < prev.from ? value : prev.from, to: value }));
  };

  return (
    <PageContainer>
      <PageHeader
        title="Analytics"
        description="Deep dive into system performance and trends."
        breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Analytics' }]}
      />

      {/* Date range picker cho admin */}
      <div className="flex flex-wrap items-end gap-3 rounded-lg border bg-card p-4">
        <div className="flex flex-col gap-1">
          <label htmlFor="from-date" className="text-sm font-medium text-muted-foreground">
            Từ ngày
          </label>
          <input
            id="from-date"
            type="date"
            value={from}
            max={to}
            onChange={(e) => handleFromChange(e.target.value)}
            className="rounded-md border px-3 py-2 text-sm"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label htmlFor="to-date" className="text-sm font-medium text-muted-foreground">
            Đến ngày
          </label>
          <input
            id="to-date"
            type="date"
            value={to}
            min={from}
            max={toISODate(new Date())}
            onChange={(e) => handleToChange(e.target.value)}
            className="rounded-md border px-3 py-2 text-sm"
          />
        </div>
        <button
          type="button"
          onClick={() => setRange(defaultRange())}
          className="ml-auto rounded-md border px-3 py-2 text-sm hover:bg-muted" 
        >
          30 ngày gần nhất
        </button>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Revenue" value={formatCurrency(dashboardData?.totalRevenue ?? 0)} icon={DollarSign} accent="success" loading={isLoading} />
        <StatCard title="Appointments" value={formatNumber(dashboardData?.totalAppointments ?? 0)} icon={Calendar} accent="primary" loading={isLoading} />
        <StatCard title="Patients" value={formatNumber(dashboardData?.totalPatients ?? 0)} icon={Users} accent="accent" loading={isLoading} />
        <StatCard title="Doctors" value={dashboardData?.totalDoctors ?? 0} icon={Stethoscope} accent="warning" loading={isLoading} />
      </div>
      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2"><AppointmentTrendChart data={chartData} loading={isLoading} /></div>
        <RevenueChart data={revenueChartData} loading={isLoading} />
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <SpecialtyPieChart data={dashboardData?.specialtyDistribution ?? []} loading={isLoading} />
        <TopDoctorsChart data={dashboardData?.topDoctors ?? []} loading={isLoading} />
      </div>
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><Analytics /></RoleGuard>;
}