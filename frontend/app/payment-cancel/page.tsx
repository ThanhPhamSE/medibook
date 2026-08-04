'use client';

import * as React from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { XCircle, ArrowRight, Calendar, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';

function PaymentCancelContent() {
  const params = useSearchParams();
  const router = useRouter();
  const appointmentId = params.get('appointmentId');

  // Tự động redirect về trang appointment sau 8 giây
  React.useEffect(() => {
    if (!appointmentId) return;
    const timer = setTimeout(() => {
      router.push(`/appointments/${appointmentId}`);
    }, 8000);
    return () => clearTimeout(timer);
  }, [appointmentId, router]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-rose-50 via-white to-orange-50">
      <div className="max-w-md w-full mx-auto px-6">
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
          {/* Header đỏ */}
          <div className="bg-gradient-to-r from-rose-500 to-orange-500 px-8 py-10 text-center">
            <div className="flex justify-center mb-4">
              <div className="bg-white/20 rounded-full p-4">
                <XCircle className="w-14 h-14 text-white" />
              </div>
            </div>
            <h1 className="text-2xl font-bold text-white mb-2">Thanh toán bị hủy</h1>
            <p className="text-rose-100 text-sm">
              Bạn đã hủy giao dịch thanh toán
            </p>
          </div>

          {/* Body */}
          <div className="px-8 py-6 space-y-4">
            {appointmentId && (
              <div className="bg-rose-50 border border-rose-200 rounded-xl p-4 text-center">
                <p className="text-xs text-rose-600 font-medium uppercase tracking-wide mb-1">Mã lịch hẹn</p>
                <p className="text-rose-800 font-bold text-lg">#{appointmentId}</p>
              </div>
            )}

            <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
              <p className="text-sm text-amber-800 text-center">
                Lịch hẹn của bạn <strong>vẫn còn hiệu lực</strong>. Bạn có thể quay lại
                để thực hiện thanh toán bất kỳ lúc nào.
              </p>
            </div>

            <div className="text-center text-sm text-gray-500">
              <p>Tự động chuyển về lịch hẹn sau <strong>8 giây</strong>.</p>
            </div>

            <div className="flex flex-col gap-3 pt-2">
              {appointmentId ? (
                <>
                  <Button
                    className="w-full bg-gradient-to-r from-rose-500 to-orange-500 hover:from-rose-600 hover:to-orange-600 text-white"
                    onClick={() => router.push(`/appointments/${appointmentId}`)}
                  >
                    <RefreshCw className="w-4 h-4 mr-2" />
                    Thử thanh toán lại
                    <ArrowRight className="w-4 h-4 ml-2" />
                  </Button>

                  <Button variant="outline" asChild className="w-full">
                    <Link href={`/appointments/${appointmentId}`}>
                      <Calendar className="w-4 h-4 mr-2" />
                      Xem chi tiết lịch hẹn
                    </Link>
                  </Button>
                </>
              ) : (
                <Button
                  asChild
                  className="w-full bg-gradient-to-r from-rose-500 to-orange-500 hover:from-rose-600 hover:to-orange-600 text-white"
                >
                  <Link href="/appointments">
                    <Calendar className="w-4 h-4 mr-2" />
                    Xem lịch hẹn của tôi
                  </Link>
                </Button>
              )}

              <Button variant="outline" asChild className="w-full">
                <Link href="/dashboard">Về trang chủ</Link>
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PaymentCancelPage() {
  return (
    <React.Suspense fallback={
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-rose-500 border-t-transparent" />
      </div>
    }>
      <PaymentCancelContent />
    </React.Suspense>
  );
}
