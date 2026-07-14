'use client';

import { useCurrentDoctorId } from '@/hooks/use-current-doctor-id';
import { useDoctorMedicalRecords } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Button } from '@/components/ui/button';
import type { ColumnDef } from '@tanstack/react-table';
import type { MedicalRecord, Doctor } from '@/types';
import { formatDate } from '@/utils/format';
import { Eye, Plus } from 'lucide-react';
import Link from 'next/link';

function DoctorMedicalRecords() {
  const doctorId = useCurrentDoctorId();
  const { data, isLoading } = useDoctorMedicalRecords({ doctorId, size: 100 });
  const columns: ColumnDef<MedicalRecord>[] = [
    { accessorKey: 'bookingCode', header: 'Mã lịch hẹn', cell: ({ row }) => <span className="font-mono text-xs">{row.original.bookingCode || '-'}</span> },
    { accessorKey: 'patientName', header: 'Bệnh nhân', cell: ({ row }) => <span className="font-medium">{row.original.patientName}</span> },
    { accessorKey: 'doctorName', header: 'Bác sĩ' },
    { accessorKey: 'diagnosis', header: 'Chẩn đoán', cell: ({ row }) => <span className="font-medium">{row.original.diagnosis}</span> },
    { accessorKey: 'prescription', header: 'Đơn thuốc', cell: ({ row }) => <span className="line-clamp-1 max-w-xs text-muted-foreground">{row.original.prescription || '-'}</span> },
    { accessorKey: 'note', header: 'Ghi chú', cell: ({ row }) => <span className="line-clamp-1 max-w-xs text-muted-foreground">{row.original.note || '-'}</span> },
    { accessorKey: 'createdAt', header: 'Ngày tạo', cell: ({ row }) => formatDate(row.original.createdAt) },
    { id: 'actions', cell: ({ row }) => <Button asChild size="sm" variant="ghost"><Link href={`/medical-records/${row.original.id}`}><Eye className="h-4 w-4 mr-2" /> Xem</Link></Button> },
  ];
  return (
    <PageContainer>
      <PageHeader 
        title="Medical Records" 
        description="Patient medical records you have created." 
        breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Medical Records' }]}
        actions={<Button asChild><Link href="/doctor/medical-records/new"><Plus className="mr-2 h-4 w-4" /> New Record</Link></Button>}
      />
      <DataTable columns={columns} data={data?.content ?? []} loading={isLoading} searchKey="diagnosis" searchPlaceholder="Search records..." exportName="doctor-medical-records.csv" />
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['DOCTOR']}><DoctorMedicalRecords /></RoleGuard>;
}
