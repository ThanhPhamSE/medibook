'use client';

import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { ChartPoint } from '@/types';
import { ChartSkeleton } from '@/components/skeletons';
import { useTheme } from 'next-themes';
import { formatCurrency, formatNumber } from '@/utils/format';

const COLORS = ['hsl(var(--chart-1))', 'hsl(var(--chart-2))', 'hsl(var(--chart-3))', 'hsl(var(--chart-4))', 'hsl(var(--chart-5))'];

function useAxis() {
  const { resolvedTheme } = useTheme();
  const stroke = resolvedTheme === 'dark' ? 'hsl(215 20% 65%)' : 'hsl(215 16% 47%)';
  const grid = resolvedTheme === 'dark' ? 'hsl(217 33% 20%)' : 'hsl(214 32% 91%)';
  return { stroke, grid };
}

interface ChartCardProps {
  title: string;
  description?: string;
  loading?: boolean;
  action?: React.ReactNode;
  children: React.ReactNode;
}

export function ChartCard({ title, description, loading, action, children }: ChartCardProps) {
  if (loading) return <ChartSkeleton />;
  return (
    <div className="rounded-xl border bg-card p-6 shadow-card">
      <div className="mb-4 flex items-start justify-between">
        <div>
          <h3 className="text-sm font-semibold">{title}</h3>
          {description && <p className="mt-1 text-xs text-muted-foreground">{description}</p>}
        </div>
        {action}
      </div>
      {children}
    </div>
  );
}

export function AppointmentTrendChart({ data, loading }: { data: ChartPoint[]; loading?: boolean }) {
  const { stroke, grid } = useAxis();
  const chartData = Array.isArray(data) ? data : [];
  return (
    <ChartCard title="Appointment Trends" description="Booked vs completed over the last 12 months" loading={loading}>
      <ResponsiveContainer width="100%" height={280}>
        <AreaChart data={chartData} margin={{ left: -16, right: 8, top: 8 }}>
          <defs>
            <linearGradient id="cValue" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="hsl(var(--chart-1))" stopOpacity={0.4} />
              <stop offset="95%" stopColor="hsl(var(--chart-1))" stopOpacity={0} />
            </linearGradient>
            <linearGradient id="cSecondary" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="hsl(var(--chart-2))" stopOpacity={0.3} />
              <stop offset="95%" stopColor="hsl(var(--chart-2))" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke={grid} vertical={false} />
          <XAxis dataKey="label" stroke={stroke} fontSize={12} tickLine={false} axisLine={false} />
          <YAxis stroke={stroke} fontSize={12} tickLine={false} axisLine={false} />
          <Tooltip
            contentStyle={{ borderRadius: 12, border: '1px solid hsl(var(--border))', background: 'hsl(var(--popover))', color: 'hsl(var(--popover-foreground))' }}
          />
          <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12 }} />
          <Area type="monotone" dataKey="value" name="Booked" stroke="hsl(var(--chart-1))" strokeWidth={2} fill="url(#cValue)" />
          <Area type="monotone" dataKey="secondary" name="Completed" stroke="hsl(var(--chart-2))" strokeWidth={2} fill="url(#cSecondary)" />
        </AreaChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

export function RevenueChart({ data, loading }: { data: ChartPoint[]; loading?: boolean }) {
  const { stroke, grid } = useAxis();
  const chartData = Array.isArray(data) ? data : [];
  return (
    <ChartCard title="Revenue Overview" description="Monthly revenue across all specialties" loading={loading}>
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={chartData} margin={{ left: -8, right: 8, top: 8 }}>
          <CartesianGrid strokeDasharray="3 3" stroke={grid} vertical={false} />
          <XAxis dataKey="label" stroke={stroke} fontSize={12} tickLine={false} axisLine={false} />
          <YAxis stroke={stroke} fontSize={12} tickLine={false} axisLine={false} tickFormatter={(v) => `$${v / 1000}k`} />
          <Tooltip
            formatter={(v: number) => formatCurrency(v)}
            contentStyle={{ borderRadius: 12, border: '1px solid hsl(var(--border))', background: 'hsl(var(--popover))', color: 'hsl(var(--popover-foreground))' }}
          />
          <Bar dataKey="value" name="Revenue" radius={[6, 6, 0, 0]} fill="hsl(var(--chart-1))" />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

export function SpecialtyPieChart({ data, loading }: { data: ChartPoint[]; loading?: boolean }) {
  const chartData = Array.isArray(data) ? data : [];
  return (
    <ChartCard title="Appointments by Specialty" description="Distribution across specialties" loading={loading}>
      {chartData.length === 0 ? (
        <div className="flex h-[280px] items-center justify-center text-sm text-muted-foreground">
          No data available
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={280}>
          <PieChart>
            <Pie 
              data={chartData} 
              dataKey="value" 
              nameKey="label" 
              cx="50%" 
              cy="50%" 
              innerRadius={60} 
              outerRadius={100} 
              paddingAngle={3}
              label
            >
              {chartData.map((entry, i) => (
                <Cell key={`cell-${i}`} fill={COLORS[i % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip 
              content={({ payload }) => {
                if (payload && payload.length > 0) {
                  const data = payload[0].payload;
                  return (
                    <div className="rounded-lg border bg-popover p-2 text-sm">
                      <div className="font-medium">{data.label}</div>
                      <div className="text-muted-foreground">{data.value} appointments</div>
                    </div>
                  );
                }
                return null;
              }}
            />
            <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 11 }} />
          </PieChart>
        </ResponsiveContainer>
      )}
    </ChartCard>
  );
}

export function TopDoctorsChart({ data, loading }: { data: ChartPoint[]; loading?: boolean }) {
  const { stroke, grid } = useAxis();
  const chartData = Array.isArray(data) ? data : [];
  return (
    <ChartCard title="Top Doctors" description="By number of reviews" loading={loading}>
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={chartData} layout="vertical" margin={{ left: 8, right: 16, top: 8 }}>
          <CartesianGrid strokeDasharray="3 3" stroke={grid} horizontal={false} />
          <XAxis type="number" stroke={stroke} fontSize={12} tickLine={false} axisLine={false} />
          <YAxis type="category" dataKey="label" stroke={stroke} fontSize={11} width={120} tickLine={false} axisLine={false} />
          <Tooltip
            contentStyle={{ borderRadius: 12, border: '1px solid hsl(var(--border))', background: 'hsl(var(--popover))', color: 'hsl(var(--popover-foreground))' }}
          />
          <Bar dataKey="value" name="Reviews" radius={[0, 6, 6, 0]} fill="hsl(var(--chart-3))" />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}

export function MiniLineChart({ data, loading }: { data: ChartPoint[]; loading?: boolean }) {
  const { stroke, grid } = useAxis();
  const chartData = Array.isArray(data) ? data : [];
  if (loading) return <ChartSkeleton />;
  return (
    <ResponsiveContainer width="100%" height={80}>
      <LineChart data={chartData}>
        <Line type="monotone" dataKey="value" stroke="hsl(var(--chart-1))" strokeWidth={2} dot={false} />
        <XAxis dataKey="label" hide />
        <YAxis hide />
        <Tooltip
          contentStyle={{ borderRadius: 12, border: '1px solid hsl(var(--border))', background: 'hsl(var(--popover))', color: 'hsl(var(--popover-foreground))' }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
