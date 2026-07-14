'use client';

import * as React from 'react';
import Link from 'next/link';
import { type ColumnDef } from '@tanstack/react-table';
import { Calendar, Video, Eye } from 'lucide-react';
import { DataTable } from '@/components/data-table';
import { AppointmentStatusBadge } from '@/components/appointment-status-badge';
import { Button } from '@/components/ui/button';
import type { Appointment } from '@/types';
import { formatDate, formatTime } from '@/utils/format';

interface Props {
  data: Appointment[];
  loading?: boolean;
  showPatient?: boolean;
  showDoctor?: boolean;
  viewHref: (a: Appointment) => string;
  exportName?: string;
  toolbar?: React.ReactNode;
}

export function AppointmentsTable({ data, loading, showPatient, showDoctor, viewHref, exportName, toolbar }: Props) {
  const columns: ColumnDef<Appointment>[] = React.useMemo(
    () => [
      {
        accessorKey: 'bookingCode',
        header: 'Code',
        cell: ({ row }) => <span className="font-mono text-xs font-medium">{row.original.bookingCode}</span>,
      },
      ...(showPatient
        ? [{ accessorKey: 'patientName' as keyof Appointment, header: 'Patient', cell: ({ row }: any) => <span className="font-medium">{row.original.patientName}</span> }]
        : []),
      ...(showDoctor
        ? [{ accessorKey: 'doctorName' as keyof Appointment, header: 'Doctor', cell: ({ row }: any) => <span className="font-medium">{row.original.doctorName}</span> }]
        : []),
      { accessorKey: 'specialtyName', header: 'Chuyên khoa' },
      {
        accessorKey: 'date',
        header: 'Date',
        cell: ({ row }) => (
          <div className="text-sm">
            <p>{formatDate(row.original.date)}</p>
            <p className="text-xs text-muted-foreground">{formatTime(row.original.date)}</p>
          </div>
        ),
      },
      {
        accessorKey: 'type',
        header: 'Type',
        cell: ({ row }) => (
          <span className="inline-flex items-center gap-1 text-xs">
            {row.original.type === 'VIDEO' ? <Video className="h-3 w-3" /> : <Calendar className="h-3 w-3" />}
            {row.original.type === 'VIDEO' ? 'Video' : 'In-person'}
          </span>
        ),
      },
      {
        accessorKey: 'status',
        header: 'Status',
        cell: ({ row }) => <AppointmentStatusBadge status={row.original.status} />,
      },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => (
          <Button asChild size="sm" variant="ghost">
            <Link href={viewHref(row.original)}><Eye className="h-4 w-4" /> View</Link>
          </Button>
        ),
      },
    ],
    [showPatient, showDoctor, viewHref]
  );

  return (
    <DataTable
      columns={columns}
      data={data}
      loading={loading}
      searchKey="bookingCode"
      searchPlaceholder="Search by code..."
      exportName={exportName}
      emptyTitle="No appointments"
      emptyDescription="Bookings will appear here once created."
      toolbar={toolbar}
    />
  );
}
