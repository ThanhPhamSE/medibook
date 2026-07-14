'use client';

import * as React from 'react';
import { useAppointments } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import type { ColumnDef } from '@tanstack/react-table';
import type { Appointment } from '@/types';
import { ArrowRight } from 'lucide-react';
import { formatDate, formatTime } from '@/utils/format';

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
  NO_SHOW: 'Không đến',
};

const STATUS_COLORS: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-blue-100 text-blue-800',
  CANCELLED: 'bg-red-100 text-red-800',
  NO_SHOW: 'bg-gray-100 text-gray-800',
};

function MyAppointmentsPage() {
  const [status, setStatus] = React.useState<string>('all');
  const [tab, setTab] = React.useState('upcoming');
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');

  const handleTabChange = (val: string) => {
    setTab(val);
    setPage(0);
  };

  const handleStatusChange = (val: string) => {
    setStatus(val);
    setPage(0);
  };

  const params: Record<string, unknown> = {
    page,
    size: 10,
    timeFilter: tab, // upcoming | past | all
  };
  if (status !== 'all') params.status = status;
  if (search) params.search = search;

  const { data, isLoading } = useAppointments(params);

  const content: Appointment[] = data?.content ?? [];
  const totalPages: number = data?.totalPages ?? 0;
  const totalElements: number = data?.totalElements ?? 0;

  const columns: ColumnDef<Appointment>[] = [
    {
      accessorKey: 'bookingCode',
      header: 'Mã đặt lịch',
      cell: ({ row }) => (
        <span className="font-mono text-xs font-medium text-muted-foreground">
          {row.original.bookingCode}
        </span>
      ),
    },
    {
      accessorKey: 'doctorName',
      header: 'Bác sĩ',
      cell: ({ row }) => <div className="text-sm font-medium">{row.original.doctorName}</div>,
    },
    {
      accessorKey: 'startDatetime',
      header: 'Ngày',
      cell: ({ row }) => (
        <div className="text-sm">{formatDate(row.original.startDatetime, 'dd/MM/yyyy')}</div>
      ),
    },
    {
      id: 'time',
      header: 'Giờ',
      cell: ({ row }) => (
        <div className="text-sm">
          {`${formatTime(row.original.startDatetime)} — ${formatTime(row.original.endDatetime)}`}
        </div>
      ),
    },
    {
      accessorKey: 'status',
      header: 'Trạng thái',
      cell: ({ row }) => (
        <Badge className={STATUS_COLORS[row.original.status] ?? 'bg-gray-100 text-gray-800'}>
          {STATUS_LABELS[row.original.status] ?? row.original.status}
        </Badge>
      ),
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
      <PageHeader
        title="Lịch hẹn của tôi"
        description="Quản lý các lịch hẹn đã đặt."
        breadcrumbs={[{ label: 'Trang chủ', href: '/dashboard' }, { label: 'Lịch hẹn' }]}
      />

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <Tabs value={tab} onValueChange={handleTabChange}>
          <TabsList>
            <TabsTrigger value="upcoming">Sắp tới</TabsTrigger>
            <TabsTrigger value="past">Đã qua</TabsTrigger>
            <TabsTrigger value="all">Tất cả</TabsTrigger>
          </TabsList>
        </Tabs>

        <Select value={status} onValueChange={handleStatusChange}>
          <SelectTrigger className="w-full sm:w-52">
            <SelectValue placeholder="Lọc theo trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả trạng thái</SelectItem>
            <SelectItem value="PENDING">Chờ xác nhận</SelectItem>
            <SelectItem value="CONFIRMED">Đã xác nhận</SelectItem>
            <SelectItem value="COMPLETED">Hoàn thành</SelectItem>
            <SelectItem value="CANCELLED">Đã hủy</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <DataTable
        columns={columns}
        data={content}
        loading={isLoading}
        serverSide
        totalPages={totalPages}
        totalElements={totalElements}
        currentPage={page}
        onPageChange={setPage}
        onSearchChange={setSearch}
        currentSearch={search}
        searchKey="doctorName"
        searchPlaceholder="Tìm kiếm bác sĩ..."
        emptyTitle="Không tìm thấy lịch hẹn"
        emptyDescription="Thử tìm kiếm với từ khóa khác hoặc bộ lọc khác."
        exportName="lich-hen.csv"
      />
    </PageContainer>
  );
}

export default function Page() {
  return (
    <RoleGuard roles={['CUSTOMER']}>
      <MyAppointmentsPage />
    </RoleGuard>
  );
}