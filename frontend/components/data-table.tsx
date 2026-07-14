'use client';

import * as React from 'react';
import {
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
  type ColumnDef,
  type SortingState,
  type ColumnFiltersState,
} from '@tanstack/react-table';
import { ArrowUpDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, Download, Search } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Skeleton } from '@/components/skeletons';
import { EmptyState } from '@/components/empty-state';
import { exportToCsv } from '@/utils/format';
import { cn } from '@/lib/utils';

interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  loading?: boolean;
  searchKey?: string;
  searchPlaceholder?: string;
  exportName?: string;
  emptyTitle?: string;
  emptyDescription?: string;
  toolbar?: React.ReactNode;
  pageSize?: number;
  // Server-side pagination
  serverSide?: boolean;
  totalPages?: number;
  totalElements?: number;
  currentPage?: number;
  onPageChange?: (page: number) => void;
  onSearchChange?: (search: string) => void;
  currentSearch?: string;
}

export function DataTable<TData, TValue>({
  columns,
  data,
  loading,
  searchKey,
  searchPlaceholder = 'Search...',
  exportName,
  emptyTitle = 'No records found',
  emptyDescription = 'Try adjusting your search or filters.',
  toolbar,
  pageSize = 10,
  serverSide = false,
  totalPages,
  totalElements,
  currentPage = 0,
  onPageChange,
  onSearchChange,
  currentSearch = '',
}: DataTableProps<TData, TValue>) {
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [globalFilter, setGlobalFilter] = React.useState(currentSearch);

  React.useEffect(() => {
    setGlobalFilter(currentSearch);
  }, [currentSearch]);

  const table = useReactTable({
    data,
    columns,
    state: { sorting, columnFilters, globalFilter },
    initialState: { pagination: { pageSize, pageIndex: serverSide ? currentPage : 0 } },
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    onGlobalFilterChange: (value) => {
      setGlobalFilter(value);
      if (onSearchChange) onSearchChange(String(value));
    },
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: serverSide ? undefined : getPaginationRowModel(),
    manualPagination: serverSide,
  });

  const handleExport = () => {
    const rows = table.getFilteredRowModel().rows.map((r) => r.original as unknown as Record<string, unknown>);
    exportToCsv(exportName ?? 'export.csv', rows);
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-1 items-center gap-2">
          {searchKey && (
            <div className="relative w-full max-w-xs">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder={searchPlaceholder}
                value={globalFilter}
                onChange={(e) => {
                  setGlobalFilter(e.target.value);
                  if (onSearchChange) onSearchChange(e.target.value);
                }}
                className="pl-9"
              />
            </div>
          )}
        </div>
        <div className="flex items-center gap-2">
          {toolbar}
          {exportName && (
            <Button variant="outline" size="sm" onClick={handleExport}>
              <Download className="mr-2 h-4 w-4" />
              Export
            </Button>
          )}
        </div>
      </div>

      <div className="rounded-xl border bg-card shadow-card">
        <Table>
          <TableHeader>
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id} className="bg-muted/40 hover:bg-muted/40">
                {headerGroup.headers.map((header) => (
                  <TableHead key={header.id} className="h-11 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    {header.isPlaceholder ? null : (
                      <div
                        className={cn('flex items-center gap-1', header.column.getCanSort() && 'cursor-pointer select-none')}
                        onClick={header.column.getToggleSortingHandler()}
                      >
                        {flexRender(header.column.columnDef.header, header.getContext())}
                        {header.column.getCanSort() && <ArrowUpDown className="h-3 w-3 opacity-50" />}
                      </div>
                    )}
                  </TableHead>
                ))}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {loading ? (
              Array.from({ length: 6 }).map((_, i) => (
                <TableRow key={i}>
                  {columns.map((_c, j) => (
                    <TableCell key={j}>
                      <Skeleton className="h-4 w-full max-w-[120px]" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : table.getRowModel().rows.length ? (
              table.getRowModel().rows.map((row) => (
                <TableRow key={row.id} data-state={row.getIsSelected() && 'selected'}>
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id} className="text-sm">
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={columns.length} className="h-48 p-0">
                  <EmptyState title={emptyTitle} description={emptyDescription} />
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-muted-foreground">
          {serverSide 
            ? `${totalElements ?? 0} total record(s)` 
            : `${table.getFilteredRowModel().rows.length} of ${data.length} record(s)`
          }
        </p>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="icon" onClick={() => serverSide && onPageChange ? onPageChange(0) : table.setPageIndex(0)} disabled={serverSide ? currentPage === 0 : !table.getCanPreviousPage()} aria-label="First page">
            <ChevronsLeft className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="icon" onClick={() => serverSide && onPageChange ? onPageChange(currentPage - 1) : table.previousPage()} disabled={serverSide ? currentPage === 0 : !table.getCanPreviousPage()} aria-label="Previous page">
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {serverSide ? currentPage + 1 : table.getState().pagination.pageIndex + 1} of {serverSide ? totalPages ?? 1 : table.getPageCount() || 1}
          </span>
          <Button variant="outline" size="icon" onClick={() => serverSide && onPageChange ? onPageChange(currentPage + 1) : table.nextPage()} disabled={serverSide ? currentPage >= (totalPages ?? 0) - 1 : !table.getCanNextPage()} aria-label="Next page">
            <ChevronRight className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="icon" onClick={() => serverSide && onPageChange ? onPageChange((totalPages ?? 1) - 1) : table.setPageIndex(table.getPageCount() - 1)} disabled={serverSide ? currentPage >= (totalPages ?? 0) - 1 : !table.getCanNextPage()} aria-label="Last page">
            <ChevronsRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
