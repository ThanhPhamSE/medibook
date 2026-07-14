'use client';

import * as React from 'react';
import { useAdminBookings } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import type { ColumnDef } from '@tanstack/react-table';
import type { Appointment } from '@/types';
import { ArrowRight, Video, MapPin } from 'lucide-react';
import { formatDate, formatTime } from '@/utils/format';

function AdminBookings() {
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');
  const { data, isLoading } = useAdminBookings({ page, size: 10, search });

  const columns: ColumnDef<Appointment>[] = [
    {
      accessorKey: 'date',
      header: 'Ngày',
      cell: ({ row }) => <div className="text-sm">{formatDate(row.original.date, 'MMM d, yyyy')}</div>,
    },
    {
      accessorKey: 'time',
      header: 'Giờ',
      cell: ({ row }) => <div className="text-sm">{formatTime(row.original.date)}</div>,
    },
    {
      accessorKey: 'patientName',
      header: 'Bệnh nhân',
      cell: ({ row }) => <div className="text-sm font-medium">{row.original.patientName}</div>,
    },
    {
      accessorKey: 'doctorName',
      header: 'Bác sĩ',
      cell: ({ row }) => <div className="text-sm font-medium">{row.original.doctorName}</div>,
    },
    {
      accessorKey: 'specialtyName',
      header: 'Phí khám',
      cell: ({ row }) => <div className="text-sm text-muted-foreground">{row.original.consultationFee}$</div>,
    },
    {
      accessorKey: 'type',
      header: 'Loại',
      cell: ({ row }) => (
        <div className="flex items-center gap-1 text-sm">
          {row.original.type === 'VIDEO' ? <Video className="h-4 w-4 text-primary" /> : <MapPin className="h-4 w-4 text-primary" />}
          <span>{row.original.type === 'VIDEO' ? 'Video' : 'Trực tiếp'}</span>
        </div>
      ),
    },
    {
      accessorKey: 'status',
      header: 'Trạng thái',
      cell: ({ row }) => {
        const statusColors: Record<string, string> = {
          PENDING: 'bg-yellow-100 text-yellow-800',
          CONFIRMED: 'bg-green-100 text-green-800',
          COMPLETED: 'bg-blue-100 text-blue-800',
          CANCELLED: 'bg-red-100 text-red-800',
          NO_SHOW: 'bg-gray-100 text-gray-800',
        };
        return <Badge className={statusColors[row.original.status] || 'bg-gray-100 text-gray-800'}>{row.original.status}</Badge>;
      },
    },
    {
      id: 'actions',
      cell: ({ row }) => (
        <Button asChild variant="ghost" size="sm">
          <a href={`/appointments/${row.original.id}`}>
            Xem <ArrowRight className="ml-2 h-4 w-4" />
          </a>
        </Button>
      ),
    },
  ];

  return (
    <PageContainer>
      <PageHeader title="Lịch hẹn" description="Tất cả lịch hẹn trong hệ thống." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Lịch hẹn' }]} />
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        loading={isLoading}
        serverSide
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        currentPage={page}
        onPageChange={setPage}
        onSearchChange={setSearch}
        currentSearch={search}
        searchKey="patientName"
        searchPlaceholder="Tìm kiếm bệnh nhân hoặc bác sĩ..."
        emptyTitle="Không tìm thấy lịch hẹn"
        emptyDescription="Thử tìm kiếm với từ khóa khác hoặc bộ lọc khác."
        exportName="lich-hen.csv"
      />
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><AdminBookings /></RoleGuard>;
}
