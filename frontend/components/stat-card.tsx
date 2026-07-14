'use client';

import { cn } from '@/lib/utils';
import type { LucideIcon } from 'lucide-react';
import { ArrowDownRight, ArrowUpRight } from 'lucide-react';
import { CardSkeleton } from '@/components/skeletons';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: LucideIcon;
  change?: number;
  changeLabel?: string;
  accent?: 'primary' | 'accent' | 'success' | 'warning' | 'destructive' | 'info';
  loading?: boolean;
}

const accentMap: Record<NonNullable<StatCardProps['accent']>, string> = {
  primary: 'bg-primary/10 text-primary',
  accent: 'bg-accent/10 text-accent',
  success: 'bg-success/10 text-success',
  warning: 'bg-warning/10 text-warning',
  destructive: 'bg-destructive/10 text-destructive',
  info: 'bg-info/10 text-info',
};

export function StatCard({
  title,
  value,
  icon: Icon,
  change,
  changeLabel,
  accent = 'primary',
  loading,
}: StatCardProps) {
  if (loading) return <CardSkeleton />;

  const positive = (change ?? 0) >= 0;

  return (
    <div className="group relative overflow-hidden rounded-xl border bg-card p-6 shadow-card transition-all hover:shadow-soft">
      <div className="flex items-start justify-between">
        <div className={cn('flex h-12 w-12 items-center justify-center rounded-xl', accentMap[accent])}>
          <Icon className="h-6 w-6" />
        </div>
        {typeof change === 'number' && (
          <span
            className={cn(
              'inline-flex items-center gap-1 rounded-full px-2 py-1 text-xs font-medium',
              positive ? 'bg-success/10 text-success' : 'bg-destructive/10 text-destructive'
            )}
          >
            {positive ? <ArrowUpRight className="h-3 w-3" /> : <ArrowDownRight className="h-3 w-3" />}
            {Math.abs(change)}%
          </span>
        )}
      </div>
      <div className="mt-4">
        <p className="text-sm font-medium text-muted-foreground">{title}</p>
        <p className="mt-1 text-3xl font-semibold tracking-tight">{value}</p>
        {changeLabel && <p className="mt-1 text-xs text-muted-foreground">{changeLabel}</p>}
      </div>
      <div className="pointer-events-none absolute -right-6 -top-6 h-24 w-24 rounded-full bg-gradient-to-br from-primary/5 to-accent/5 opacity-0 transition-opacity group-hover:opacity-100" />
    </div>
  );
}
