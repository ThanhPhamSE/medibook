'use client';

import Link from 'next/link';
import { Home, ArrowLeft, RotateCcw, ShieldAlert, ServerCrash, Compass, Lock, AlertTriangle } from 'lucide-react';
import { Button } from '@/components/ui/button';

type IconName = 'ShieldAlert' | 'ServerCrash' | 'Compass' | 'Lock' | 'AlertTriangle';

interface ErrorPageProps {
  code: string;
  title: string;
  description: string;
  icon: IconName;
  action?: { label: string; href: string };
}

const icons: Record<IconName, any> = {
  ShieldAlert,
  ServerCrash,
  Compass,
  Lock,
  AlertTriangle,
};

export function ErrorPage({ code, title, description, icon, action }: ErrorPageProps) {
  const Icon = icons[icon] || AlertTriangle;

  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-background px-4">
      <div className="absolute inset-0 -z-10 bg-dots opacity-30" />
      <div className="absolute -top-32 left-1/2 -z-10 h-96 w-96 -translate-x-1/2 rounded-full bg-primary/10 blur-3xl" />

      <div className="text-center animate-slide-up">
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-3xl border bg-card text-primary shadow-soft">
          <Icon className="h-10 w-10" />
        </div>
        <p className="mt-8 text-7xl font-bold tracking-tight text-balance sm:text-8xl">
          <span className="gradient-text">{code}</span>
        </p>
        <h1 className="mt-4 text-2xl font-semibold tracking-tight sm:text-3xl">{title}</h1>
        <p className="mx-auto mt-3 max-w-md text-muted-foreground text-balance">{description}</p>

        <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <Button asChild>
            <Link href="/"><Home className="mr-2 h-4 w-4" /> Back to home</Link>
          </Button>
          {action ? (
            <Button asChild variant="outline">
              <Link href={action.href}><ArrowLeft className="mr-2 h-4 w-4" /> {action.label}</Link>
            </Button>
          ) : (
            <Button variant="outline" onClick={() => window.location.reload()}>
              <RotateCcw className="mr-2 h-4 w-4" /> Reload page
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}
