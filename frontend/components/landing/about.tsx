'use client';

import { Award, Target, Eye, Heart } from 'lucide-react';

const pillars = [
  { icon: Target, title: 'Our Mission', text: 'To make quality healthcare accessible to everyone, everywhere, through technology that puts patients first.' },
  { icon: Eye, title: 'Our Vision', text: 'A world where booking a doctor is as simple as a tap, and your medical history is always at your fingertips.' },
  { icon: Heart, title: 'Our Values', text: 'Compassion, integrity, and innovation guide every decision we make for patients and doctors alike.' },
  { icon: Award, title: 'Our Standards', text: 'We hold ourselves to the highest clinical and security standards — HIPAA-compliant and doctor-approved.' },
];

export function LandingAbout() {
  return (
    <section id="about" className="bg-muted/30 py-20 sm:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid gap-12 lg:grid-cols-2 lg:items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wider text-primary">About Us</p>
            <h2 className="mt-2 text-3xl font-semibold tracking-tight text-balance sm:text-4xl">
              Reimagining healthcare for the digital age
            </h2>
            <p className="mt-4 text-muted-foreground text-balance">
              MediBook was founded with a simple belief: healthcare should be effortless. We connect patients with verified doctors, digitize medical records, and make every visit — in-person or online — smoother than ever.
            </p>
            <div className="mt-8 grid gap-5 sm:grid-cols-2">
              {pillars.map((p) => (
                <div key={p.title} className="rounded-2xl border bg-card p-5 shadow-card">
                  <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
                    <p.icon className="h-5 w-5" />
                  </div>
                  <h3 className="mt-3 text-sm font-semibold">{p.title}</h3>
                  <p className="mt-1.5 text-xs text-muted-foreground">{p.text}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="relative">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-4">
                <div className="rounded-2xl border bg-card p-6 shadow-card">
                  <p className="text-4xl font-semibold text-primary">12K+</p>
                  <p className="mt-1 text-sm text-muted-foreground">Active patients</p>
                </div>
                <div className="rounded-2xl border bg-gradient-to-br from-primary to-accent p-6 text-primary-foreground shadow-glow">
                  <p className="text-4xl font-semibold">98K+</p>
                  <p className="mt-1 text-sm text-primary-foreground/90">Appointments</p>
                </div>
              </div>
              <div className="space-y-4 pt-10">
                <div className="rounded-2xl border bg-card p-6 shadow-card">
                  <p className="text-4xl font-semibold text-accent">56+</p>
                  <p className="mt-1 text-sm text-muted-foreground">Specialists</p>
                </div>
                <div className="rounded-2xl border bg-card p-6 shadow-card">
                  <p className="text-4xl font-semibold text-success">4.9★</p>
                  <p className="mt-1 text-sm text-muted-foreground">Avg. rating</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
