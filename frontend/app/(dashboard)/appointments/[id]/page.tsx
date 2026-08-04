'use client';

import * as React from 'react';
import { useParams, useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import {
  useAppointment,
  useCancelAppointment,
  useRescheduleAppointment,
  useConfirmAppointment,
  useCompleteAppointment,
  useNoShowAppointment,
  useDoctor,
  useDoctorReviews,
  useCreateReview,
  useCreateMedicalRecord,
  usePaymentStatus,
} from '@/hooks/use-api';
import { useAuth } from '@/contexts/auth-context';
import { PageContainer, PageHeader } from '@/components/page-header';
import { AppointmentStatusBadge } from '@/components/appointment-status-badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Skeleton } from '@/components/skeletons';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Calendar, Clock, FileText, CheckCircle2, XCircle, CalendarClock, ArrowLeft, PartyPopper,
  Stethoscope, Wallet, Star, ClipboardList, Loader2, AlertTriangle,
} from 'lucide-react';
import { formatDate, formatTime } from '@/utils/format';
import { EmptyState } from '@/components/empty-state';
import { cn } from '@/lib/utils';
import { paymentService } from '@/services/payment.service';
import { toast } from 'sonner';

const TIMELINE_STEPS = [
  { key: 'CREATED', label: 'Đã tạo' },
  { key: 'PENDING', label: 'Chờ xác nhận' },
  { key: 'CONFIRMED', label: 'Đã xác nhận' },
  { key: 'COMPLETED', label: 'Hoàn thành' },
] as const;

function formatCurrency(value?: number) {
  if (value === undefined || value === null) return 'N/A';
  return value.toLocaleString('vi-VN') + ' đ';
}

export default function AppointmentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const params = useSearchParams();
  const router = useRouter();
  const { user } = useAuth();
  const justBooked = params.get('booked') === '1';

  const { data: appt, isLoading } = useAppointment(id);
  const cancelMutation = useCancelAppointment();
  const rescheduleMutation = useRescheduleAppointment();
  const confirmMutation = useConfirmAppointment();
  const completeMutation = useCompleteAppointment();
  const noShowMutation = useNoShowAppointment();
  const createReviewMutation = useCreateReview();
  const createMedicalRecordMutation = useCreateMedicalRecord();

  // Trạng thái thanh toán
  const { data: paymentStatus } = usePaymentStatus(appt?.id);
  const isPaid = paymentStatus?.status === 'PAID';

  // Lấy chuyên khoa từ hồ sơ bác sĩ (appointment response không có field riêng)
  const { data: doctor } = useDoctor(appt?.doctorId ? String(appt.doctorId) : '');
  const specialtyName = doctor?.specialtyName ?? null;

  // Kiểm tra lịch hẹn này đã được đánh giá chưa, để ẩn nút "Đánh giá"
  const { data: doctorReviews } = useDoctorReviews(appt?.doctorId ? String(appt.doctorId) : '');
  const alreadyReviewed = React.useMemo(
    () => (doctorReviews ?? []).some((r: any) => r.appointmentId === appt?.id),
    [doctorReviews, appt?.id]
  );

  const [rescheduleOpen, setRescheduleOpen] = React.useState(false);
  const [newDate, setNewDate] = React.useState('');
  const [newTime, setNewTime] = React.useState('');

  const [reviewOpen, setReviewOpen] = React.useState(false);
  const [rating, setRating] = React.useState(5);
  const [comment, setComment] = React.useState('');

  // --- Medical record trước khi hoàn thành lịch hẹn ---
  const [completeOpen, setCompleteOpen] = React.useState(false);
  const [diagnosis, setDiagnosis] = React.useState('');
  const [symptoms, setSymptoms] = React.useState('');
  const [prescription, setPrescription] = React.useState('');
  const [recordNotes, setRecordNotes] = React.useState('');
  const [paying, setPaying] = React.useState(false);

  const handlePayment = async () => {
    if (!appt?.id) return;
    try {
      setPaying(true);
      const res = await paymentService.createLink(appt.id);
      if (res?.checkoutUrl) {
        window.location.href = res.checkoutUrl;
      } else {
        toast.error('Không thể tạo liên kết thanh toán');
      }
    } catch (error: any) {
      toast.error(error?.message || 'Có lỗi xảy ra khi tạo liên kết thanh toán');
    } finally {
      setPaying(false);
    }
  };

  const resetMedicalRecordForm = () => {
    setDiagnosis('');
    setSymptoms('');
    setPrescription('');
    setRecordNotes('');
  };

  // Backend chỉ cho tạo medical record khi appointment đã COMPLETED,
  // nên phải complete trước rồi mới tạo record ngay sau đó.
  const handleSubmitMedicalRecordAndComplete = () => {
    if (!appt || !diagnosis.trim()) return;

    const createRecord = () => {
      createMedicalRecordMutation.mutate(
        {
          appointmentId: appt.id,
          patientId: appt.patientId,
          doctorId: appt.doctorId,
          diagnosis: diagnosis.trim(),
          symptoms: symptoms.trim(),
          prescription: prescription.trim(),
          notes: recordNotes.trim() || undefined,
        },
        {
          onSuccess: () => {
            setCompleteOpen(false);
            resetMedicalRecordForm();
          },
          // Nếu tạo record lỗi, KHÔNG đóng dialog — giữ nguyên dữ liệu đã nhập
          // để bác sĩ có thể bấm lưu lại (appointment lúc này đã COMPLETED rồi).
        }
      );
    };

    if (appt.status === 'CONFIRMED') {
      completeMutation.mutate(appt.id, { onSuccess: createRecord });
    } else {
      // Trường hợp retry: appointment đã COMPLETED từ lần bấm trước nhưng
      // record chưa tạo được (lỗi mạng, validate...), chỉ cần tạo lại record.
      createRecord();
    }
  };

  const isSubmittingCompletion = createMedicalRecordMutation.isPending || completeMutation.isPending;

  if (isLoading) return <PageContainer><Skeleton className="h-96 rounded-xl" /></PageContainer>;
  if (!appt) {
    return (
      <PageContainer>
        <EmptyState title="Không tìm thấy lịch hẹn" action={<Button asChild><Link href="/appointments">Quay lại</Link></Button>} />
      </PageContainer>
    );
  }

  const canCancel = appt.status === 'PENDING' || appt.status === 'CONFIRMED';
  const isCancelledOrNoShow = appt.status === 'CANCELLED' || appt.status === 'NO_SHOW';

  const currentStepIndex = isCancelledOrNoShow
    ? -1
    : TIMELINE_STEPS.findIndex((t) => t.key === appt.status) === -1
      ? 0 // status không khớp bước nào -> coi như mới ở bước "Created"
      : TIMELINE_STEPS.findIndex((t) => t.key === appt.status);

  return (
    <PageContainer>
      {justBooked && (
        <div className="mb-6 flex items-center gap-3 rounded-xl border border-success/30 bg-success/10 p-4 text-success animate-slide-up">
          <PartyPopper className="h-5 w-5" />
          <div>
            <p className="text-sm font-semibold">Đặt lịch thành công!</p>
            <p className="text-xs">
              Mã đặt lịch của bạn là <span className="font-mono font-bold">{appt.bookingCode}</span>. Vui lòng lưu lại để tra cứu.
            </p>
          </div>
        </div>
      )}

      <PageHeader
        breadcrumbs={[
          { label: 'Trang chủ', href: '/dashboard' },
          { label: 'Lịch hẹn', href: '/appointments' },
          { label: appt.bookingCode },
        ]}
        title={`Lịch hẹn ${appt.bookingCode}`}
        description={appt.doctorName}
        actions={<Button variant="outline" onClick={() => router.back()}><ArrowLeft className="mr-2 h-4 w-4" /> Quay lại</Button>}
      />

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          {/* Chi tiết lịch khám */}
          <Card>
            <CardHeader><CardTitle className="text-base">Chi tiết lịch khám</CardTitle></CardHeader>
            <CardContent className="grid gap-4 sm:grid-cols-2">
              <InfoRow icon={FileText} label="Mã đặt lịch" value={appt.bookingCode} mono />
              <InfoRow icon={Stethoscope} label="Bác sĩ" value={appt.doctorName} />
              <InfoRow icon={Stethoscope} label="Chuyên khoa" value={specialtyName ?? 'N/A'} />
              <InfoRow icon={Calendar} label="Ngày" value={formatDate(appt.startTime || appt.date, 'EEEE, dd/MM/yyyy')} />
              <InfoRow icon={Clock} label="Giờ" value={`${formatTime(appt.startTime || appt.date)} — ${formatTime(appt.endTime || appt.date)}`} />
              <InfoRow icon={Wallet} label="Phí khám" value={formatCurrency(appt.consultationFee)} />
              {appt.notes && <InfoRow icon={FileText} label="Ghi chú" value={appt.notes} />}
            </CardContent>
          </Card>

          {/* Timeline */}
          <Card>
            <CardHeader><CardTitle className="text-base">Tiến trình</CardTitle></CardHeader>
            <CardContent>
              {isCancelledOrNoShow ? (
                <div className="flex items-center gap-3 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-destructive">
                  <XCircle className="h-5 w-5" />
                  <p className="text-sm font-medium">
                    {appt.status === 'CANCELLED' ? 'Lịch hẹn này đã bị hủy.' : 'Bạn đã không đến vào lịch hẹn này.'}
                  </p>
                </div>
              ) : (
                <div className="flex items-center">
                  {TIMELINE_STEPS.map((step, i) => {
                    const done = i <= currentStepIndex;
                    const isLast = i === TIMELINE_STEPS.length - 1;
                    return (
                      <React.Fragment key={step.key}>
                        <div className="flex flex-col items-center gap-2">
                          <div
                            className={cn(
                              'flex h-9 w-9 items-center justify-center rounded-full border-2 text-xs font-semibold',
                              done ? 'border-primary bg-primary text-primary-foreground' : 'border-muted-foreground/30 text-muted-foreground'
                            )}
                          >
                            {done ? <CheckCircle2 className="h-4 w-4" /> : i + 1}
                          </div>
                          <p className={cn('text-xs font-medium', done ? 'text-foreground' : 'text-muted-foreground')}>
                            {step.label}
                          </p>
                        </div>
                        {!isLast && (
                          <div className={cn('mx-2 h-0.5 flex-1', i < currentStepIndex ? 'bg-primary' : 'bg-muted-foreground/20')} />
                        )}
                      </React.Fragment>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <div className="space-y-4">
          <Card className="h-fit">
            <CardHeader><CardTitle className="text-base">Trạng thái</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <AppointmentStatusBadge status={appt.status} />

              <div className="border-t pt-4">
                <p className="text-xs uppercase text-muted-foreground">Bệnh nhân</p>
                <div className="mt-2 flex items-center gap-3">
                  <Avatar className="h-10 w-10 border bg-primary/10">
                    <AvatarFallback className="bg-transparent text-xs font-semibold text-primary">
                      {appt.patientName?.split(' ').slice(0, 2).map((s) => s[0]).join('') ?? '?'}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-sm font-medium">{appt.patientName}</p>
                    <p className="text-xs text-muted-foreground">Bệnh nhân</p>
                  </div>
                </div>
              </div>

              <div className="space-y-2 border-t pt-4">
              {user?.roleName === 'CUSTOMER' && appt.status === 'PENDING' && (
                  isPaid ? (
                    <div className="flex items-center justify-center gap-2 rounded-lg border border-emerald-500/30 bg-emerald-500/10 py-3 px-4 text-sm font-medium text-emerald-700 dark:text-emerald-400">
                      <CheckCircle2 className="h-4 w-4" />
                      Cuộc hẹn đã được thanh toán
                    </div>
                  ) : paymentStatus?.status === 'CANCELLED' ? (
                    <div className="space-y-2">
                      <div className="flex items-center gap-2 rounded-lg border border-amber-500/30 bg-amber-500/10 py-2 px-3 text-xs text-amber-700 dark:text-amber-400">
                        <AlertTriangle className="h-4 w-4 shrink-0" />
                        Thanh toán trước bị hủy hoặc hết hạn. Bạn có thể thử lại.
                      </div>
                      <Button className="w-full bg-emerald-600 hover:bg-emerald-700 text-white" onClick={handlePayment} disabled={paying}>
                        {paying ? (
                          <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            Đang tạo liên kết...
                          </>
                        ) : (
                          <>
                            <Wallet className="mr-2 h-4 w-4" />
                            Thử thanh toán lại
                          </>
                        )}
                      </Button>
                    </div>
                  ) : (
                    <Button className="w-full bg-emerald-600 hover:bg-emerald-700 text-white" onClick={handlePayment} disabled={paying}>
                      {paying ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Đang tạo liên kết...
                        </>
                      ) : (
                        <>
                          <Wallet className="mr-2 h-4 w-4" />
                          Thanh toán qua PayOS (VietQR)
                        </>
                      )}
                    </Button>
                  )
                )}

                {canCancel && (
                  <>
                    <Dialog open={rescheduleOpen} onOpenChange={setRescheduleOpen}>
                      <DialogTrigger asChild>
                        <Button variant="outline" className="w-full">
                          <CalendarClock className="mr-2 h-4 w-4" /> Đổi lịch
                        </Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>Đổi lịch hẹn</DialogTitle>
                          <DialogDescription>Chọn ngày và giờ mới cho buổi khám.</DialogDescription>
                        </DialogHeader>
                        <div className="space-y-3 py-2">
                          <Input type="date" min={new Date().toISOString().split('T')[0]} value={newDate} onChange={(e) => setNewDate(e.target.value)} />
                          <Input type="time" value={newTime} onChange={(e) => setNewTime(e.target.value)} />
                        </div>
                        <DialogFooter>
                          <Button variant="outline" onClick={() => setRescheduleOpen(false)}>Hủy</Button>
                          <Button
                            disabled={!newDate || !newTime || rescheduleMutation.isPending}
                            onClick={() => rescheduleMutation.mutate({ id: appt.id, date: newDate, startTime: newTime }, { onSuccess: () => setRescheduleOpen(false) })}
                          >
                            {rescheduleMutation.isPending ? 'Đang lưu...' : 'Xác nhận đổi lịch'}
                          </Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>

                    <Button
                      variant="destructive"
                      className="w-full"
                      disabled={cancelMutation.isPending}
                      onClick={() => cancelMutation.mutate({ id: appt.id })}
                    >
                      <XCircle className="mr-2 h-4 w-4" /> Hủy lịch
                    </Button>
                  </>
                )}

                {appt.status === 'COMPLETED' && !alreadyReviewed && (
                  <Dialog open={reviewOpen} onOpenChange={setReviewOpen}>
                    <DialogTrigger asChild>
                      <Button className="w-full">
                        <Star className="mr-2 h-4 w-4" /> Đánh giá
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Đánh giá bác sĩ</DialogTitle>
                        <DialogDescription>Chia sẻ trải nghiệm khám bệnh của bạn với {appt.doctorName}.</DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4 py-2">
                        <div className="flex items-center gap-1">
                          {[1, 2, 3, 4, 5].map((n) => (
                            <button key={n} type="button" onClick={() => setRating(n)} aria-label={`${n} sao`}>
                              <Star
                                className={cn('h-7 w-7', n <= rating ? 'fill-amber-400 text-amber-400' : 'text-muted-foreground/30')}
                              />
                            </button>
                          ))}
                        </div>
                        <Textarea
                          placeholder="Nhận xét của bạn (không bắt buộc)..."
                          value={comment}
                          onChange={(e) => setComment(e.target.value)}
                          rows={4}
                        />
                      </div>
                      <DialogFooter>
                        <Button variant="outline" onClick={() => setReviewOpen(false)}>Hủy</Button>
                        <Button
                          disabled={createReviewMutation.isPending}
                          onClick={() =>
                            createReviewMutation.mutate(
                              { appointmentId: appt.id, doctorId: appt.doctorId, rating, comment },
                              { onSuccess: () => setReviewOpen(false) }
                            )
                          }
                        >
                          {createReviewMutation.isPending ? 'Đang gửi...' : 'Gửi đánh giá'}
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                )}

                {appt.status === 'COMPLETED' && alreadyReviewed && user?.roleName === 'CUSTOMER' && (
                  <div className="flex items-center justify-center gap-2 rounded-lg border bg-muted/50 py-2 text-sm text-muted-foreground">
                    <Star className="h-4 w-4 fill-amber-400 text-amber-400" />
                    Bạn đã đánh giá lịch hẹn này
                  </div>
                )}

                {(user?.roleName === 'DOCTOR' || user?.roleName === 'ADMIN') && appt.status === 'PENDING' && (
                  <Button className="w-full" disabled={confirmMutation.isPending} onClick={() => confirmMutation.mutate(appt.id)}>
                    <CheckCircle2 className="mr-2 h-4 w-4" /> Xác nhận
                  </Button>
                )}

                {/* Hoàn thành: bắt buộc nhập medical record trước.
                    Nút trigger chỉ hiện khi CONFIRMED, nhưng Dialog được
                    render độc lập (điều khiển bởi completeOpen) để không bị
                    unmount giữa chừng khi appt.status đổi sang COMPLETED
                    sau bước complete (trước khi tạo record xong). */}
                {(user?.roleName === 'DOCTOR' || user?.roleName === 'ADMIN') && appt.status === 'CONFIRMED' && (
                  <Button className="w-full" onClick={() => setCompleteOpen(true)}>
                    <CheckCircle2 className="mr-2 h-4 w-4" /> Đánh dấu hoàn thành
                  </Button>
                )}

                {(user?.roleName === 'DOCTOR' || user?.roleName === 'ADMIN') && (
                  <Dialog
                    open={completeOpen}
                    onOpenChange={(open) => {
                      setCompleteOpen(open);
                      if (!open) resetMedicalRecordForm();
                    }}
                  >
                    <DialogContent className="sm:max-w-lg">
                      <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                          <ClipboardList className="h-5 w-5" />
                          Nhập bệnh án trước khi hoàn thành
                        </DialogTitle>
                        <DialogDescription>
                          Lịch hẹn chỉ được đánh dấu hoàn thành sau khi bệnh án của {appt.patientName} được lưu lại.
                        </DialogDescription>
                      </DialogHeader>

                      {appt.status === 'COMPLETED' && createMedicalRecordMutation.isError && (
                        <div className="rounded-lg border border-warning/30 bg-warning/10 p-3 text-xs text-warning">
                          Lịch hẹn đã được đánh dấu hoàn thành, nhưng lưu bệnh án chưa thành công.
                          Vui lòng kiểm tra lại thông tin và bấm lưu lại.
                        </div>
                      )}

                      <div className="space-y-3 py-2">
                        <div>
                          <label className="mb-1 block text-sm font-medium">
                            Chẩn đoán <span className="text-destructive">*</span>
                          </label>
                          <Input
                            placeholder="VD: Viêm họng cấp"
                            value={diagnosis}
                            onChange={(e) => setDiagnosis(e.target.value)}
                          />
                        </div>

                        <div>
                          <label className="mb-1 block text-sm font-medium">Triệu chứng</label>
                          <Textarea
                            placeholder="Mô tả triệu chứng của bệnh nhân..."
                            value={symptoms}
                            onChange={(e) => setSymptoms(e.target.value)}
                            rows={3}
                          />
                        </div>

                        <div>
                          <label className="mb-1 block text-sm font-medium">Đơn thuốc</label>
                          <Textarea
                            placeholder="Liệt kê thuốc và liều dùng..."
                            value={prescription}
                            onChange={(e) => setPrescription(e.target.value)}
                            rows={3}
                          />
                        </div>

                        <div>
                          <label className="mb-1 block text-sm font-medium">Ghi chú thêm</label>
                          <Textarea
                            placeholder="Ghi chú khác (không bắt buộc)..."
                            value={recordNotes}
                            onChange={(e) => setRecordNotes(e.target.value)}
                            rows={2}
                          />
                        </div>
                      </div>

                      <DialogFooter>
                        <Button variant="outline" onClick={() => setCompleteOpen(false)} disabled={isSubmittingCompletion}>
                          Hủy
                        </Button>
                        <Button
                          disabled={!diagnosis.trim() || isSubmittingCompletion}
                          onClick={handleSubmitMedicalRecordAndComplete}
                        >
                          {isSubmittingCompletion ? 'Đang lưu...' : 'Lưu bệnh án & Hoàn thành'}
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </PageContainer>
  );
}

function InfoRow({ icon: Icon, label, value, mono }: { icon: any; label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-start gap-3">
      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
        <Icon className="h-4 w-4" />
      </div>
      <div>
        <p className="text-xs uppercase text-muted-foreground">{label}</p>
        <p className={cn('text-sm font-medium', mono && 'font-mono')}>{value}</p>
      </div>
    </div>
  );
}
