'use client';

import * as React from 'react';
import Link from 'next/link';
import { useMedicalRecords } from '@/hooks/use-api';
import { useAuth } from '@/contexts/auth-context';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import type { ColumnDef } from '@tanstack/react-table';
import type { MedicalRecord } from '@/types';
import { formatDate } from '@/utils/format';
import { FileText, Eye, Plus } from 'lucide-react';
import { EmptyState } from '@/components/empty-state';

function MedicalRecordsPage() {
  const { user } = useAuth();
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');
  const params: Record<string, unknown> = { page, size: 10, search };
  if (user?.roleName === 'CUSTOMER') params.patientId = user.id;
  const { data, isLoading } = useMedicalRecords(params);

  const columns: ColumnDef<MedicalRecord>[] = [
    { accessorKey: 'bookingCode', header: 'Mã lịch hẹn', cell: ({ row }) => <span className="font-mono text-xs">{row.original.bookingCode || '-'}</span> },
    { accessorKey: 'patientName', header: 'Bệnh nhân' },
    { accessorKey: 'doctorName', header: 'Bác sĩ' },
    { accessorKey: 'diagnosis', header: 'Chẩn đoán', cell: ({ row }) => <span className="font-medium">{row.original.diagnosis}</span> },
    // { accessorKey: 'symptoms', header: 'Triệu chứng', cell: ({ row }) => <span className="line-clamp-1 max-w-xs text-muted-foreground">{row.original.symptoms}</span> },
    { accessorKey: 'prescription', header: 'Đơn thuốc', cell: ({ row }) => <span className="line-clamp-1 max-w-xs text-muted-foreground">{row.original.prescription || '-'}</span> },
    { accessorKey: 'note', header: 'Ghi chú', cell: ({ row }) => <span className="line-clamp-1 max-w-xs text-muted-foreground">{row.original.note || '-'}</span> },
    { accessorKey: 'createdAt', header: 'Ngày tạo', cell: ({ row }) => formatDate(row.original.createdAt || row.original.date) },
    {
      id: 'actions',
      cell: ({ row }) => (
        <Button asChild size="sm" variant="ghost"><Link href={`/medical-records/${row.original.id}`}><Eye className="h-4 w-4" /> Xem</Link></Button>
      ),
    },
  ];

  return (
    <PageContainer>
      <PageHeader
        title="Bệnh án"
        description="Lịch sử y tế hoàn chỉnh của bạn."
        breadcrumbs={[{ label: 'Trang chủ', href: '/dashboard' }, { label: 'Bệnh án' }]}
        actions={user?.roleName === 'DOCTOR' && <Button asChild><Plus className="mr-2 h-4 w-4" /> Tạo bệnh án</Button>}
      />
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
        searchKey="diagnosis" 
        searchPlaceholder="Tìm kiếm chẩn đoán..." 
        exportName="benh-an.csv"
        emptyTitle="Không có bệnh án"
        emptyDescription="Bệnh án sẽ hiển thị sau khi hoàn thành lịch khám."
      />
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['CUSTOMER', 'DOCTOR']}><MedicalRecordsPage /></RoleGuard>;
}
