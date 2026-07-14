'use client';

import * as React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { doctorSchema, type DoctorInput } from '@/schemas';
import { useSpecialties, useSaveDoctor } from '@/hooks/use-api';
import { useUsers } from '@/hooks/use-api';
import type { Doctor } from '@/types';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';

interface Props {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  doctor?: Doctor | null;
}

export function DoctorFormDialog({ open, onOpenChange, doctor }: Props) {
  const { data: specialties } = useSpecialties();
  const { data: users } = useUsers({ page: 0, size: 100, role: 'CUSTOMER' });
  const saveMut = useSaveDoctor();

  const form = useForm<DoctorInput>({
    resolver: zodResolver(doctorSchema),
    defaultValues: {
      specialtyId: '',
      degree: '',
      experienceYears: 0,
      consultationFee: 0,
      biography: '',
    },
  });

  React.useEffect(() => {
    if (doctor) {
      form.reset({
        specialtyId: doctor.specialtyId,
        degree: doctor.degree || doctor.education?.[0] || '',
        experienceYears: doctor.yearsOfExperience,
        consultationFee: doctor.consultationFee,
        biography: doctor.biography || doctor.bio || '',
      });
    } else {
      form.reset({
        specialtyId: '',
        degree: '',
        experienceYears: 0,
        consultationFee: 0,
        biography: '',
      });
    }
  }, [doctor, form]);

  const onSubmit = (values: DoctorInput) => {
    saveMut.mutate(
      { id: doctor?.id, input: values },
      { onSuccess: () => onOpenChange(false) }
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{doctor ? 'Edit doctor' : 'Add new doctor'}</DialogTitle>
          <DialogDescription>{doctor ? 'Update doctor profile and credentials.' : 'Add a new doctor to the system.'}</DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 py-2">
          
          {!doctor && (
            <FormField label="User" error={form.formState.errors.userId?.message}>
              <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm" {...form.register('userId')}>
                <option value="">Select user</option>
                {(users?.content ?? []).map((u) => <option key={u.id} value={u.id}>{u.fullName} ({u.email})</option>)}
              </select>
            </FormField>
          )}

          <FormField label="Chuyên khoa" error={form.formState.errors.specialtyId?.message}>
            <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm" {...form.register('specialtyId')}>
              <option value="">Select specialty</option>
              {(specialties?.content ?? []).map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </FormField>

          <FormField label="Degree" error={form.formState.errors.degree?.message}>
            <Input {...form.register('degree')} placeholder="e.g. Doctor of Dental Surgery" />
          </FormField>

          <div className="grid gap-4 sm:grid-cols-2">
            <FormField label="Years of experience" error={form.formState.errors.experienceYears?.message}>
              <Input type="number" min={0} {...form.register('experienceYears')} />
            </FormField>
            <FormField label="Consultation fee" error={form.formState.errors.consultationFee?.message}>
              <Input type="number" min={0.01} step={0.01} {...form.register('consultationFee')} />
            </FormField>
          </div>

          <FormField label="Biography" error={form.formState.errors.biography?.message}>
            <Textarea rows={4} {...form.register('biography')} placeholder="Professional biography..." />
          </FormField>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
            <Button type="submit" disabled={saveMut.isPending}><Loader2 className="mr-2 h-4 w-4 animate-spin" /> {saveMut.isPending ? 'Saving...' : 'Save doctor'}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function FormField({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1.5">
      <Label>{label}</Label>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  );
}
