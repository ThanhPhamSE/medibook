'use client';

import * as React from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { bookingSchema, type BookingInput } from '@/schemas';
import { useDoctor, useTimeSlots, useCreateAppointment } from '@/hooks/use-api';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Label } from '@/components/ui/label';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/skeletons';
import { CheckCircle2, Clock, Calendar as CalIcon, ArrowRight, ArrowLeft, Video, MapPin, Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';
import { formatCurrency, formatDate, formatTime, initials } from '@/utils/format';
import { toast } from 'sonner';
import type { Doctor } from '@/types';

interface Slot { startTime: string; endTime: string; available: boolean }

const steps = ['Date & Time', 'Details', 'Review'] as const;

export default function BookAppointmentPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { data: doc, isLoading } = useDoctor(id);
  const [step, setStep] = React.useState(0);
  const [date, setDate] = React.useState('');
  const [slot, setSlot] = React.useState('');
  const { data: slots, isLoading: slotsLoading } = useTimeSlots(id, date);
  const createMutation = useCreateAppointment();

  const docData: Doctor | undefined = doc;
  const slotList: Slot[] = slots ?? [];

  const form = useForm<BookingInput>({
    resolver: zodResolver(bookingSchema),
    defaultValues: { doctorId: id, date: '', startTime: '', type: 'IN_PERSON', reason: '', notes: '' },
  });

  const watchType = form.watch('type');

  const next = async () => {
    if (step === 0) {
      if (!date) return toast.error('Please pick a date');
      if (!slot) return toast.error('Please choose a time slot');
      form.setValue('date', date);
      form.setValue('startTime', slot);
      setStep(1);
    } else if (step === 1) {
      const valid = await form.trigger(['type']);
      if (valid) setStep(2);
    }
  };

  const confirm = async (values: BookingInput) => {
    try {
      const created = await createMutation.mutateAsync(values);
      router.push(`/appointments/${created.id}?booked=1`);
    } catch { /* handled */ }
  };

  if (isLoading) return <PageContainer><Skeleton className="h-96 rounded-xl" /></PageContainer>;

  return (
    <PageContainer>
      <PageHeader
        breadcrumbs={[{ label: 'Doctors', href: '/doctors' }, { label: `${docData?.fullName}`, href: `/doctors/${id}` }, { label: 'Book' }]}
        title="Book an appointment"
        description={`With ${docData?.fullName} · ${docData?.specialtyName}`}
      />

      <div className="flex items-center gap-2">
        {steps.map((s, i) => (
          <React.Fragment key={s}>
            <div className={cn('flex items-center gap-2 rounded-full border px-3 py-1.5 text-sm font-medium transition-colors', i <= step ? 'border-primary bg-primary/10 text-primary' : 'text-muted-foreground')}>
              <span className={cn('flex h-5 w-5 items-center justify-center rounded-full text-xs', i < step ? 'bg-primary text-primary-foreground' : i === step ? 'bg-primary/20' : 'bg-muted')}>
                {i < step ? <CheckCircle2 className="h-3.5 w-3.5" /> : i + 1}
              </span>
              {s}
            </div>
            {i < steps.length - 1 && <div className="h-px w-6 bg-border sm:w-12" />}
          </React.Fragment>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardContent className="p-6">
            {step === 0 && (
              <div className="space-y-5">
                <div>
                  <Label className="mb-2 block">Choose a date</Label>
                  <Input type="date" min={new Date().toISOString().split('T')[0]} value={date} onChange={(e) => { setDate(e.target.value); setSlot(''); }} />
                </div>
                {date && (
                  <div>
                    <p className="mb-2 text-sm font-medium">Available time slots {formatDate(date, 'EEEE, MMM d')}</p>
                    {slotsLoading ? (
                      <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">{Array.from({ length: 8 }).map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
                    ) : (
                      <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">
                        {slotList.map((s: Slot, index: number) => (
                          <button
                            key={`${s.startTime}-${index}`}
                            disabled={!s.available}
                            onClick={() => setSlot(s.startTime)}
                            className={cn('rounded-lg border px-3 py-2 text-sm font-medium transition-all', !s.available && 'cursor-not-allowed border-dashed bg-muted/40 text-muted-foreground/50 line-through', s.available && slot === s.startTime && 'border-primary bg-primary/10 text-primary', s.available && slot !== s.startTime && 'hover:border-primary hover:bg-primary/5')}
                          >
                            {formatTime(s.startTime)}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                )}
                <div className="flex justify-end">
                  <Button onClick={next} disabled={!date || !slot}>Continue <ArrowRight className="ml-2 h-4 w-4" /></Button>
                </div>
              </div>
            )}

            {step === 1 && (
              <Form {...form}>
                <form onSubmit={form.handleSubmit(() => setStep(2))} className="space-y-5">
                  <FormField control={form.control} name="type" render={({ field }) => (
                    <FormItem>
                      <FormLabel>Visit type</FormLabel>
                      <FormControl>
                        <RadioGroup onValueChange={field.onChange} value={field.value} className="grid gap-3 sm:grid-cols-2">
                          <label className={cn('flex cursor-pointer items-center gap-3 rounded-lg border p-4 transition-all', field.value === 'IN_PERSON' && 'border-primary bg-primary/5')}>
                            <RadioGroupItem value="IN_PERSON" />
                            <MapPin className="h-5 w-5 text-primary" />
                            <div><p className="text-sm font-medium">In-person visit</p><p className="text-xs text-muted-foreground">At the clinic</p></div>
                          </label>
                          {/* <label className={cn('flex cursor-pointer items-center gap-3 rounded-lg border p-4 transition-all', field.value === 'VIDEO' && 'border-primary bg-primary/5')}>
                            <RadioGroupItem value="VIDEO" />
                            <Video className="h-5 w-5 text-primary" />
                            <div><p className="text-sm font-medium">Video consultation</p><p className="text-xs text-muted-foreground">From anywhere</p></div>
                          </label> */}
                        </RadioGroup>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )} />
                  {/* <FormField control={form.control} name="reason" render={({ field }) => (<FormItem><FormLabel>Reason for visit</FormLabel><FormControl><Textarea rows={3} placeholder="Briefly describe your symptoms or reason..." {...field} /></FormControl><FormMessage /></FormItem>)} /> */}
                  <FormField control={form.control} name="notes" render={({ field }) => (<FormItem><FormLabel>Notes <span className="text-muted-foreground">(optional)</span></FormLabel><FormControl><Textarea rows={2} placeholder="Anything else the doctor should know?" {...field} /></FormControl><FormMessage /></FormItem>)} />
                  <div className="flex justify-between">
                    <Button type="button" variant="outline" onClick={() => setStep(0)}><ArrowLeft className="mr-2 h-4 w-4" /> Back</Button>
                    <Button type="submit">Review <ArrowRight className="ml-2 h-4 w-4" /></Button>
                  </div>
                </form>
              </Form>
            )}

            {step === 2 && (
              <div className="space-y-5">
                <div className="rounded-lg border p-4">
                  <h3 className="text-sm font-semibold">Review your booking</h3>
                  <p className="mt-3 text-xs uppercase text-muted-foreground">Doctor</p>
                  <p className="text-sm font-medium">{docData?.fullName} · {docData?.specialtyName}</p>
                  <div className="mt-3 grid grid-cols-2 gap-3 text-sm">
                    <div><p className="text-xs uppercase text-muted-foreground">Date</p><p className="font-medium">{formatDate(date, 'EEEE, MMM d')}</p></div>
                    <div><p className="text-xs uppercase text-muted-foreground">Time</p><p className="font-medium">{slot}</p></div>
                    <div><p className="text-xs uppercase text-muted-foreground">Type</p><p className="font-medium">{watchType === 'VIDEO' ? 'Video consultation' : 'In-person visit'}</p></div>
                    <div><p className="text-xs uppercase text-muted-foreground">Fee</p><p className="font-medium text-primary">{formatCurrency(docData?.consultationFee ?? 0)}</p></div>
                  </div>
                  {form.watch('reason') && (<div className="mt-3 border-t pt-3"><p className="text-xs uppercase text-muted-foreground">Reason</p><p className="text-sm">{form.watch('reason')}</p></div>)}
                </div>
                <div className="flex justify-between">
                  <Button variant="outline" onClick={() => setStep(1)}><ArrowLeft className="mr-2 h-4 w-4" /> Back</Button>
                  <Button onClick={form.handleSubmit(confirm)} disabled={createMutation.isPending}>
                    {createMutation.isPending ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Confirming...</> : <><CheckCircle2 className="mr-2 h-4 w-4" /> Confirm booking</>}
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="h-fit lg:sticky lg:top-20">
          <CardHeader><CardTitle className="text-base">Summary</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <Avatar className="h-12 w-12 border bg-primary/10"><AvatarFallback className="bg-transparent text-sm font-semibold text-primary">{initials(docData?.firstName ?? '', docData?.lastName ?? '')}</AvatarFallback></Avatar>
              <div><p className="text-sm font-semibold">{docData?.fullName}</p><p className="text-xs text-muted-foreground">{docData?.specialtyName}</p></div>
            </div>
            <div className="space-y-2 border-t pt-4 text-sm">
              <p className="flex items-center gap-2 text-muted-foreground"><CalIcon className="h-4 w-4 text-primary" /> {date ? formatDate(date, 'EEE, MMM d') : 'Pick a date'}</p>
              <p className="flex items-center gap-2 text-muted-foreground"><Clock className="h-4 w-4 text-primary" /> {slot || 'Pick a time'}</p>
              <p className="flex items-center gap-2 text-muted-foreground">{watchType === 'VIDEO' ? <Video className="h-4 w-4 text-primary" /> : <MapPin className="h-4 w-4 text-primary" />} {watchType === 'VIDEO' ? 'Video consultation' : 'In-person visit'}</p>
            </div>
            <div className="flex items-center justify-between border-t pt-4">
              <span className="text-sm text-muted-foreground">Fee</span>
              <span className="text-lg font-semibold text-primary">{formatCurrency(docData?.consultationFee ?? 0)}</span>
            </div>
            <Badge variant="success" className="w-full justify-center gap-1.5"><span className="h-1.5 w-1.5 rounded-full bg-success" /> Free cancellation up to 2h before</Badge>
          </CardContent>
        </Card>
      </div>
    </PageContainer>
  );
}
