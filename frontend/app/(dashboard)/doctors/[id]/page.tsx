'use client';

import * as React from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { Star, GraduationCap, Languages, Clock, ArrowLeft, Calendar, MessageSquare } from 'lucide-react';
import { useDoctor, useDoctorReviews } from '@/hooks/use-api';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/skeletons';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { initials, formatCurrency, formatDate } from '@/utils/format';
import { EmptyState } from '@/components/empty-state';
import type { Doctor, Review } from '@/types';

export default function DoctorDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { data: docData, isLoading } = useDoctor(id);
  const { data: reviewsData } = useDoctorReviews(id);

  const doc: Doctor | undefined = docData;
  const reviews: Review[] = Array.isArray(reviewsData) ? reviewsData : (reviewsData as any)?.content ?? (reviewsData as any)?.items ?? [];

  if (isLoading) return <PageContainer><Skeleton className="h-96 w-full rounded-xl" /></PageContainer>;
  if (!doc) return <PageContainer><EmptyState title="Doctor not found" description="This doctor may no longer be available." action={<Button asChild><Link href="/doctors">Browse doctors</Link></Button>} /></PageContainer>;

  return (
    <PageContainer>
      <PageHeader
        breadcrumbs={[{ label: 'Home', href: '/dashboard' }, { label: 'Doctors', href: '/doctors' }, { label: `${doc.fullName}` }]}
        actions={<Button variant="outline" onClick={() => router.back()}><ArrowLeft className="mr-2 h-4 w-4" /> Back</Button>}
        title={`${doc.fullName}`}
        description={doc.specialtyName}
      />

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardContent className="p-6">
            <div className="flex flex-col gap-6 sm:flex-row">
              <Avatar className="h-24 w-24 border-2 bg-gradient-to-br from-primary/15 to-accent/15"><AvatarFallback className="bg-transparent text-2xl font-semibold text-primary">{initials(doc.firstName, doc.lastName)}</AvatarFallback></Avatar>
              <div className="flex-1">
                <div className="flex flex-wrap items-center gap-3">
                  <h2 className="text-xl font-semibold">{doc.fullName}</h2>
                  <Badge variant="success" className="gap-1"><span className="h-1.5 w-1.5 rounded-full bg-success" /> Available</Badge>
                </div>
                <p className="mt-1 text-sm text-muted-foreground">{doc.specialtyName}</p>
                <div className="mt-3 flex items-center gap-3">
                  <div className="flex items-center gap-1"><Star className="h-4 w-4 fill-warning text-warning" /><span className="text-sm font-medium">{doc.rating?.toFixed(1) ?? 'N/A'}</span><span className="text-sm text-muted-foreground">({doc.reviewCount ?? 0} reviews)</span></div>
                  <span className="text-muted-foreground">·</span>
                  <span className="text-sm text-muted-foreground">{doc.yearsOfExperience ?? 0} years experience</span>
                </div>
                <div className="mt-4 flex flex-wrap gap-1.5">
                  {(doc.specializations ?? []).map((s: string) => <Badge key={s} variant="secondary" className="font-normal">{s}</Badge>)}
                </div>
              </div>
            </div>

            <Tabs defaultValue="about" className="mt-6">
              <TabsList>
                <TabsTrigger value="about">About</TabsTrigger>
                <TabsTrigger value="education">Education</TabsTrigger>
                <TabsTrigger value="reviews">Reviews ({reviews.length})</TabsTrigger>
              </TabsList>
              <TabsContent value="about" className="mt-4 space-y-4">
                <p className="text-sm leading-relaxed text-muted-foreground">{doc.bio}</p>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="rounded-lg border p-4"><p className="flex items-center gap-2 text-xs font-medium uppercase text-muted-foreground"><Languages className="h-4 w-4" /> Languages</p><p className="mt-1 text-sm">{doc.languages.join(', ')}</p></div>
                  <div className="rounded-lg border p-4"><p className="flex items-center gap-2 text-xs font-medium uppercase text-muted-foreground"><Clock className="h-4 w-4" /> Experience</p><p className="mt-1 text-sm">{doc.yearsOfExperience} years</p></div>
                </div>
              </TabsContent>
              <TabsContent value="education" className="mt-4">
                <ul className="space-y-3">
                  {doc.education.map((e: string, i: number) => (
                    <li key={i} className="flex items-start gap-3 rounded-lg border p-4"><div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10 text-primary"><GraduationCap className="h-5 w-5" /></div><p className="text-sm">{e}</p></li>
                  ))}
                </ul>
              </TabsContent>
              <TabsContent value="reviews" className="mt-4">
                {reviews.length === 0 ? (
                  <EmptyState icon={<MessageSquare className="h-7 w-7" />} title="No reviews yet" description="Be the first to review this doctor." />
                ) : (
                  <div className="space-y-3">
                    {reviews.map((r: Review) => (
                      <div key={r.id} className="rounded-lg border p-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <Avatar className="h-8 w-8 bg-muted"><AvatarFallback className="bg-transparent text-xs font-semibold">{initials(r.patientName.split(' ')[0], r.patientName.split(' ')[1] || '')}</AvatarFallback></Avatar>
                            <div><p className="text-sm font-medium">{r.patientName}</p><p className="text-xs text-muted-foreground">{formatDate(r.createdAt ?? (r as any).date)}</p></div>
                          </div>
                          <div className="flex gap-0.5">{Array.from({ length: 5 }).map((_, i: number) => (<Star key={i} className={`h-3.5 w-3.5 ${i < r.rating ? 'fill-warning text-warning' : 'text-muted-foreground/30'}`} />))}</div>
                        </div>
                        <p className="mt-3 text-sm text-muted-foreground">{r.comment}</p>
                      </div>
                    ))}
                  </div>
                )}
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        <div className="space-y-4">
          <Card className="sticky top-20 shadow-soft">
            <CardHeader><CardTitle className="text-base">Book a visit</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between rounded-lg bg-muted/50 p-3"><span className="text-sm text-muted-foreground">Consultation fee</span><span className="text-xl font-semibold text-primary">{formatCurrency(doc.consultationFee)}</span></div>
              <Button asChild className="w-full" size="lg"><Link href={`/doctors/${doc.id}/book`}><Calendar className="mr-2 h-4 w-4" /> Book appointment</Link></Button>
              <p className="text-center text-xs text-muted-foreground">Instant confirmation · Free cancellation up to 2h before</p>
            </CardContent>
          </Card>
        </div>
      </div>
    </PageContainer>
  );
}
