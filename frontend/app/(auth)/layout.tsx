export const dynamic = 'force-dynamic';

import Link from 'next/link';
import { HeartPulse, ArrowLeft } from 'lucide-react';

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="relative flex flex-col px-6 py-8 sm:px-10 lg:px-16">
        <Link href="/" className="inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-foreground">
          <ArrowLeft className="h-4 w-4" />
          Back to home
        </Link>
        <div className="flex flex-1 items-center justify-center py-10">
          <div className="w-full max-w-sm">{children}</div>
        </div>
        <p className="text-center text-xs text-muted-foreground">
          © {new Date().getFullYear()} MediBook Health Systems
        </p>
      </div>

      <div className="relative hidden overflow-hidden bg-gradient-to-br from-primary to-accent lg:block">
        <div className="absolute inset-0 bg-dots opacity-20" />
        <div className="absolute -right-20 -top-20 h-96 w-96 rounded-full bg-white/10 blur-3xl" />
        <div className="absolute -bottom-20 -left-20 h-96 w-96 rounded-full bg-white/10 blur-3xl" />
        <div className="relative flex h-full flex-col justify-between p-16 text-primary-foreground">
          <Link href="/" className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15 backdrop-blur">
              <HeartPulse className="h-6 w-6" />
            </div>
            <span className="text-xl font-semibold">MediBook</span>
          </Link>

          <div className="max-w-md">
            <h2 className="text-4xl font-semibold leading-tight tracking-tight text-balance">
              Healthcare, simplified.
            </h2>
            <p className="mt-4 text-lg text-primary-foreground/90">
              Join thousands of patients and doctors who trust MediBook for smarter, faster, and safer care.
            </p>
            <div className="mt-10 grid grid-cols-3 gap-6">
              <div>
                <p className="text-3xl font-semibold">12K+</p>
                <p className="mt-1 text-sm text-primary-foreground/80">Patients</p>
              </div>
              <div>
                <p className="text-3xl font-semibold">56+</p>
                <p className="mt-1 text-sm text-primary-foreground/80">Doctors</p>
              </div>
              <div>
                <p className="text-3xl font-semibold">4.9★</p>
                <p className="mt-1 text-sm text-primary-foreground/80">Rating</p>
              </div>
            </div>
          </div>

          <p className="text-sm text-primary-foreground/70">
            "MediBook transformed how I manage my practice. My patients love the convenience."
            <br />
            <span className="mt-2 block font-medium text-primary-foreground">— Dr. Sarah Chen, Cardiologist</span>
          </p>
        </div>
      </div>
    </div>
  );
}
