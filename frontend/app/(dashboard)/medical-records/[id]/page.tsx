'use client';

import { useParams } from 'next/navigation';
import Link from 'next/link';
import { useMedicalRecord } from '@/hooks/use-api';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/skeletons';
import { ArrowLeft, FileText, Pill, Stethoscope, Calendar, CalendarClock, User } from 'lucide-react';
import { formatDate } from '@/utils/format';
import { EmptyState } from '@/components/empty-state';

export default function MedicalRecordDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: record, isLoading } = useMedicalRecord(id);

  if (isLoading) return <PageContainer><Skeleton className="h-96 rounded-xl" /></PageContainer>;
  if (!record) return <PageContainer><EmptyState title="Record not found" action={<Button asChild><Link href="/medical-records">Back</Link></Button>} /></PageContainer>;

  return (
    <PageContainer>
      <PageHeader
        breadcrumbs={[{ label: 'Home', href: '/dashboard' }, { label: 'Medical Records', href: '/medical-records' }, { label: record.diagnosis }]}
        title={record.diagnosis}
        description={`Diagnosed on ${formatDate(record.createdAt)}`}
        actions={<Button variant="outline" onClick={() => history.back()}><ArrowLeft className="mr-2 h-4 w-4" /> Back</Button>}
      />
      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader><CardTitle className="text-base">Thông tin lâm sàng</CardTitle></CardHeader>
          <CardContent className="space-y-5">
            <Section icon={Stethoscope} title="Chẩn đoán"><p className="text-sm text-muted-foreground">{record.diagnosis}</p></Section>
            <Section icon={Pill} title="Đơn thuốc"><p className="text-sm text-muted-foreground">{record.prescription || 'Không có đơn thuốc.'}</p></Section>
            {record.note && <Section icon={FileText} title="Ghi chú"><p className="text-sm text-muted-foreground">{record.note}</p></Section>}
          </CardContent>
        </Card>
        <Card className="h-fit">
          <CardHeader><CardTitle className="text-base">Thông tin khám</CardTitle></CardHeader>
          <CardContent className="space-y-4 text-sm">
            <Row label="Mã lịch hẹn" value={record.bookingCode || '-'} />
            <Row label="Bệnh nhân" value={record.patientName} />
            <Row label="Bác sĩ" value={record.doctorName} />
            <Row label="Ngày tạo" value={formatDate(record.createdAt, 'EEEE, MMM d, yyyy')} />
          </CardContent>
        </Card>
      </div>
    </PageContainer>
  );
}

function Section({ icon: Icon, title, children }: any) {
  return (
    <div className="rounded-lg border p-4">
      <p className="flex items-center gap-2 text-xs font-semibold uppercase text-muted-foreground"><Icon className="h-4 w-4 text-primary" /> {title}</p>
      <div className="mt-2">{children}</div>
    </div>
  );
}
function Row({ label, value }: { label: string; value: string }) {
  return <div className="flex items-center justify-between border-b pb-2 last:border-0"><span className="text-muted-foreground">{label}</span><span className="font-medium">{value}</span></div>;
}
