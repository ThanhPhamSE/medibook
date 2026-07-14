'use client';

import { Star, Quote } from 'lucide-react';

const testimonials = [
  { name: 'Emily Roberts', role: 'Patient', rating: 5, text: 'Booking an appointment was effortless. The doctor was thorough and the video consultation felt just like an in-person visit.' },
  { name: 'Marcus Lee', role: 'Patient', rating: 5, text: 'MediBook saved me hours of waiting. I got a same-day cardiology appointment and my prescriptions were ready instantly.' },
  { name: 'Dr. Aisha Khan', role: 'Dermatologist', rating: 5, text: 'As a doctor, the dashboard lets me manage my schedule and patient records in one place. It is exactly what modern practice needs.' },
  { name: 'Sophia Martinez', role: 'Patient', rating: 4, text: 'I love that all my medical records are in one place. No more carrying paper files between clinics.' },
];

export function LandingTestimonials() {
  return (
    <section className="bg-muted/30 py-20 sm:py-28">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-sm font-semibold uppercase tracking-wider text-primary">Testimonials</p>
          <h2 className="mt-2 text-3xl font-semibold tracking-tight text-balance sm:text-4xl">
            Loved by patients and doctors alike
          </h2>
        </div>

        <div className="mt-14 grid gap-6 sm:grid-cols-2">
          {testimonials.map((t) => (
            <figure key={t.name} className="relative rounded-2xl border bg-card p-6 shadow-card">
              <Quote className="absolute right-6 top-6 h-8 w-8 text-primary/15" />
              <div className="flex gap-1">
                {Array.from({ length: 5 }).map((_, i) => (
                  <Star
                    key={i}
                    className={`h-4 w-4 ${i < t.rating ? 'fill-warning text-warning' : 'text-muted-foreground/30'}`}
                  />
                ))}
              </div>
              <blockquote className="mt-4 text-sm leading-relaxed text-muted-foreground">{t.text}</blockquote>
              <figcaption className="mt-5 flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-primary/15 to-accent/15 text-sm font-semibold text-primary">
                  {t.name.split(' ').map((n) => n[0]).join('')}
                </div>
                <div>
                  <p className="text-sm font-semibold">{t.name}</p>
                  <p className="text-xs text-muted-foreground">{t.role}</p>
                </div>
              </figcaption>
            </figure>
          ))}
        </div>
      </div>
    </section>
  );
}
