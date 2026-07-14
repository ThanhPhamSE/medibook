'use client';

import * as React from 'react';
import Link from 'next/link';
import { useNotifications, useMarkNotificationRead, useMarkAllNotificationsRead } from '@/hooks/use-api';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/skeletons';
import { EmptyState } from '@/components/empty-state';
import { Bell, CheckCheck, Calendar, FileText, HeartPulse, Info, ArrowRight } from 'lucide-react';
import { timeAgo } from '@/utils/format';
import { cn } from '@/lib/utils';
import type { Notification } from '@/types';

const iconForType: Record<Notification['type'], any> = {
  BOOKING: Calendar,
  MEDICAL: FileText,
  REMINDER: HeartPulse,
  SYSTEM: Info,
};
const colorForType: Record<Notification['type'], string> = {
  BOOKING: 'bg-primary/10 text-primary',
  MEDICAL: 'bg-accent/10 text-accent',
  REMINDER: 'bg-warning/10 text-warning',
  SYSTEM: 'bg-muted text-muted-foreground',
};

export default function NotificationsPage() {
  const { data, isLoading } = useNotifications();
  const markRead = useMarkNotificationRead();
  const markAll = useMarkAllNotificationsRead();
  const [filter, setFilter] = React.useState<'all' | 'unread'>('all');

  const all: Notification[] = data ?? [];
  const items: Notification[] = all.filter((n: Notification) => filter === 'all' || !n.read);
  const unread: number = all.filter((n: Notification) => !n.read).length;

  return (
    <PageContainer>
      <PageHeader
        title="Notifications"
        description={`${unread} unread notification${unread === 1 ? '' : 's'}`}
        breadcrumbs={[{ label: 'Home', href: '/dashboard' }, { label: 'Notifications' }]}
        actions={unread > 0 && <Button variant="outline" onClick={() => markAll.mutate()}><CheckCheck className="mr-2 h-4 w-4" /> Mark all read</Button>}
      />

      <div className="flex gap-2">
        <Button size="sm" variant={filter === 'all' ? 'default' : 'outline'} onClick={() => setFilter('all')}>All</Button>
        <Button size="sm" variant={filter === 'unread' ? 'default' : 'outline'} onClick={() => setFilter('unread')}>Unread ({unread})</Button>
      </div>

      {isLoading ? (
        <div className="space-y-3">{Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-20 rounded-xl" />)}</div>
      ) : items.length === 0 ? (
        <Card><CardContent className="p-0"><EmptyState icon={<Bell className="h-7 w-7" />} title="No notifications" description="You're all caught up!" /></CardContent></Card>
      ) : (
        <div className="space-y-2">
          {items.map((n: Notification) => {
            const Icon = iconForType[n.type];
            return (
              <button
                key={n.id}
                onClick={() => markRead.mutate(n.id)}
                className={cn('flex w-full items-start gap-4 rounded-xl border p-4 text-left transition-all hover:shadow-card', !n.read && 'border-primary/30 bg-primary/5')}
              >
                <div className={cn('flex h-10 w-10 shrink-0 items-center justify-center rounded-lg', colorForType[n.type])}><Icon className="h-5 w-5" /></div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    {!n.read && <span className="h-2 w-2 rounded-full bg-primary" />}
                    <p className="text-sm font-semibold">{n.title}</p>
                  </div>
                  <p className="mt-0.5 text-sm text-muted-foreground">{n.message}</p>
                  <p className="mt-1 text-xs text-muted-foreground">{timeAgo(n.createdAt)}</p>
                </div>
                {n.link && <ArrowRight className="mt-1 h-4 w-4 shrink-0 text-muted-foreground" />}
              </button>
            );
          })}
        </div>
      )}
    </PageContainer>
  );
}
