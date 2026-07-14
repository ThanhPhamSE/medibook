'use client';

import * as React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { specialtySchema, type SpecialtyInput } from '@/schemas';
import { useSpecialtiesPaged, useSaveSpecialty, useDeleteSpecialty } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog';
import { SPECIALTY_ICONS, DEFAULT_SPECIALTY_ICON } from '@/constants/medical';
import type { ColumnDef } from '@tanstack/react-table';
import type { Specialty } from '@/types';
import { Plus, Pencil, Trash2, Loader2, Stethoscope } from 'lucide-react';

function AdminSpecialties() {
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');
  const { data, isLoading } = useSpecialtiesPaged({ page, size: 10, search });
  const saveMut = useSaveSpecialty();
  const deleteMut = useDeleteSpecialty();
  const [open, setOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<Specialty | null>(null);

  const form = useForm<SpecialtyInput>({
    resolver: zodResolver(specialtySchema),
    values: editing ? { name: editing.name, description: editing.description, icon: editing.icon || '', color: editing.color || '' } : { name: '', description: '', icon: '', color: '#14b8a6' },
  });

  const onSubmit = (values: SpecialtyInput) => {
    saveMut.mutate({ id: editing?.id, input: values }, { onSuccess: () => setOpen(false) });
  };

  const columns: ColumnDef<Specialty>[] = [
    {
      accessorKey: 'name',
      header: 'Chuyên khoa',
      cell: ({ row }) => {
        const Icon = SPECIALTY_ICONS[row.original.name] ?? DEFAULT_SPECIALTY_ICON;
        return (
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg" style={{ backgroundColor: `${row.original.color}1a`, color: row.original.color }}><Icon className="h-5 w-5" /></div>
            <span className="font-medium">{row.original.name}</span>
          </div>
        );
      },
    },
    { accessorKey: 'description', header: 'Description', cell: ({ row }) => <span className="line-clamp-1 max-w-md text-muted-foreground">{row.original.description}</span> },
    { accessorKey: 'doctorCount', header: 'Doctors', cell: ({ row }) => <Badge variant="secondary">{row.original.doctorCount}</Badge> },
    {
      id: 'actions',
      cell: ({ row }) => (
        <div className="flex gap-1">
          <Button size="icon" variant="ghost" onClick={() => { setEditing(row.original); setOpen(true); }}><Pencil className="h-4 w-4" /></Button>
          <AlertDialog>
            <AlertDialogTrigger asChild><Button size="icon" variant="ghost" className="text-destructive"><Trash2 className="h-4 w-4" /></Button></AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader><AlertDialogTitle>Delete specialty?</AlertDialogTitle><AlertDialogDescription>This will remove {row.original.name} and reassign its doctors.</AlertDialogDescription></AlertDialogHeader>
              <AlertDialogFooter><AlertDialogCancel>Cancel</AlertDialogCancel><AlertDialogAction onClick={() => deleteMut.mutate(row.original.id)}>Delete</AlertDialogAction></AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        </div>
      ),
    },
  ];

  return (
    <PageContainer>
      <PageHeader title="Chuyên khoa" description="Quản lý các chuyên khoa y tế." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Chuyên khoa' }]}
        actions={<Button onClick={() => { setEditing(null); form.reset({ name: '', description: '', icon: '', color: '#14b8a6' }); setOpen(true); }}><Plus className="mr-2 h-4 w-4" /> Thêm chuyên khoa</Button>} />
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
        searchPlaceholder="Tìm kiếm chuyên khoa..." 
        exportName="chuyen-khoa.csv"
        emptyTitle="Không tìm thấy chuyên khoa"
        emptyDescription="Thử tìm kiếm với từ khóa khác."
      />

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editing ? 'Edit specialty' : 'New specialty'}</DialogTitle>
            <DialogDescription>{editing ? 'Update specialty details.' : 'Add a new medical specialty.'}</DialogDescription>
          </DialogHeader>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 py-2">
            <div className="space-y-1.5"><Label>Name</Label><Input {...form.register('name')} />{form.formState.errors.name && <p className="text-xs text-destructive">{form.formState.errors.name.message}</p>}</div>
            <div className="space-y-1.5"><Label>Description</Label><Textarea rows={3} {...form.register('description')} />{form.formState.errors.description && <p className="text-xs text-destructive">{form.formState.errors.description.message}</p>}</div>
            <div className="space-y-1.5"><Label>Color</Label><Input type="color" {...form.register('color')} className="h-10 w-20 p-1" /></div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
              <Button type="submit" disabled={saveMut.isPending}><Loader2 className="mr-2 h-4 w-4 animate-spin" /> {saveMut.isPending ? 'Saving...' : 'Save'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><AdminSpecialties /></RoleGuard>;
}
