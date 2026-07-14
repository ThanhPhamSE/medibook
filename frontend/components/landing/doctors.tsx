'use client';

import Link from 'next/link';
import { Star, ArrowRight } from 'lucide-react';
import { useDoctors } from '@/hooks/use-api';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { initials, formatCurrency } from '@/utils/format';
import { CardSkeleton } from '@/components/skeletons';

export function LandingDoctors() {
  const { data, isLoading } = useDoctors({ size: 4 });

  return (
    <section id="doctors" className="py-20 sm:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-end">
          <div className="max-w-xl">
            <p className="text-sm font-semibold uppercase tracking-wider text-primary">Our Doctors</p>
            <h2 className="mt-2 text-3xl font-semibold tracking-tight text-balance sm:text-4xl">
              Meet our trusted specialists
            </h2>
            <p className="mt-4 text-muted-foreground">
              Board-certified doctors with years of experience and a passion for patient care.
            </p>
          </div>
          <Button asChild variant="outline">
            <Link href="/doctors">
              All doctors <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
        </div>

        <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {isLoading
            ? Array.from({ length: 4 }).map((_, i) => <CardSkeleton key={i} />)
            : (data?.content ?? []).map((doc: any) => (
                <Link
                  key={doc.id}
                  href={`/doctors/${doc.id}`}
                  className="group rounded-2xl border bg-card p-6 text-center shadow-card transition-all hover:-translate-y-1 hover:shadow-soft"
                >
                  <Avatar className="mx-auto h-20 w-20 border-2 bg-gradient-to-br from-primary/15 to-accent/15">
                    <AvatarFallback className="bg-transparent text-lg font-semibold text-primary">
                      {initials(doc.firstName, doc.lastName)}
                    </AvatarFallback>
                  </Avatar>
                  <h3 className="mt-4 text-base font-semibold">
                    {doc.fullName}
                  </h3>
                  <p className="mt-1 text-xs text-muted-foreground">{doc.specialtyName}</p>
                  <div className="mt-3 flex items-center justify-center gap-1 text-sm">
                    <Star className="h-4 w-4 fill-warning text-warning" />
                    <span className="font-medium">{doc.rating.toFixed(1)}</span>
                    <span className="text-muted-foreground">({doc.reviewCount})</span>
                  </div>
                  <p className="mt-3 text-sm font-semibold text-primary">{formatCurrency(doc.consultationFee)}</p>
                </Link>
              ))}
        </div>
      </div>
    </section>
  );
}
