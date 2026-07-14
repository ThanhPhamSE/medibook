'use client';

import Link from 'next/link';
import { ShieldAlert } from 'lucide-react';
import { Button } from '@/components/ui/button';

/**
 * Trang /reset-password không có token trong URL.
 * Link hợp lệ có dạng: /reset-password/[token]
 * Hướng dẫn user yêu cầu link mới.
 */
export default function ResetPasswordPage() {
  return (
    <div className="animate-slide-up text-center">
      <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-destructive/10 text-destructive">
        <ShieldAlert className="h-7 w-7" />
      </div>
      <h1 className="mt-5 text-2xl font-semibold tracking-tight">Invalid or missing link</h1>
      <p className="mt-2 text-sm text-muted-foreground">
        The password reset link is missing or has expired.<br />
        Please request a new link from the forgot password page.
      </p>
      <Button asChild className="mt-6 w-full">
        <Link href="/forgot-password">Request a new link</Link>
      </Button>
      <p className="mt-4 text-center text-sm text-muted-foreground">
        <Link href="/login" className="font-medium text-primary hover:underline">
          Back to sign in
        </Link>
      </p>
    </div>
  );
}
