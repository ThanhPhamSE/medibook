'use client';

import { useTodayAppointments } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent } from '@/components/ui/card';
import { AppointmentStatusBadge } from '@/components/appointment-status-badge';
import { Button } from '@/components/ui/button';
import { EmptyState } from '@/components/empty-state';
import { Clock, Eye } from 'lucide-react';
import { formatTime, formatDate, formatCurrency } from '@/utils/format';
import Link from 'next/link';
import type { Appointment } from '@/types';

function TodaysSchedule() {
  const { data, isLoading } = useTodayAppointments({ size: 50 });

  const todays: Appointment[] = (data?.content ?? [])
    .slice()
    .sort((a, b) => new Date(a.startDatetime).getTime() - new Date(b.startDatetime).getTime());

  return (
    <PageContainer>
      <PageHeader
        title="Lịch khám hôm nay"
        description="Các cuộc hẹn khám bệnh của bạn trong ngày."
        breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Lịch khám hôm nay' }]}
      />
      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-20 animate-pulse rounded-xl bg-muted" />
          ))}
        </div>
      ) : todays.length === 0 ? (
        <Card>
          <CardContent className="p-0">
            <EmptyState icon={<Clock className="h-7 w-7" />} title="Không có lịch khám hôm nay" description="Hãy tận hưởng ngày nghỉ hoặc kiểm tra các lịch hẹn sắp tới." />
          </CardContent>
        </Card>
      ) : (
        <div className="relative space-y-3 border-l-2 pl-6">
          {todays.map((a: Appointment) => (
            <div key={a.id} className="relative">
              <span className="absolute -left-[29px] flex h-5 w-5 items-center justify-center rounded-full border-2 border-primary bg-background">
                <span className="h-2 w-2 rounded-full bg-primary" />
              </span>
              <Card className="transition-all hover:shadow-soft">
                <CardContent className="flex items-center gap-4 p-4">
                  <div className="flex flex-col items-center rounded-lg bg-primary/10 px-3 py-2 text-primary">
                    <span className="text-sm font-semibold">{formatTime(a.startDatetime)}</span>
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-medium">{formatDate(a.startDatetime, 'dd/MM/yyyy')}</p>
                    <p className="text-xs text-muted-foreground">{formatTime(a.startDatetime)} - {formatTime(a.endDatetime)}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-semibold">{formatCurrency(a.consultationFee || 0)}</p>
                    <p className="text-xs text-muted-foreground">Phí khám</p>
                  </div>
                  <AppointmentStatusBadge status={a.status} />
                  <Button asChild size="sm" variant="ghost">
                    <Link href={`/appointments/${a.id}`}>
                      <Eye className="h-4 w-4 mr-2" /> Xem
                    </Link>
                  </Button>
                </CardContent>
              </Card>
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
      <TodaysSchedule />
    </RoleGuard>
  );
}