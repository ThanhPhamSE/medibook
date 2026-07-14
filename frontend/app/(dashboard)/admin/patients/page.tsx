'use client';

import * as React from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { usePatients, useUpdateUserStatus } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import type { User } from '@/types';
import { initials, formatDate } from '@/utils/format';

function AdminPatients() {
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');
  const { data, isLoading } = usePatients({ page, size: 10, search });
  const statusMut = useUpdateUserStatus();
  const patients = data?.content ?? [];

  const columns: ColumnDef<User>[] = [
    {
      accessorKey: 'fullName',
      header: 'Patient',
      cell: ({ row }) => {
        const nameParts = row.original.fullName?.split(' ') || ['', ''];
        return (
          <div className="flex items-center gap-3">
            <Avatar className="h-9 w-9 border bg-accent/10"><AvatarFallback className="bg-transparent text-xs font-semibold text-accent">{initials(nameParts[0], nameParts[1])}</AvatarFallback></Avatar>
            <div><p className="text-sm font-medium">{row.original.fullName}</p><p className="text-xs text-muted-foreground">{row.original.email}</p></div>
          </div>
        );
      },
    },
    { accessorKey: 'phone', header: 'Phone', cell: ({ row }) => row.original.phone || '—' },
    {
      accessorKey: 'isActive',
      header: 'Status',
      cell: ({ row }) => <Badge variant={row.original.isActive ? 'success' : 'secondary'}>{row.original.isActive ? 'Active' : 'Inactive'}</Badge>,
    },
    {
      id: 'actions',
      cell: ({ row }) => (
        <Select defaultValue={row.original.isActive ? 'true' : 'false'} onValueChange={(v) => statusMut.mutate({ id: String(row.original.id), isActive: v === 'true' })}>
          <SelectTrigger className="h-8 w-32"><SelectValue /></SelectTrigger>
          <SelectContent>
            <SelectItem value="true">Active</SelectItem>
            <SelectItem value="false">Inactive</SelectItem>
          </SelectContent>
        </Select>
      ),
    },
  ];

  return (
    <PageContainer>
      <PageHeader title="Bệnh nhân" description="Xem và quản lý tất cả bệnh nhân đã đăng ký." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Bệnh nhân' }]} />
      <DataTable 
        columns={columns} 
        data={patients} 
        loading={isLoading} 
        serverSide
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        currentPage={page}
        onPageChange={setPage}
        onSearchChange={setSearch}
        currentSearch={search}
        searchKey="name" 
        searchPlaceholder="Tìm kiếm bệnh nhân..." 
        exportName="benh-nhan.csv"
        emptyTitle="Không tìm thấy bệnh nhân"
        emptyDescription="Thử tìm kiếm với từ khóa khác."
      />
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><AdminPatients /></RoleGuard>;
}
