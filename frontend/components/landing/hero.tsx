'use client';

import Link from 'next/link';
import { ArrowRight, Calendar, ShieldCheck, Stethoscope, Star } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { APP_CONFIG } from '@/constants/app';

const stats = [
  { value: '56+', label: 'Verified Doctors' },
  { value: '12K+', label: 'Happy Patients' },
  { value: '30+', label: 'Specialties' },
  { value: '4.9', label: 'Average Rating' },
];

export function LandingHero() {
  return (
    <section className="relative overflow-hidden pt-32 pb-20 sm:pt-40 sm:pb-28">
      <div className="absolute inset-0 -z-10 bg-gradient-hero" />
      <div className="absolute inset-0 -z-10 bg-dots opacity-40" />
      <div className="absolute -right-32 top-20 -z-10 h-96 w-96 rounded-full bg-primary/20 blur-3xl" />
      <div className="absolute -left-32 top-40 -z-10 h-96 w-96 rounded-full bg-accent/20 blur-3xl" />

      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid items-center gap-12 lg:grid-cols-2">
          <div className="animate-slide-up">
            <div className="inline-flex items-center gap-2 rounded-full border bg-background/60 px-3 py-1 text-xs font-medium text-muted-foreground backdrop-blur">
              <span className="flex h-2 w-2 rounded-full bg-success" />
              Trusted by 12,000+ patients worldwide
            </div>
            <h1 className="mt-6 text-4xl font-semibold tracking-tight text-balance sm:text-5xl lg:text-6xl">
              Your health, <span className="gradient-text">one tap away.</span>
            </h1>
            <p className="mt-6 max-w-xl text-lg text-muted-foreground text-balance">
              {APP_CONFIG.description} Book appointments with verified specialists, access your medical records, and consult from anywhere.
            </p>
            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <Button asChild size="lg" className="h-12 px-6 text-base shadow-glow">
                <Link href="/register">
                  Book an Appointment
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
              <Button asChild size="lg" variant="outline" className="h-12 px-6 text-base">
                <Link href="/doctors">Find a Doctor</Link>
              </Button>
            </div>

            <dl className="mt-12 grid grid-cols-2 gap-6 sm:grid-cols-4">
              {stats.map((s) => (
                <div key={s.label}>
                  <dt className="text-2xl font-semibold tracking-tight sm:text-3xl">{s.value}</dt>
                  <dd className="mt-1 text-xs text-muted-foreground sm:text-sm">{s.label}</dd>
                </div>
              ))}
            </dl>
          </div>

          <div className="relative animate-scale-in">
            <div className="relative rounded-3xl border bg-card p-6 shadow-soft">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10 text-primary">
                    <Stethoscope className="h-6 w-6" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold">Dr. Sarah Chen</p>
                    <p className="text-xs text-muted-foreground">Cardiology</p>
                  </div>
                </div>
                <div className="flex items-center gap-1 rounded-full bg-warning/10 px-2 py-1 text-xs font-medium text-warning">
                  <Star className="h-3 w-3 fill-current" /> 4.9
                </div>
              </div>
              <div className="mt-5 grid grid-cols-3 gap-2">
                {['09:00', '10:00', '11:00', '14:00', '15:00', '16:00'].map((t, i) => (
                  <div
                    key={t}
                    className={`rounded-lg border px-2 py-2 text-center text-xs font-medium ${
                      i === 2 ? 'border-primary bg-primary/10 text-primary' : 'text-muted-foreground'
                    }`}
                  >
                    {t}
                  </div>
                ))}
              </div>
              <div className="mt-5 flex items-center justify-between rounded-xl bg-muted/50 p-3">
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <Calendar className="h-4 w-4 text-primary" />
                  Tomorrow, 10:00 AM
                </div>
                <span className="rounded-full bg-success/10 px-2 py-0.5 text-[11px] font-medium text-success">Available</span>
              </div>
              <Button className="mt-4 w-full">Confirm Booking</Button>
            </div>

            <div className="absolute -left-6 -bottom-6 hidden rounded-2xl border bg-card p-4 shadow-soft sm:block">
              <div className="flex items-center gap-2">
                <ShieldCheck className="h-5 w-5 text-success" />
                <div>
                  <p className="text-xs font-semibold">HIPAA Compliant</p>
                  <p className="text-[11px] text-muted-foreground">Your data is secure</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
