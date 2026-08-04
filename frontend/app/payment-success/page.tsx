'use client';

import * as React from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { CheckCircle2, ArrowRight, Calendar, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { api } from '@/services/api';

function PaymentSuccessContent() {
  const params = useSearchParams();
  const router = useRouter();
  const appointmentId = params.get('appointmentId');
  const [verifying, setVerifying] = React.useState(true);
  const [verified, setVerified] = React.useState(false);

  // Gọi backend verify để update DB nếu webhook không đến được (localhost dev)
  // Retry tối đa 5 lần với delay 2s vì PayOS đôi khi cần vài giây để finalize
  React.useEffect(() => {
    if (!appointmentId) {
      setVerifying(false);
      return;
    }

    let cancelled = false;
    const MAX_RETRIES = 5;
    const RETRY_DELAY_MS = 2000;

    const verify = async () => {
      for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
        if (cancelled) return;
        try {
          const res = await api.post<any>(`/payments/verify/${appointmentId}`);
          const status = res?.data?.status;
          if (status === 'PAID') {
            if (!cancelled) { setVerified(true); setVerifying(false); }
            return;
          }
        } catch (e) {
          // ignore, retry
        }
        // Chờ trước khi retry (trừ lần cuối)
        if (attempt < MAX_RETRIES && !cancelled) {
          await new Promise((r) => setTimeout(r, RETRY_DELAY_MS));
        }
      }
      // Hết retry — vẫn hiển thị success vì user đã thanh toán xong ở PayOS
      if (!cancelled) { setVerified(true); setVerifying(false); }
    };

    verify();
    return () => { cancelled = true; };
  }, [appointmentId]);


  // Tự động redirect về trang appointment sau 5 giây (chờ verify xong)
  React.useEffect(() => {
    if (!appointmentId || verifying) return;
    const timer = setTimeout(() => {
      router.push(`/appointments/${appointmentId}`);
    }, 5000);
    return () => clearTimeout(timer);
  }, [appointmentId, router, verifying]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 via-white to-teal-50">
      <div className="max-w-md w-full mx-auto px-6">
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
          {/* Header xanh */}
          <div className="bg-gradient-to-r from-emerald-500 to-teal-600 px-8 py-10 text-center">
            <div className="flex justify-center mb-4">
              <div className="bg-white/20 rounded-full p-4">
                {verifying ? (
                  <Loader2 className="w-14 h-14 text-white animate-spin" />
                ) : (
                  <CheckCircle2 className="w-14 h-14 text-white" />
                )}
              </div>
            </div>
            <h1 className="text-2xl font-bold text-white mb-2">
              {verifying ? 'Đang xác nhận thanh toán...' : 'Thanh toán thành công!'}
            </h1>
            <p className="text-emerald-100 text-sm">
              {verifying
                ? 'Vui lòng đợi trong giây lát'
                : 'Lịch hẹn của bạn đã được xác nhận'}
            </p>
          </div>

          {/* Body */}
          <div className="px-8 py-6 space-y-4">
            {appointmentId && (
              <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-4 text-center">
                <p className="text-xs text-emerald-600 font-medium uppercase tracking-wide mb-1">Mã lịch hẹn</p>
                <p className="text-emerald-800 font-bold text-lg">#{appointmentId}</p>
              </div>
            )}

            {!verifying && (
              <>
                <div className="text-center text-sm text-gray-500 space-y-1">
                  <p>Cảm ơn bạn đã sử dụng MediBook.</p>
                  <p>Bạn sẽ được chuyển về trang lịch hẹn sau <strong>5 giây</strong>.</p>
                </div>

                <div className="flex flex-col gap-3 pt-2">
                  {appointmentId ? (
                    <Button
                      asChild
                      className="w-full bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-600 hover:to-teal-700 text-white"
                    >
                      <Link href={`/appointments/${appointmentId}`}>
                        <Calendar className="w-4 h-4 mr-2" />
                        Xem chi tiết lịch hẹn
                        <ArrowRight className="w-4 h-4 ml-2" />
                      </Link>
                    </Button>
                  ) : (
                    <Button
                      asChild
                      className="w-full bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-600 hover:to-teal-700 text-white"
                    >
                      <Link href="/appointments">
                        <Calendar className="w-4 h-4 mr-2" />
                        Xem lịch hẹn của tôi
                        <ArrowRight className="w-4 h-4 ml-2" />
                      </Link>
                    </Button>
                  )}

                  <Button variant="outline" asChild className="w-full">
                    <Link href="/dashboard">Về trang chủ</Link>
                  </Button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PaymentSuccessPage() {
  return (
    <React.Suspense fallback={
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-emerald-500 border-t-transparent" />
      </div>
    }>
      <PaymentSuccessContent />
    </React.Suspense>
  );
}
