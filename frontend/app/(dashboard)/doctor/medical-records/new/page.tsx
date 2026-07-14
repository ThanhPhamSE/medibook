'use client';

import * as React from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { medicalRecordSchema, type MedicalRecordInput } from '@/schemas';
import { useAuth } from '@/contexts/auth-context';
import { useCurrentDoctorId } from '@/hooks/use-current-doctor-id';
import { useCreateMedicalRecord, useAppointments } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Loader2, Save } from 'lucide-react';
import type { Appointment } from '@/types';

interface PatientOption { id: string; name: string }

function CreateMedicalRecord() {
  const { user } = useAuth();
  const router = useRouter();
  const createMut = useCreateMedicalRecord();
  const doctorId = useCurrentDoctorId();
  const { data: appts } = useAppointments({ userId: doctorId, role: 'DOCTOR', status: 'COMPLETED', size: 50 });
  const apptList: Appointment[] = React.useMemo(() => appts?.content ?? [], [appts?.content]);

  const patients: PatientOption[] = React.useMemo(() => {
    const map = new Map<string, string>();
    apptList.forEach((a: Appointment) => map.set(a.patientId, a.patientName));
    return Array.from(map.entries()).map(([id, name]) => ({ id, name }));
  }, [apptList]);

  const form = useForm<MedicalRecordInput>({
    resolver: zodResolver(medicalRecordSchema),
    defaultValues: { patientId: '', diagnosis: '', symptoms: '', prescription: '', notes: '' },
  });

  const onSubmit = (values: MedicalRecordInput) => {
    const appt = apptList.find((a: Appointment) => a.patientId === values.patientId);
    createMut.mutate(
      { ...values, doctorId, doctorName: `Dr. ${user?.fullName}`, patientName: appt?.patientName, appointmentId: appt?.id },
      { onSuccess: () => router.push('/medical-records') }
    );
  };

  return (
    <PageContainer>
      <PageHeader title="Create Medical Record" description="Record a diagnosis, prescription, and notes for a patient." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Medical Records', href: '/medical-records' }, { label: 'New' }]} />
      <Card className="max-w-2xl">
        <CardHeader><CardTitle className="text-base">Record details</CardTitle></CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField control={form.control} name="patientId" render={({ field }) => (
                <FormItem>
                  <FormLabel>Patient</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl><SelectTrigger><SelectValue placeholder="Select patient" /></SelectTrigger></FormControl>
                    <SelectContent>{patients.map((p: PatientOption) => <SelectItem key={p.id} value={p.id}>{p.name}</SelectItem>)}</SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )} />
              <FormField control={form.control} name="diagnosis" render={({ field }) => (<FormItem><FormLabel>Diagnosis</FormLabel><FormControl><Input placeholder="e.g. Hypertension" {...field} /></FormControl><FormMessage /></FormItem>)} />
              <FormField control={form.control} name="symptoms" render={({ field }) => (<FormItem><FormLabel>Symptoms</FormLabel><FormControl><Textarea rows={2} placeholder="Reported symptoms" {...field} /></FormControl><FormMessage /></FormItem>)} />
              <FormField control={form.control} name="prescription" render={({ field }) => (<FormItem><FormLabel>Prescription</FormLabel><FormControl><Textarea rows={3} placeholder="Medication and dosage" {...field} /></FormControl><FormMessage /></FormItem>)} />
              <FormField control={form.control} name="notes" render={({ field }) => (<FormItem><FormLabel>Notes <span className="text-muted-foreground">(optional)</span></FormLabel><FormControl><Textarea rows={2} {...field} /></FormControl><FormMessage /></FormItem>)} />
              <Button type="submit" disabled={createMut.isPending}><Save className="mr-2 h-4 w-4" /> {createMut.isPending ? 'Saving...' : 'Save record'}</Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['DOCTOR']}><CreateMedicalRecord /></RoleGuard>;
}
