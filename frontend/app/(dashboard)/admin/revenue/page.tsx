'use client';

import { useState } from 'react';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { StatCard } from '@/components/stat-card';
import { RevenueChart } from '@/components/charts';
import { useAdminDashboard } from '@/hooks/use-api';
import { DollarSign, TrendingUp, Receipt, PiggyBank } from 'lucide-react';
import { formatCurrency } from '@/utils/format';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import type { ChartPoint } from '@/types';

function toISODate(d: Date) {
  return d.toISOString().split('T')[0];
}

function defaultRange() {
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - 29);
  return { from: toISODate(from), to: toISODate(to) };
}

function Revenue() {
  const [{ from, to }, setRange] = useState(defaultRange());
  const { data: dashboardData, isLoading } = useAdminDashboard(from, to);

  const chartData = (dashboardData?.revenueTrend ?? []).map(item => ({
    label: item.date,
    value: Number(item.total ?? 0),
  }));

  const total = chartData.reduce((sum: number, r: ChartPoint) => sum + r.value, 0);
  const avg = chartData.length ? total / chartData.length : 0;

  const handleFromChange = (value: string) => {
    setRange((prev) => ({ from: value, to: value > prev.to ? value : prev.to }));
  };

  const handleToChange = (value: string) => {
    setRange((prev) => ({ from: value < prev.from ? value : prev.from, to: value }));
  };

  return (
    <PageContainer>
      <PageHeader
        title="Revenue"
        description="Financial performance and billing insights."
        breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Revenue' }]}
      />

      {/* Date range picker */}
      <div className="flex flex-wrap items-end gap-3 rounded-lg border bg-card p-4">
        <div className="flex flex-col gap-1">
          <label htmlFor="revenue-from-date" className="text-sm font-medium text-muted-foreground">
            Từ ngày
          </label>
          <input
            id="revenue-from-date"
            type="date"
            value={from}
            max={to}
            onChange={(e) => handleFromChange(e.target.value)}
            className="rounded-md border px-3 py-2 text-sm"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label htmlFor="revenue-to-date" className="text-sm font-medium text-muted-foreground">
            Đến ngày
          </label>
          <input
            id="revenue-to-date"
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
        <StatCard title="Total Revenue" value={formatCurrency(dashboardData?.totalRevenue ?? 0)} icon={DollarSign} accent="success" loading={isLoading} />
        <StatCard title="Monthly Avg" value={formatCurrency(avg)} icon={TrendingUp} accent="primary" loading={isLoading} />
        <StatCard title="Appointments" value={dashboardData?.totalAppointments ?? 0} icon={Receipt} accent="accent" loading={isLoading} />
        <StatCard title="Avg / Visit" value={formatCurrency(dashboardData?.totalAppointments ? (dashboardData?.totalRevenue ?? 0) / (dashboardData?.totalAppointments || 1) : 0)} icon={PiggyBank} accent="warning" loading={isLoading} />
      </div>
      <RevenueChart data={chartData} loading={isLoading} />
      <Card>
        <CardHeader><CardTitle className="text-base">Monthly breakdown</CardTitle></CardHeader>
        <CardContent>
          <div className="grid gap-3 sm:grid-cols-3 lg:grid-cols-6">
            {chartData.map((r: ChartPoint) => (
              <div key={r.label} className="rounded-lg border p-3 text-center">
                <p className="text-xs text-muted-foreground">{r.label}</p>
                <p className="mt-1 text-sm font-semibold">{formatCurrency(r.value)}</p>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><Revenue /></RoleGuard>;
}
