'use client';

import * as React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/contexts/auth-context';
import { useCurrentDoctorId } from '@/hooks/use-current-doctor-id';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Clock, CalendarOff } from 'lucide-react';
import { apiGet } from '@/services/api';
import { formatDate } from '@/utils/format';

const dayLabel: Record<string, string> = {
  MON: 'Thứ 2', TUE: 'Thứ 3', WED: 'Thứ 4', THU: 'Thứ 5',
  FRI: 'Thứ 6', SAT: 'Thứ 7', SUN: 'Chủ nhật',
};
const DAYS_OF_WEEK = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'] as const;

// ---- Matches backend com.medibook.modules.schedule.dto.response.* ----
interface WorkingPatternResponse {
  id: number;
  doctorId: number;
  dayOfWeek: string;
  startTime: string; // "09:00:00"
  endTime: string;   // "17:00:00"
  slotDuration: number;
  bufferDuration: number;
}

interface TimeOffResponse {
  id: number;
  doctorId: number;
  startDateTime: string;
  endDateTime: string;
  reason: string | null;
}

interface DoctorScheduleResponse {
  doctorId: number;
  workingPartterns: WorkingPatternResponse[];
  timeOffs: TimeOffResponse[];
}

function useDoctorSchedule(doctorId: number | undefined) {
  return useQuery({
    queryKey: ['doctor-schedule', doctorId],
    queryFn: () => apiGet<DoctorScheduleResponse>(`/schedules/doctor/${doctorId}`),
    enabled: !!doctorId,
  });
}

// Backend returns "HH:mm:ss" — trim seconds for display
function formatTime(t: string) {
  return t?.slice(0, 5) ?? t;
}

function WorkingSchedule() {
  const { user } = useAuth();
  const doctorId = useCurrentDoctorId();
  const { data, isLoading } = useDoctorSchedule(doctorId ? Number(doctorId) : undefined);

  const patterns: WorkingPatternResponse[] = data?.workingPartterns ?? [];
  const timeOffs: TimeOffResponse[] = data?.timeOffs ?? [];

  const grouped: Record<string, WorkingPatternResponse[]> = React.useMemo(() => {
    const acc: Record<string, WorkingPatternResponse[]> = {};
    patterns.forEach((p) => { (acc[p.dayOfWeek] = acc[p.dayOfWeek] || []).push(p); });
    return acc;
  }, [patterns]);

  return (
    <PageContainer>
      <PageHeader
        title="Lịch làm việc"
        description="Xem khung giờ làm việc hàng tuần và thời gian nghỉ của bạn."
        breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Lịch làm việc' }]}
      />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader><CardTitle className="text-base">Khung giờ làm việc theo tuần</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {isLoading ? (
              <div className="space-y-3">
                {Array.from({ length: 5 }).map((_, i) => (
                  <div key={i} className="h-16 animate-pulse rounded-lg bg-muted" />
                ))}
              </div>
            ) : (
              DAYS_OF_WEEK.map((day) => {
                const slots = grouped[day] || [];
                return (
                  <div key={day} className="rounded-lg border p-4">
                    <div className="flex items-center justify-between">
                      <p className="text-sm font-semibold">{dayLabel[day]}</p>
                      <span className="text-xs text-muted-foreground">
                        {slots.length} slot
                      </span>
                    </div>
                    {slots.length === 0 ? (
                      <p className="mt-2 text-xs text-muted-foreground">Không làm việc</p>
                    ) : (
                      <div className="mt-2 grid grid-cols-2 gap-2">
                        {slots.map((s) => (
                          <div key={s.id} className="flex items-center gap-2 rounded-md border bg-primary/5 px-3 py-2 text-xs">
                            <Clock className="h-3 w-3 text-primary" />
                            <span className="font-medium">{formatTime(s.startTime)}–{formatTime(s.endTime)}</span>
                            <span className="text-muted-foreground">
                              ({s.slotDuration}p{s.bufferDuration ? ` +${s.bufferDuration}p` : ''})
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-base">Thời gian nghỉ</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {isLoading ? (
              <div className="space-y-3">
                {Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="h-16 animate-pulse rounded-lg bg-muted" />
                ))}
              </div>
            ) : timeOffs.length === 0 ? (
              <p className="text-sm text-muted-foreground">Không có thời gian nghỉ nào.</p>
            ) : (
              timeOffs.map((to) => (
                <div key={to.id} className="rounded-lg border p-4">
                  <div className="flex items-start gap-3">
                    <CalendarOff className="h-4 w-4 text-destructive mt-0.5" />
                    <div className="flex-1">
                      <p className="text-sm font-medium">{to.reason || 'Không có lý do'}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {formatDate(to.startDateTime, 'dd/MM/yyyy HH:mm')} - {formatDate(to.endDateTime, 'dd/MM/yyyy HH:mm')}
                      </p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </CardContent>
        </Card>
      </div>
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['DOCTOR']}><WorkingSchedule /></RoleGuard>;
}
