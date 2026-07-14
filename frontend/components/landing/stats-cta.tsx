'use client';

import Link from 'next/link';
import { ArrowRight, Calendar, Users, Stethoscope, Activity } from 'lucide-react';
import { Button } from '@/components/ui/button';

const stats = [
  { icon: Users, value: '12,000+', label: 'Patients served' },
  { icon: Stethoscope, value: '56+', label: 'Expert doctors' },
  { icon: Calendar, value: '98K+', label: 'Appointments booked' },
  { icon: Activity, value: '99.9%', label: 'Uptime' },
];

export function LandingStats() {
  return (
    <section className="py-20 sm:py-24">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {stats.map((s) => (
            <div key={s.label} className="rounded-2xl border bg-card p-6 text-center shadow-card">
              <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <s.icon className="h-6 w-6" />
              </div>
              <p className="mt-4 text-3xl font-semibold tracking-tight">{s.value}</p>
              <p className="mt-1 text-sm text-muted-foreground">{s.label}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export function LandingCta() {
  return (
    <section className="py-20 sm:py-24">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-primary to-accent px-6 py-16 text-center text-primary-foreground shadow-glow sm:px-16">
          <div className="absolute inset-0 bg-dots opacity-20" />
          <div className="relative">
            <h2 className="text-3xl font-semibold tracking-tight text-balance sm:text-4xl">
              Ready to take charge of your health?
            </h2>
            <p className="mx-auto mt-4 max-w-xl text-primary-foreground/90 text-balance">
              Join thousands of patients who book smarter, faster, and safer with MediBook.
            </p>
            <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
              <Button asChild size="lg" variant="secondary" className="h-12 px-6 text-base">
                <Link href="/register">
                  Get started free <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
              <Button asChild size="lg" variant="outline" className="h-12 border-primary-foreground/30 bg-transparent px-6 text-base text-primary-foreground hover:bg-primary-foreground/10 hover:text-primary-foreground">
                <Link href="/doctors">Browse doctors</Link>
              </Button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
