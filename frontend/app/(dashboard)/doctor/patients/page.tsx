'use client';

import * as React from 'react';
import { useCurrentDoctorId } from '@/hooks/use-current-doctor-id';
import { useAppointments } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import type { ColumnDef } from '@tanstack/react-table';
import type { Appointment } from '@/types';
import { formatDate, initials, formatCurrency } from '@/utils/format';

function DoctorPatients() {
  const doctorId = useCurrentDoctorId();
  const { data, isLoading } = useAppointments({ userId: doctorId, role: 'DOCTOR', size: 100 });
  const appts: Appointment[] = data?.content ?? [];

  const columns: ColumnDef<Appointment>[] = [
    {
      accessorKey: 'bookingCode',
      header: 'Mã lịch hẹn',
      cell: ({ row }) => <span className="font-mono text-xs">{row.original.bookingCode || '-'}</span>,
    },
    {
      accessorKey: 'patientName',
      header: 'Bệnh nhân',
      cell: ({ row }) => (
        <div className="flex items-center gap-3">
          <Avatar className="h-9 w-9 border bg-accent/10"><AvatarFallback className="bg-transparent text-xs font-semibold text-accent">{initials(row.original.patientName?.split(' ')[0], row.original.patientName?.split(' ')[1] || '')}</AvatarFallback></Avatar>
          <span className="font-medium">{row.original.patientName}</span>
        </div>
      ),
    },
    { accessorKey: 'doctorName', header: 'Bác sĩ' },
    {
      accessorKey: 'startDatetime',
      header: 'Thời gian',
      cell: ({ row }) => `${formatDate(row.original.startDatetime, 'dd/MM/yyyy')} ${formatDate(row.original.startDatetime, 'HH:mm')}`,
    },
    {
      accessorKey: 'status',
      header: 'Trạng thái',
      cell: ({ row }) => {
        const status = row.original.status;
        const statusColors: Record<string, string> = {
          PENDING: 'bg-yellow-100 text-yellow-800',
          CONFIRMED: 'bg-blue-100 text-blue-800',
          COMPLETED: 'bg-green-100 text-green-800',
          CANCELLED: 'bg-red-100 text-red-800',
        };
        return <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusColors[status] || 'bg-gray-100 text-gray-800'}`}>{status}</span>;
      },
    },
    {
      accessorKey: 'consultationFee',
      header: 'Phí khám',
      cell: ({ row }) => formatCurrency(row.original.consultationFee),
    },
    {
      accessorKey: 'note',
      header: 'Ghi chú',
      cell: ({ row }) => <span className="line-clamp-1 max-w-xs text-muted-foreground">{row.original.note || '-'}</span>,
    },
  ];

  return (
    <PageContainer>
      <PageHeader title="Danh sách bệnh nhân" description="Các cuộc hẹn khám bệnh của bạn." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Bệnh nhân' }]} />
      <DataTable columns={columns} data={appts} loading={isLoading} searchKey="patientName" searchPlaceholder="Tìm kiếm bệnh nhân..." exportName="my-patients.csv" />
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['DOCTOR']}><DoctorPatients /></RoleGuard>;
}
