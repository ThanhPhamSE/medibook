'use client';

import { Activity, Brain, CalendarClock, HeartPulse, ShieldCheck, Video } from 'lucide-react';

const services = [
  { icon: CalendarClock, title: 'Easy Appointment Booking', description: 'Schedule visits with top specialists in under 60 seconds, anytime.' },
  { icon: Video, title: 'Video Consultations', description: 'Connect with doctors from the comfort of your home via secure HD video.' },
  { icon: HeartPulse, title: 'Medical Records', description: 'Access prescriptions, diagnoses, and history in one secure place.' },
  { icon: Brain, title: 'Specialist Care', description: '30+ specialties — from cardiology to neurology and mental health.' },
  { icon: Activity, title: 'Real-time Tracking', description: 'Track appointment status and get reminders so you never miss a visit.' },
  { icon: ShieldCheck, title: 'Secure & Private', description: 'Bank-grade encryption keeps your health data confidential at all times.' },
];

export function LandingServices() {
  return (
    <section id="services" className="py-20 sm:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-sm font-semibold uppercase tracking-wider text-primary">Our Services</p>
          <h2 className="mt-2 text-3xl font-semibold tracking-tight text-balance sm:text-4xl">
            Everything you need for better health
          </h2>
          <p className="mt-4 text-muted-foreground text-balance">
            From booking to follow-ups, MediBook brings the entire care experience online — simple, secure, and built around you.
          </p>
        </div>

        <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {services.map((s) => (
            <div
              key={s.title}
              className="group relative overflow-hidden rounded-2xl border bg-card p-6 shadow-card transition-all hover:-translate-y-1 hover:shadow-soft"
            >
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
                <s.icon className="h-6 w-6" />
              </div>
              <h3 className="mt-5 text-lg font-semibold">{s.title}</h3>
              <p className="mt-2 text-sm text-muted-foreground">{s.description}</p>
              <div className="pointer-events-none absolute -right-8 -top-8 h-24 w-24 rounded-full bg-gradient-to-br from-primary/5 to-accent/5 opacity-0 transition-opacity group-hover:opacity-100" />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
