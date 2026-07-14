'use client';

import Link from 'next/link';
import { HeartPulse, Mail, Phone, MapPin, Facebook, Twitter, Linkedin, Instagram } from 'lucide-react';
import { APP_CONFIG } from '@/constants/app';

const columns = [
  {
    title: 'Services',
    links: [
      { label: 'Book Appointment', href: '/doctors' },
      { label: 'Find a Doctor', href: '/doctors' },
      { label: 'Medical Records', href: '/medical-records' },
      { label: 'Video Consultation', href: '/doctors' },
    ],
  },
  {
    title: 'Company',
    links: [
      { label: 'About Us', href: '/#about' },
      { label: 'Our Doctors', href: '/#doctors' },
      { label: 'Specialties', href: '/#specialties' },
      { label: 'Careers', href: '/#about' },
    ],
  },
  {
    title: 'Support',
    links: [
      { label: 'Help Center', href: '/#contact' },
      { label: 'Contact Us', href: '/#contact' },
      { label: 'FAQs', href: '/#faqs' },
      { label: 'Privacy Policy', href: '/#' },
    ],
  },
];

const socials = [
  { Icon: Facebook, href: '/#', label: 'Facebook' },
  { Icon: Twitter, href: '/#', label: 'Twitter' },
  { Icon: Linkedin, href: '/#', label: 'LinkedIn' },
  { Icon: Instagram, href: '/#', label: 'Instagram' },
];

export function LandingFooter() {
  return (
    <footer className="border-t bg-card">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid gap-10 md:grid-cols-2 lg:grid-cols-5">
          <div className="lg:col-span-2">
            <Link href="/" className="flex items-center gap-2.5">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-accent text-primary-foreground">
                <HeartPulse className="h-5 w-5" />
              </div>
              <span className="text-lg font-semibold">MediBook</span>
            </Link>
            <p className="mt-4 max-w-sm text-sm text-muted-foreground">{APP_CONFIG.description}</p>
            <div className="mt-5 space-y-2 text-sm text-muted-foreground">
              <p className="flex items-center gap-2"><Mail className="h-4 w-4 text-primary" /> {APP_CONFIG.supportEmail}</p>
              <p className="flex items-center gap-2"><Phone className="h-4 w-4 text-primary" /> {APP_CONFIG.supportPhone}</p>
              <p className="flex items-start gap-2"><MapPin className="mt-0.5 h-4 w-4 text-primary" /> {APP_CONFIG.address}</p>
            </div>
          </div>

          {columns.map((col) => (
            <div key={col.title}>
              <h3 className="text-sm font-semibold">{col.title}</h3>
              <ul className="mt-3 space-y-2">
                {col.links.map((l) => (
                  <li key={l.label}>
                    <Link href={l.href} className="text-sm text-muted-foreground transition-colors hover:text-foreground">
                      {l.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-10 flex flex-col items-center justify-between gap-4 border-t pt-6 sm:flex-row">
          <p className="text-xs text-muted-foreground">
            © {new Date().getFullYear()} {APP_CONFIG.fullName}. All rights reserved.
          </p>
          <div className="flex items-center gap-2">
            {socials.map(({ Icon, href, label }) => (
              <Link
                key={label}
                href={href}
                aria-label={label}
                className="flex h-9 w-9 items-center justify-center rounded-lg border text-muted-foreground transition-colors hover:border-primary hover:text-primary"
              >
                <Icon className="h-4 w-4" />
              </Link>
            ))}
          </div>
        </div>
      </div>
    </footer>
  );
}
