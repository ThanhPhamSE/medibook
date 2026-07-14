'use client';

import * as React from 'react';
import Link from 'next/link';
import type { ColumnDef } from '@tanstack/react-table';
import { useDoctors, useDeleteDoctor } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { DoctorFormDialog } from '@/components/admin/doctor-form-dialog';
import { Pencil, Plus, CalendarClock, Star } from 'lucide-react';
import type { Doctor } from '@/types';
import { initials, formatCurrency } from '@/utils/format';

function AdminDoctors() {
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');
  const { data, isLoading } = useDoctors({ page, size: 10, search });
  const deleteMut = useDeleteDoctor();
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<Doctor | null>(null);

  const columns: ColumnDef<Doctor>[] = [
    {
      accessorKey: 'fullName',
      header: 'Doctor',
      cell: ({ row }) => (
        <div className="flex items-center gap-3">
          <Avatar className="h-9 w-9 border bg-primary/10"><AvatarFallback className="bg-transparent text-xs font-semibold text-primary">{row.original.fullName}</AvatarFallback></Avatar>
          <div><p className="text-sm font-medium">{row.original.fullName}</p><p className="text-xs text-muted-foreground">{row.original.email}</p></div>
        </div>
      ),
    },
    { accessorKey: 'specialtyName', header: 'Chuyên khoa' },
    {
      accessorKey: 'rating',
      header: 'Rating',
      cell: ({ row }) => <span className="inline-flex items-center gap-1 text-sm"><Star className="h-3.5 w-3.5 fill-warning text-warning" />{row.original.rating.toFixed(1)}</span>,
    },
    { accessorKey: 'yearsOfExperience', header: 'Exp (yrs)' },
    { accessorKey: 'consultationFee', header: 'Fee', cell: ({ row }) => formatCurrency(row.original.consultationFee) },
    { accessorKey: 'status', header: 'Status', cell: ({ row }) => <Badge variant={row.original.status === 'ACTIVE' ? 'success' : 'secondary'}>{row.original.status}</Badge> },
    {
      id: 'actions',
      cell: ({ row }) => (
        <div className="flex gap-1">
          <Button size="icon" variant="ghost" title="Lịch làm việc" asChild>
            <Link href={`/admin/doctors/${row.original.id}/working-pattern`}>
              <CalendarClock className="h-4 w-4" />
            </Link>
          </Button>
          <Button size="icon" variant="ghost" onClick={() => { setEditing(row.original); setDialogOpen(true); }}><Pencil className="h-4 w-4" /></Button>
        </div>
      ),
    },
  ];

  return (
    <PageContainer>
      <PageHeader title="Bác sĩ" description="Quản lý tất cả bác sĩ trong hệ thống." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Bác sĩ' }]}
        actions={<Button onClick={() => { setEditing(null); setDialogOpen(true); }}><Plus className="mr-2 h-4 w-4" /> Thêm bác sĩ</Button>} />
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
        searchKey="name" 
        searchPlaceholder="Tìm kiếm bác sĩ..." 
        exportName="bac-si.csv"
        emptyTitle="Không tìm thấy bác sĩ"
        emptyDescription="Thử tìm kiếm với từ khóa khác."
      />
      <DoctorFormDialog open={dialogOpen} onOpenChange={setDialogOpen} doctor={editing} />
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><AdminDoctors /></RoleGuard>;
}
