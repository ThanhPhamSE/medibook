'use client';

import { useAllMedicalRecords } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Button } from '@/components/ui/button';
import type { ColumnDef } from '@tanstack/react-table';
import type { MedicalRecord } from '@/types';
import { formatDate } from '@/utils/format';
import { Eye } from 'lucide-react';
import Link from 'next/link';

function AdminMedicalRecords() {
  const { data, isLoading } = useAllMedicalRecords({ size: 100 });
  const columns: ColumnDef<MedicalRecord>[] = [
    { accessorKey: 'patientName', header: 'Patient', cell: ({ row }) => <span className="font-medium">{row.original.patientName}</span> },
    { accessorKey: 'doctorName', header: 'Doctor' },
    { accessorKey: 'diagnosis', header: 'Diagnosis' },
    { accessorKey: 'date', header: 'Date', cell: ({ row }) => formatDate(row.original.createdAt) },
    { id: 'actions', cell: ({ row }) => <Button asChild size="sm" variant="ghost"><Link href={`/medical-records/${row.original.id}`}><Eye className="h-4 w-4" /> View</Link></Button> },
  ];
  return (
    <PageContainer>
      <PageHeader title="Medical Records" description="All medical records in the system." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Medical Records' }]} />
      <DataTable columns={columns} data={data?.content ?? []} loading={isLoading} searchKey="diagnosis" searchPlaceholder="Search records..." exportName="admin-medical-records.csv" />
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><AdminMedicalRecords /></RoleGuard>;
}
