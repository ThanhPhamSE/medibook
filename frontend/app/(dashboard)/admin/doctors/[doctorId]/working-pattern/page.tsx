'use client';

import * as React from 'react';
import { useParams } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

import { apiGet, apiPost, apiPut, apiDelete, extractApiError } from '@/services/api';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { toast } from 'sonner';
import { Plus, Trash2, Clock, Edit, Calendar } from 'lucide-react';

// ---- Matches backend com.medibook.modules.schedule.dto.request.WorkingPatternRequest ----
const workingPatternSchema = z.object({
  doctorId: z.coerce.number().positive(),
  dayOfWeek: z.enum(['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']),
  startTime: z.string().min(1, 'Bắt buộc'),
  endTime: z.string().min(1, 'Bắt buộc'),
  slotDuration: z.coerce.number().min(10, 'Tối thiểu 10 phút'),
  bufferDuration: z.coerce.number().min(0).default(0),
}).refine((v) => v.startTime < v.endTime, {
  message: 'Giờ kết thúc phải sau giờ bắt đầu',
  path: ['endTime'],
});

// ---- Matches backend com.medibook.modules.schedule.dto.request.TimeOffRequest ----
const timeOffSchema = z.object({
  doctorId: z.coerce.number().positive(),
  date: z.string().min(1, 'Bắt buộc'),
  startTime: z.string().min(1, 'Bắt buộc'),
  endTime: z.string().min(1, 'Bắt buộc'),
  reason: z.string().min(1, 'Bắt buộc').max(255, 'Tối đa 255 ký tự'),
}).refine((v) => v.startTime < v.endTime, {
  message: 'Giờ kết thúc phải sau giờ bắt đầu',
  path: ['endTime'],
}).refine((v) => {
  const now = new Date();
  const selectedDate = new Date(v.date);
  const selectedStart = new Date(`${v.date}T${v.startTime}`);
  return selectedStart > now;
}, {
  message: 'Không thể chọn thời gian trong quá khứ',
  path: ['startTime'],
});

type TimeOffInput = z.infer<typeof timeOffSchema>;
type WorkingPatternInput = z.infer<typeof workingPatternSchema>;

// ---- Payload shapes actually sent over the wire ----
// Backend WorkingPatternUpdateRequest / TimeOffUpdateRequest have no doctorId field,
// so it must be stripped before PUT-ing, unlike the create payloads.
type WorkingPatternCreatePayload = WorkingPatternInput;
type WorkingPatternUpdatePayload = Omit<WorkingPatternInput, 'doctorId'>;

// Request DTO field names (confirmed via 400 validation error keys from
// POST /schedules/time-offs: { "startDatetime": "must not be null", ... }).
// NOTE: this is intentionally lowercase-d and DIFFERENT from TimeOffResponse
// below, which uses "startDateTime" (capital D). The backend's request DTO
// and response DTO for time-offs use inconsistent casing for the same
// concept — this isn't a frontend bug, it's a backend naming mismatch we
// have to mirror on both sides to interoperate correctly.
interface TimeOffCreatePayload {
  doctorId: number;
  startDatetime: string;
  endDatetime: string;
  reason: string;
}
type TimeOffUpdatePayload = Omit<TimeOffCreatePayload, 'doctorId'>;

const dayLabel: Record<string, string> = {
  MON: 'Thứ 2', TUE: 'Thứ 3', WED: 'Thứ 4', THU: 'Thứ 5',
  FRI: 'Thứ 6', SAT: 'Thứ 7', SUN: 'Chủ nhật',
};
const DAYS_OF_WEEK = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'] as const;

// ---- Types matching backend response DTOs ----
interface WorkingPatternResponse {
  id: number;
  doctorId: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  slotDuration: number;
  bufferDuration: number;
}

interface DoctorScheduleResponse {
  doctorId: number;
  workingPartterns: WorkingPatternResponse[];
  timeOffs: TimeOffResponse[];
}

// CONFIRMED via Network tab (GET /schedules/doctor/{id}): backend returns
// `startDateTime` / `endDateTime` (capital D) here. This is DIFFERENT from
// the request DTO (see TimeOffCreatePayload above, which uses lowercase-d
// `startDatetime`/`endDatetime` per the validation error keys) — the backend
// simply isn't consistent between its request and response DTOs for this
// resource. Do not "fix" one to match the other; each must match its own
// confirmed wire format.
interface TimeOffResponse {
  id: number;
  doctorId: number;
  startDateTime: string | null;
  endDateTime: string | null;
  reason: string;
}

interface DoctorResponse {
  id: number;
  fullName: string;
  email: string;
  specialtyName: string;
  consultationFee: number;
  status: string;
}

// Splits an ISO-ish LocalDateTime string ("2024-01-01T09:30:00") into
// separate date ("2024-01-01") and time ("09:30") pieces for form inputs.
// Defensive against undefined/null input (e.g. a backend field-name mismatch)
// so a single bad record can't crash the whole page with a runtime TypeError.
function splitDateTime(dateTime?: string | null): { date: string; time: string } {
  if (!dateTime) return { date: '', time: '' };
  const [date, timePart = ''] = dateTime.split('T');
  return { date, time: timePart.slice(0, 5) };
}

function useDoctorSchedule(doctorId: number) {
  return useQuery({
    queryKey: ['doctor-schedule', doctorId],
    queryFn: () => apiGet<DoctorScheduleResponse>(`/schedules/doctor/${doctorId}`),
    enabled: !!doctorId,
  });
}

function useDoctorDetails(doctorId: number) {
  return useQuery({
    queryKey: ['doctor', doctorId],
    queryFn: () => apiGet<DoctorResponse>(`/doctors/${doctorId}`),
    enabled: !!doctorId,
  });
}

function useCreateWorkingPattern() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: WorkingPatternCreatePayload) =>
      apiPost<WorkingPatternResponse>('/schedules/working-patterns', payload),
    onSuccess: (_data, variables) => {
      qc.invalidateQueries({ queryKey: ['doctor-schedule', variables.doctorId] });
      toast.success('Đã tạo lịch làm việc');
    },
    onError: (err) => {
      toast.error(extractApiError(err, 'Không thể tạo lịch làm việc'));
    },
  });
}

function useDeleteWorkingPattern(doctorId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete<void>(`/schedules/working-patterns/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctor-schedule', doctorId] });
      toast.success('Đã xoá khung giờ');
    },
    onError: (err) => {
      toast.error(extractApiError(err, 'Không thể xoá khung giờ'));
    },
  });
}

function useUpdateWorkingPattern(doctorId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: WorkingPatternUpdatePayload }) =>
      apiPut<WorkingPatternResponse>(`/schedules/working-patterns/${id}`, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctor-schedule', doctorId] });
      toast.success('Đã cập nhật khung giờ');
    },
    onError: (err) => {
      toast.error(extractApiError(err, 'Không thể cập nhật khung giờ'));
    },
  });
}

function useCreateTimeOff(doctorId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: TimeOffCreatePayload) =>
      apiPost<TimeOffResponse>('/schedules/time-offs', payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctor-schedule', doctorId] });
      toast.success('Đã thêm thời gian nghỉ');
    },
    onError: (err) => {
      toast.error(extractApiError(err, 'Không thể thêm thời gian nghỉ'));
    },
  });
}

function useUpdateTimeOff(doctorId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: TimeOffUpdatePayload }) =>
      apiPut<TimeOffResponse>(`/schedules/time-offs/${id}`, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctor-schedule', doctorId] });
      toast.success('Đã cập nhật thời gian nghỉ');
    },
    onError: (err) => {
      toast.error(extractApiError(err, 'Không thể cập nhật thời gian nghỉ'));
    },
  });
}

function useDeleteTimeOff(doctorId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete<void>(`/schedules/time-offs/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['doctor-schedule', doctorId] });
      toast.success('Đã xoá thời gian nghỉ');
    },
    onError: (err) => {
      toast.error(extractApiError(err, 'Không thể xoá thời gian nghỉ'));
    },
  });
}

const DEFAULT_PATTERN_VALUES = (doctorId: number): WorkingPatternInput => ({
  doctorId,
  dayOfWeek: 'MON',
  startTime: '09:00',
  endTime: '17:00',
  slotDuration: 30,
  bufferDuration: 0,
});

const DEFAULT_TIME_OFF_VALUES = (doctorId: number): TimeOffInput => ({
  doctorId,
  date: '',
  startTime: '',
  endTime: '',
  reason: '',
});

function AdminDoctorWorkingPattern({ doctorId }: { doctorId: number }) {
  const { data, isLoading } = useDoctorSchedule(doctorId);
  const { data: doctorData, isLoading: doctorLoading } = useDoctorDetails(doctorId);

  const createMut = useCreateWorkingPattern();
  const updateMut = useUpdateWorkingPattern(doctorId);
  const deleteMut = useDeleteWorkingPattern(doctorId);

  const createTimeOffMut = useCreateTimeOff(doctorId);
  const updateTimeOffMut = useUpdateTimeOff(doctorId);
  const deleteTimeOffMut = useDeleteTimeOff(doctorId);

  const [editingPattern, setEditingPattern] = React.useState<WorkingPatternResponse | null>(null);
  const [editingTimeOff, setEditingTimeOff] = React.useState<TimeOffResponse | null>(null);

  const patterns: WorkingPatternResponse[] = data?.workingPartterns ?? [];
  const timeOffs: TimeOffResponse[] = data?.timeOffs ?? [];
  const grouped = React.useMemo(() => {
    const acc: Record<string, WorkingPatternResponse[]> = {};
    patterns.forEach((p) => { (acc[p.dayOfWeek] = acc[p.dayOfWeek] || []).push(p); });
    return acc;
  }, [patterns]);

  // ---------------- Working pattern form ----------------
  const form = useForm<WorkingPatternInput>({
    resolver: zodResolver(workingPatternSchema),
    defaultValues: DEFAULT_PATTERN_VALUES(doctorId),
  });

  const onSubmit = (values: WorkingPatternInput) => {
    if (editingPattern) {
      const { doctorId: _doctorId, ...updatePayload } = values;
      updateMut.mutate({ id: editingPattern.id, payload: updatePayload }, {
        onSuccess: () => {
          setEditingPattern(null);
          form.reset(DEFAULT_PATTERN_VALUES(doctorId));
        },
      });
    } else {
      createMut.mutate(values, {
        onSuccess: () => form.reset(DEFAULT_PATTERN_VALUES(doctorId)),
      });
    }
  };

  const handleEdit = (pattern: WorkingPatternResponse) => {
    setEditingPattern(pattern);
    form.reset({
      doctorId,
      dayOfWeek: pattern.dayOfWeek as WorkingPatternInput['dayOfWeek'],
      startTime: pattern.startTime,
      endTime: pattern.endTime,
      slotDuration: pattern.slotDuration,
      bufferDuration: pattern.bufferDuration,
    });
  };

  const handleCancelEdit = () => {
    setEditingPattern(null);
    form.reset(DEFAULT_PATTERN_VALUES(doctorId));
  };

  // ---------------- Time off form ----------------
  const timeOffForm = useForm<TimeOffInput>({
    resolver: zodResolver(timeOffSchema),
    defaultValues: DEFAULT_TIME_OFF_VALUES(doctorId),
  });

  const onTimeOffSubmit = (values: TimeOffInput) => {
    // These become the outgoing request payload, so they must use the
    // request DTO's casing (lowercase-d) — see TimeOffCreatePayload above.
    const startDatetime = `${values.date}T${values.startTime}`;
    const endDatetime = `${values.date}T${values.endTime}`;

    if (editingTimeOff) {
      updateTimeOffMut.mutate(
        { id: editingTimeOff.id, payload: { startDatetime, endDatetime, reason: values.reason } },
        {
          onSuccess: () => {
            setEditingTimeOff(null);
            timeOffForm.reset(DEFAULT_TIME_OFF_VALUES(doctorId));
          },
        }
      );
    } else {
      createTimeOffMut.mutate(
        { doctorId: values.doctorId, startDatetime, endDatetime, reason: values.reason },
        { onSuccess: () => timeOffForm.reset(DEFAULT_TIME_OFF_VALUES(doctorId)) }
      );
    }
  };

  const handleEditTimeOff = (timeOff: TimeOffResponse) => {
    setEditingTimeOff(timeOff);
    const start = splitDateTime(timeOff.startDateTime);
    const end = splitDateTime(timeOff.endDateTime);
    timeOffForm.reset({
      doctorId,
      date: start.date,
      startTime: start.time,
      endTime: end.time,
      reason: timeOff.reason,
    });
  };

  const handleCancelEditTimeOff = () => {
    setEditingTimeOff(null);
    timeOffForm.reset(DEFAULT_TIME_OFF_VALUES(doctorId));
  };

  return (
    <PageContainer>
      <PageHeader
        title="Lịch làm việc của bác sĩ"
        description="Thiết lập khung giờ làm việc hàng tuần cho bác sĩ."
        breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Bác sĩ', href: '/admin/doctors' }, { label: 'Lịch làm việc' }]}
        actions={
          !doctorLoading && doctorData ? (
            <div className="flex items-center gap-3 rounded-lg border bg-card p-4">
              <Avatar className="h-12 w-12 border bg-primary/10">
                <AvatarFallback className="bg-transparent text-sm font-semibold text-primary">
                  {doctorData.fullName.split(' ').map((n) => n[0]).join('').toUpperCase()}
                </AvatarFallback>
              </Avatar>
              <div>
                <p className="font-semibold">{doctorData.fullName}</p>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <span>{doctorData.specialtyName}</span>
                  <span>•</span>
                  <Badge variant={doctorData.status === 'ACTIVE' ? 'success' : 'secondary'} className="text-xs">
                    {doctorData.status}
                  </Badge>
                </div>
              </div>
            </div>
          ) : undefined
        }
      />

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader><CardTitle className="text-base">Khung giờ theo tuần</CardTitle></CardHeader>
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
                          {slots.length} khung giờ
                        </span>
                      </div>
                      {slots.length === 0 ? (
                        <p className="mt-2 text-xs text-muted-foreground">Không làm việc</p>
                      ) : (
                        <div className="mt-2 flex flex-wrap gap-2">
                          {slots.map((p) => (
                            <div
                              key={p.id}
                              className={`group flex items-center gap-2 rounded-md border bg-card px-2.5 py-1.5 text-xs ${editingPattern?.id === p.id ? 'ring-2 ring-primary' : ''}`}
                            >
                              <Clock className="h-3 w-3 text-primary" />
                              <span className="font-medium">{p.startTime}–{p.endTime}</span>
                              <span className="text-muted-foreground">
                                {p.slotDuration}p{p.bufferDuration ? ` +${p.bufferDuration}p nghỉ` : ''}
                              </span>
                              <button
                                type="button"
                                onClick={() => handleEdit(p)}
                                className="text-muted-foreground hover:text-primary"
                                title="Sửa"
                              >
                                <Edit className="h-3 w-3" />
                              </button>
                              <AlertDialog>
                                <AlertDialogTrigger asChild>
                                  <button type="button" className="text-muted-foreground hover:text-destructive" title="Xoá">
                                    <Trash2 className="h-3 w-3" />
                                  </button>
                                </AlertDialogTrigger>
                                <AlertDialogContent>
                                  <AlertDialogHeader>
                                    <AlertDialogTitle>Xoá khung giờ này?</AlertDialogTitle>
                                    <AlertDialogDescription>
                                      Thao tác này sẽ xoá khung giờ {dayLabel[day]} {p.startTime}–{p.endTime} khỏi lịch làm việc.
                                    </AlertDialogDescription>
                                  </AlertDialogHeader>
                                  <AlertDialogFooter>
                                    <AlertDialogCancel>Huỷ</AlertDialogCancel>
                                    <AlertDialogAction
                                      onClick={() => {
                                        if (editingPattern?.id === p.id) handleCancelEdit();
                                        deleteMut.mutate(p.id);
                                      }}
                                    >
                                      Xoá
                                    </AlertDialogAction>
                                  </AlertDialogFooter>
                                </AlertDialogContent>
                              </AlertDialog>
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
              {timeOffs.length === 0 ? (
                <p className="text-sm text-muted-foreground">Chưa có thời gian nghỉ nào</p>
              ) : (
                timeOffs.map((to) => (
                  <div
                    key={to.id}
                    className={`flex items-center justify-between rounded-lg border p-3 ${editingTimeOff?.id === to.id ? 'ring-2 ring-primary' : ''}`}
                  >
                    <div className="flex items-center gap-3">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-sm font-medium">{to.reason}</p>
                        <p className="text-xs text-muted-foreground">
                          {to.startDateTime ? new Date(to.startDateTime).toLocaleString('vi-VN') : 'Chưa có ngày (dữ liệu lỗi)'}
                          {to.startDateTime || to.endDateTime ? ' - ' : ''}
                          {to.endDateTime ? new Date(to.endDateTime).toLocaleString('vi-VN') : ''}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-1">
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="text-muted-foreground hover:text-primary"
                        title="Sửa"
                        onClick={() => handleEditTimeOff(to)}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button variant="ghost" size="icon" className="text-destructive hover:text-destructive" title="Xoá">
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>Xoá thời gian nghỉ?</AlertDialogTitle>
                            <AlertDialogDescription>
                              Thao tác này sẽ xoá thời gian nghỉ &quot;{to.reason}&quot;.
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>Huỷ</AlertDialogCancel>
                            <AlertDialogAction
                              onClick={() => {
                                if (editingTimeOff?.id === to.id) handleCancelEditTimeOff();
                                deleteTimeOffMut.mutate(to.id);
                              }}
                            >
                              Xoá
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    </div>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </Card>

        <div className="space-y-6">
          <Card className="h-fit">
            <CardHeader>
              <CardTitle className="text-base">
                {editingPattern ? 'Cập nhật khung giờ làm việc' : 'Thêm khung giờ làm việc'}
              </CardTitle>
            </CardHeader>
            <CardContent>
              {/* key forces the form (and its Radix Select) to remount when the
                  editing target changes, so fields/labels sync on the first click
                  instead of requiring a second click to "catch up". */}
              <Form {...form} key={editingPattern?.id ?? 'new-pattern'}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                  <FormField control={form.control} name="dayOfWeek" render={({ field }) => (
                    <FormItem>
                      <FormLabel>Ngày</FormLabel>
                      <Select onValueChange={field.onChange} defaultValue={field.value} disabled={!!editingPattern}>
                        <FormControl><SelectTrigger><SelectValue /></SelectTrigger></FormControl>
                        <SelectContent>
                          {DAYS_OF_WEEK.filter((d) => !grouped[d] || grouped[d].length === 0 || (editingPattern && editingPattern.dayOfWeek === d)).map((d) => (
                            <SelectItem key={d} value={d}>{dayLabel[d]}</SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )} />

                  <div className="grid grid-cols-2 gap-3">
                    <FormField control={form.control} name="startTime" render={({ field }) => (
                      <FormItem>
                        <FormLabel>Bắt đầu</FormLabel>
                        <FormControl><Input type="time" {...field} /></FormControl>
                        <FormMessage />
                      </FormItem>
                    )} />
                    <FormField control={form.control} name="endTime" render={({ field }) => (
                      <FormItem>
                        <FormLabel>Kết thúc</FormLabel>
                        <FormControl><Input type="time" {...field} /></FormControl>
                        <FormMessage />
                      </FormItem>
                    )} />
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <FormField control={form.control} name="slotDuration" render={({ field }) => (
                      <FormItem>
                        <FormLabel>Thời lượng slot (phút)</FormLabel>
                        <FormControl><Input type="number" min={10} step={5} {...field} /></FormControl>
                        <FormMessage />
                      </FormItem>
                    )} />
                    <FormField control={form.control} name="bufferDuration" render={({ field }) => (
                      <FormItem>
                        <FormLabel>Thời gian nghỉ giữa (phút)</FormLabel>
                        <FormControl><Input type="number" min={0} step={5} {...field} /></FormControl>
                        <FormMessage />
                      </FormItem>
                    )} />
                  </div>

                  <div className="flex gap-2">
                    {editingPattern && (
                      <Button type="button" variant="outline" onClick={handleCancelEdit} className="flex-1">
                        Huỷ
                      </Button>
                    )}
                    <Button type="submit" className="flex-1" disabled={createMut.isPending || updateMut.isPending}>
                      {editingPattern ? <Edit className="mr-2 h-4 w-4" /> : <Plus className="mr-2 h-4 w-4" />}
                      {editingPattern ? 'Cập nhật' : 'Thêm khung giờ'}
                    </Button>
                  </div>
                </form>
              </Form>
            </CardContent>
          </Card>

          <Card className="h-fit">
            <CardHeader>
              <CardTitle className="text-base">
                {editingTimeOff ? 'Cập nhật thời gian nghỉ' : 'Thêm thời gian nghỉ'}
              </CardTitle>
            </CardHeader>
            <CardContent>
              {/* Same remount-on-key-change fix applied here for consistency,
                  in case this form later gains a Select or similar uncontrolled
                  Radix input. */}
              <Form {...timeOffForm} key={editingTimeOff?.id ?? 'new-timeoff'}>
                <form onSubmit={timeOffForm.handleSubmit(onTimeOffSubmit)} className="space-y-4">
                  <FormField control={timeOffForm.control} name="date" render={({ field }) => (
                    <FormItem>
                      <FormLabel>Ngày</FormLabel>
                      <FormControl>
                        <Input
                          type="date"
                          {...field}
                          min={new Date().toISOString().split('T')[0]}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />

                  <div className="grid grid-cols-2 gap-3">
                    <FormField control={timeOffForm.control} name="startTime" render={({ field }) => (
                      <FormItem>
                        <FormLabel>Giờ bắt đầu</FormLabel>
                        <FormControl><Input type="time" {...field} /></FormControl>
                        <FormMessage />
                      </FormItem>
                    )} />
                    <FormField control={timeOffForm.control} name="endTime" render={({ field }) => (
                      <FormItem>
                        <FormLabel>Giờ kết thúc</FormLabel>
                        <FormControl><Input type="time" {...field} /></FormControl>
                        <FormMessage />
                      </FormItem>
                    )} />
                  </div>

                  <FormField control={timeOffForm.control} name="reason" render={({ field }) => (
                    <FormItem>
                      <FormLabel>Lý do</FormLabel>
                      <FormControl><Textarea placeholder="Lý do nghỉ..." {...field} /></FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />

                  <div className="flex gap-2">
                    {editingTimeOff && (
                      <Button type="button" variant="outline" onClick={handleCancelEditTimeOff} className="flex-1">
                        Huỷ
                      </Button>
                    )}
                    <Button
                      type="submit"
                      className="flex-1"
                      disabled={createTimeOffMut.isPending || updateTimeOffMut.isPending}
                    >
                      {editingTimeOff ? <Edit className="mr-2 h-4 w-4" /> : <Calendar className="mr-2 h-4 w-4" />}
                      {editingTimeOff ? 'Cập nhật' : 'Thêm thời gian nghỉ'}
                    </Button>
                  </div>
                </form>
              </Form>
            </CardContent>
          </Card>
        </div>
      </div>
    </PageContainer>
  );
}

export default function Page() {
  const params = useParams<{ doctorId: string }>();
  const doctorId = Number(params.doctorId);

  return (
    <RoleGuard roles={['ADMIN']}>
      <AdminDoctorWorkingPattern doctorId={doctorId} />
    </RoleGuard>
  );
}
