'use client';

import Link from 'next/link';
import { ArrowRight } from 'lucide-react';
import { useSpecialties } from '@/hooks/use-api';
import { SPECIALTY_ICONS, DEFAULT_SPECIALTY_ICON } from '@/constants/medical';
import { CardSkeleton } from '@/components/skeletons';

export function LandingSpecialties() {
  const { data, isLoading } = useSpecialties();

  return (
    <section id="speiclaties" className="bg-muted/30 py-20 sm:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-end">
          <div className="max-w-xl">
            <p className="text-sm font-semibold uppercase tracking-wider text-primary">Speiclaties</p>
            <h2 className="mt-2 text-3xl font-semibold tracking-tight text-balance sm:text-4xl">
              Care across every specialty
            </h2>
            <p className="mt-4 text-muted-foreground">
              Our network of certified specialists spans the full spectrum of medical care.
            </p>
          </div>
          <Link href="/doctors" className="group inline-flex items-center gap-1 text-sm font-medium text-primary">
            View all doctors
            <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
          </Link>
        </div>

        <div className="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {isLoading
            ? Array.from({ length: 8 }).map((_, i) => <CardSkeleton key={i} />)
            : (data?.content ?? []).map((d: any) => {
                const Icon = SPECIALTY_ICONS[d.name] ?? DEFAULT_SPECIALTY_ICON;
                return (
                  <Link
                    key={d.id}
                    href={`/doctors?specialty=${d.id}`}
                    className="group rounded-2xl border bg-card p-6 shadow-card transition-all hover:-translate-y-1 hover:shadow-soft"
                  >
                    <div
                      className="flex h-12 w-12 items-center justify-center rounded-xl"
                      style={{ backgroundColor: `${d.color}1a`, color: d.color }}
                    >
                      <Icon className="h-6 w-6" />
                    </div>
                    <h3 className="mt-4 text-base font-semibold">{d.name}</h3>
                    <p className="mt-1.5 line-clamp-2 text-sm text-muted-foreground">{d.description}</p>
                    <p className="mt-3 text-xs font-medium text-primary">{d.doctorCount} doctors</p>
                  </Link>
                );
              })}
        </div>
      </div>
    </section>
  );
}
