'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { verifyEmailSchema, type VerifyEmailInput } from '@/schemas';
import { authService } from '@/services/auth.service';
import { Loader2, CheckCircle2 } from 'lucide-react';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { toast } from 'sonner';

export default function VerifyEmailPage() {
  const router = useRouter();
  const [verified, setVerified] = useState(false);
  const form = useForm<VerifyEmailInput>({
    resolver: zodResolver(verifyEmailSchema),
    defaultValues: { token: '' },
  });

  const onSubmit = async (values: VerifyEmailInput) => {
    try {
      await authService.verifyEmail(values.token);
      setVerified(true);
      toast.success('Email verified successfully');
      setTimeout(() => router.push('/dashboard'), 1500);
    } catch {
      toast.error('Invalid or expired code');
    }
  };

  if (verified) {
    return (
      <div className="animate-slide-up text-center">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-success/10 text-success">
          <CheckCircle2 className="h-7 w-7" />
        </div>
        <h1 className="mt-5 text-2xl font-semibold tracking-tight">Email verified</h1>
        <p className="mt-2 text-sm text-muted-foreground">Taking you to your dashboard...</p>
      </div>
    );
  }

  return (
    <div className="animate-slide-up">
      <h1 className="text-2xl font-semibold tracking-tight">Verify your email</h1>
      <p className="mt-2 text-sm text-muted-foreground">
        We sent a 6-digit code to your email. Enter it below to activate your account.
      </p>

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="mt-6 space-y-4">
          <FormField
            control={form.control}
            name="token"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Verification code</FormLabel>
                <FormControl>
                  <Input
                    placeholder="123456"
                    maxLength={6}
                    className="text-center text-lg tracking-[0.5em]"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Verifying...</> : 'Verify email'}
          </Button>
        </form>
      </Form>

      <p className="mt-6 text-center text-sm text-muted-foreground">
        Didn&apos;t get a code?{' '}
        <button className="font-medium text-primary hover:underline">Resend</button>
        <br />
        <Link href="/dashboard" className="font-medium text-primary hover:underline">Skip for now</Link>
      </p>
    </div>
  );
}
