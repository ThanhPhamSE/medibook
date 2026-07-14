'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useRef, useState } from 'react';
import { authService } from '@/services/auth.service';
import { Loader2, CheckCircle2, XCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';

export default function VerifyEmailTokenPage({ params }: { params: { token: string } }) {
  const router = useRouter();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');
  const isVerifying = useRef(false);

  useEffect(() => {
    const verify = async () => {
      if (isVerifying.current) return;
      isVerifying.current = true;

      try {
        await authService.verifyEmail(params.token);
        setStatus('success');
        toast.success('Email verified successfully');
        setTimeout(() => router.push('/login'), 2000);
      } catch (err: any) {
        setStatus('error');
        const errorMessage = err.response?.data?.message || err.message || 'Invalid or expired verification link';
        setMessage(errorMessage);
        toast.error(errorMessage);
      }
    };
    verify();
  }, [params.token, router]);

  if (status === 'loading') {
    return (
      <div className="animate-slide-up text-center">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10 text-primary">
          <Loader2 className="h-7 w-7 animate-spin" />
        </div>
        <h1 className="mt-5 text-2xl font-semibold tracking-tight">Verifying your email</h1>
        <p className="mt-2 text-sm text-muted-foreground">Please wait while we verify your email address...</p>
      </div>
    );
  }

  if (status === 'success') {
    return (
      <div className="animate-slide-up text-center">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-success/10 text-success">
          <CheckCircle2 className="h-7 w-7" />
        </div>
        <h1 className="mt-5 text-2xl font-semibold tracking-tight">Email verified</h1>
        <p className="mt-2 text-sm text-muted-foreground">Your email has been verified successfully. Redirecting to login...</p>
      </div>
    );
  }

  return (
    <div className="animate-slide-up text-center">
      <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-destructive/10 text-destructive">
        <XCircle className="h-7 w-7" />
      </div>
      <h1 className="mt-5 text-2xl font-semibold tracking-tight">Verification failed</h1>
      <p className="mt-2 text-sm text-muted-foreground">{message}</p>
      <div className="mt-6 flex justify-center gap-3">
        <Button variant="outline" onClick={() => router.push('/login')}>
          Back to login
        </Button>
        <Button onClick={() => window.location.reload()}>
          Try again
        </Button>
      </div>
    </div>
  );
}
