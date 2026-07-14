'use client';

import { cn } from '@/lib/utils';
import type { ReactNode } from 'react';
import { ChevronRight, Slash } from 'lucide-react';
import Link from 'next/link';

export interface PageHeaderProps {
  title: string;
  description?: string;
  actions?: ReactNode;
  breadcrumbs?: { label: string; href?: string }[];
  className?: string;
}

export function PageHeader({ title, description, actions, breadcrumbs, className }: PageHeaderProps) {
  return (
    <div className={cn('flex flex-col gap-4 pb-2', className)}>
      {breadcrumbs && breadcrumbs.length > 0 && (
        <nav aria-label="Breadcrumb" className="flex items-center gap-1.5 text-sm text-muted-foreground">
          {breadcrumbs.map((bc, i) => (
            <span key={i} className="flex items-center gap-1.5">
              {bc.href ? (
                <Link href={bc.href} className="transition-colors hover:text-foreground">
                  {bc.label}
                </Link>
              ) : (
                <span className="text-foreground">{bc.label}</span>
              )}
              {i < breadcrumbs.length - 1 && <Slash className="h-3 w-3 opacity-50" />}
            </span>
          ))}
        </nav>
      )}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold tracking-tight text-balance">{title}</h1>
          {description && <p className="text-sm text-muted-foreground text-balance">{description}</p>}
        </div>
        {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
      </div>
    </div>
  );
}

export function PageContainer({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn('mx-auto w-full max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8', className)}>{children}</div>;
}
