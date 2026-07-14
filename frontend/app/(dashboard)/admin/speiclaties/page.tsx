'use client';

import * as React from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import { useSpecialtiesPaged, useSaveSpecialty, useDeleteSpecialty } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { DataTable } from '@/components/data-table';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Pencil, Plus } from 'lucide-react';
import type { Specialty } from '@/types';
import { toast } from 'sonner';

const specialtySchema = z.object({
  name: z.string().min(1, 'Name is required').max(100, 'Name must not exceed 100 characters'),
  description: z.string().max(1000, 'Description must not exceed 1000 characters'),
});
type SpecialtyInput = z.infer<typeof specialtySchema>;

function AdminSpecialties() {
  const [page, setPage] = React.useState(0);
  const [search, setSearch] = React.useState('');
  const { data, isLoading } = useSpecialtiesPaged({ page, size: 10, search });
  const saveMut = useSaveSpecialty();
  const deleteMut = useDeleteSpecialty();
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<Specialty | null>(null);

  const form = useForm<SpecialtyInput>({
    resolver: zodResolver(specialtySchema),
    defaultValues: { name: '', description: '' },
  });

  const columns: ColumnDef<Specialty>[] = [
    { accessorKey: 'name', header: 'Name' },
    { accessorKey: 'description', header: 'Description' },
    {
      accessorKey: 'doctorCount',
      header: 'Doctors',
      cell: ({ row }) => <Badge variant="outline">{row.original.doctorCount}</Badge>,
    },
    {
      accessorKey: 'createdAt',
      header: 'Created',
      cell: ({ row }) => new Date(row.original.createdAt).toLocaleDateString(),
    },
    {
      id: 'actions',
      cell: ({ row }) => (
        <Button size="icon" variant="ghost" onClick={() => { setEditing(row.original); setDialogOpen(true); form.reset({ name: row.original.name, description: row.original.description || '' }); }}><Pencil className="h-4 w-4" /></Button>
      ),
    },
  ];

  const onSubmit = (values: SpecialtyInput) => {
    saveMut.mutate(
      { id: editing?.id, input: values },
      {
        onSuccess: () => {
          setDialogOpen(false);
          setEditing(null);
          form.reset();
        },
      }
    );
  };

  const handleAdd = () => {
    setEditing(null);
    form.reset({ name: '', description: '' });
    setDialogOpen(true);
  };

  return (
    <PageContainer>
      <PageHeader
        title="Specialties"
        description="Manage medical specialties and departments."
        actions={<Button onClick={handleAdd}><Plus className="mr-2 h-4 w-4" /> Add Specialty</Button>}
      />
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        loading={isLoading}
        serverSide
        currentPage={page}
        totalElements={data?.totalElements ?? 0}
        onPageChange={setPage}
        currentSearch={search}
        onSearchChange={setSearch}
      />

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{editing ? 'Edit' : 'Add'} Specialty</DialogTitle></DialogHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField control={form.control} name="name" render={({ field }) => (
                <FormItem><FormLabel>Name</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
              )} />
              <FormField control={form.control} name="description" render={({ field }) => (
                <FormItem><FormLabel>Description</FormLabel><FormControl><Textarea {...field} /></FormControl><FormMessage /></FormItem>
              )} />
              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>Cancel</Button>
                <Button type="submit" disabled={saveMut.isPending}>{saveMut.isPending ? 'Saving...' : 'Save'}</Button>
              </div>
            </form>
          </Form>
        </DialogContent>
      </Dialog>
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><AdminSpecialties /></RoleGuard>;
}
