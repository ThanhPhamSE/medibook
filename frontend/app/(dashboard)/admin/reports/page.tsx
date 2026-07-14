'use client';

import { useState } from 'react';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { StatCard } from '@/components/stat-card';
import { AppointmentTrendChart, RevenueChart, SpecialtyPieChart, TopDoctorsChart } from '@/components/charts';
import { useAdminDashboard } from '@/hooks/use-api';
import { Calendar, DollarSign, Users, Stethoscope, FileBarChart } from 'lucide-react';
import { formatCurrency, formatNumber } from '@/utils/format';
import { Button } from '@/components/ui/button';
import { exportToCsv } from '@/utils/format';

function toISODate(d: Date) {
  return d.toISOString().split('T')[0];
}

function defaultRange() {
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - 29);
  return { from: toISODate(from), to: toISODate(to) };
}

function Reports() {
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
    setRange((prev) => ({ from: value, to: value > prev.to ? value : prev.to }));
  };

  const handleToChange = (value: string) => {
    setRange((prev) => ({ from: value < prev.from ? value : prev.from, to: value }));
  };

  return (
    <PageContainer>
      <PageHeader title="Reports" description="Generate and export system reports." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Reports' }]}
        actions={
          <>
            <Button variant="outline" onClick={() => exportToCsv('appointments-report.csv', chartData as any)}>Export appointments</Button>
            <Button variant="outline" onClick={() => exportToCsv('revenue-report.csv', revenueChartData as any)}>Export revenue</Button>
          </>
        } />

      {/* Date range picker cho admin */}
      <div className="flex flex-wrap items-end gap-3 rounded-lg border bg-card p-4">
        <div className="flex flex-col gap-1">
          <label htmlFor="reports-from-date" className="text-sm font-medium text-muted-foreground">
            Từ ngày
          </label>
          <input
            id="reports-from-date"
            type="date"
            value={from}
            max={to}
            onChange={(e) => handleFromChange(e.target.value)}
            className="rounded-md border px-3 py-2 text-sm"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label htmlFor="reports-to-date" className="text-sm font-medium text-muted-foreground">
            Đến ngày
          </label>
          <input
            id="reports-to-date"
            type="date"
            value={to}
            min={from}
            max={toISODate(new Date())}
            onChange={(e) => handleToChange(e.target.value)}
            className="rounded-md border px-3 py-2 text-sm"
          />
        </div>
        <Button variant="outline" className="ml-auto" onClick={() => setRange(defaultRange())}>
          30 ngày gần nhất
        </Button>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Total Appointments" value={formatNumber(dashboardData?.totalAppointments ?? 0)} icon={Calendar} accent="primary" loading={isLoading} />
        <StatCard title="Completed" value={formatNumber(dashboardData?.totalAppointments ?? 0)} icon={FileBarChart} accent="success" loading={isLoading} />
        <StatCard title="Cancelled" value={formatNumber(0)} icon={Calendar} accent="destructive" loading={isLoading} />
        <StatCard title="Total Doctors" value={dashboardData?.totalDoctors ?? 0} icon={Stethoscope} accent="warning" loading={isLoading} />
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <AppointmentTrendChart data={chartData} loading={isLoading} />
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
  return <RoleGuard roles={['ADMIN']}><Reports /></RoleGuard>;
}
