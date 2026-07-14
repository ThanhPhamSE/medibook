'use client';

import { useAuditLogs } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Badge } from '@/components/ui/badge';
import type { ColumnDef } from '@tanstack/react-table';
import { formatDate, formatDateTime } from '@/utils/format';
import { ScrollText, User, Activity, Globe } from 'lucide-react';

type Log = { id: string; actor: string; action: string; target: string; ip: string; timestamp: string };

function AuditLogs() {
  const { data, isLoading } = useAuditLogs({ size: 100 });
  const columns: ColumnDef<Log>[] = [
    { accessorKey: 'timestamp', header: 'Time', cell: ({ row }) => <span className="text-sm">{formatDateTime(row.original.timestamp)}</span> },
    { accessorKey: 'actor', header: 'Actor', cell: ({ row }) => <span className="inline-flex items-center gap-1.5 font-medium"><User className="h-3.5 w-3.5 text-muted-foreground" />{row.original.actor}</span> },
    { accessorKey: 'action', header: 'Action', cell: ({ row }) => <Badge variant="info">{row.original.action}</Badge> },
    { accessorKey: 'target', header: 'Target', cell: ({ row }) => <span className="font-mono text-xs">{row.original.target}</span> },
    { accessorKey: 'ip', header: 'IP', cell: ({ row }) => <span className="inline-flex items-center gap-1.5 text-xs text-muted-foreground"><Globe className="h-3 w-3" />{row.original.ip}</span> },
  ];
  return (
    <PageContainer>
      <PageHeader title="Audit Logs" description="Track all administrative and system actions." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Audit Logs' }]} />
      <DataTable columns={columns} data={data?.content ?? []} loading={isLoading} searchKey="actor" searchPlaceholder="Search by actor..." exportName="audit-logs.csv" />
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><AuditLogs /></RoleGuard>;
}
