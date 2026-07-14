import Link from 'next/link';
import { Wrench, Mail } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { APP_CONFIG } from '@/constants/app';

export default function MaintenancePage() {
  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-background px-4">
      <div className="absolute inset-0 -z-10 bg-dots opacity-30" />
      <div className="absolute -top-32 left-1/2 -z-10 h-96 w-96 -translate-x-1/2 rounded-full bg-warning/10 blur-3xl" />

      <div className="text-center animate-slide-up">
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-3xl border bg-card text-warning shadow-soft">
          <Wrench className="h-10 w-10" />
        </div>
        <span className="mt-8 inline-block rounded-full bg-warning/10 px-3 py-1 text-xs font-semibold uppercase tracking-wider text-warning">
          Under maintenance
        </span>
        <h1 className="mt-4 text-3xl font-semibold tracking-tight sm:text-4xl">We&apos;ll be right back</h1>
        <p className="mx-auto mt-3 max-w-md text-muted-foreground text-balance">
          MediBook is undergoing scheduled maintenance to bring you a better experience. We expect to be back shortly.
        </p>
        <Button asChild variant="outline" className="mt-8">
          <a href={`mailto:${APP_CONFIG.supportEmail}`}>
            <Mail className="mr-2 h-4 w-4" /> Contact support
          </a>
        </Button>
      </div>
    </div>
  );
}
