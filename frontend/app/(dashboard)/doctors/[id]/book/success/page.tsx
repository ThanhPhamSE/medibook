'use client';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { CheckCircle2, Calendar, Clock, User, MapPin, Download, Share2 } from 'lucide-react';
import { useSearchParams } from 'next/navigation';

export default function BookingSuccessPage() {
  const searchParams = useSearchParams();
  const bookingCode = searchParams.get('code') || 'MB-12345';
  const doctorName = searchParams.get('doctor') || 'Dr. John Doe';
  const date = searchParams.get('date') || '2024-01-15';
  const time = searchParams.get('time') || '09:00';
  const specialty = searchParams.get('specialty') || 'Cardiology';

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/5 via-background to-primary/5 p-4">
      <div className="w-full max-w-2xl space-y-6">
        <div className="text-center">
          <div className="mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-green-100 text-green-600">
            <CheckCircle2 className="h-10 w-10" />
          </div>
          <h1 className="text-3xl font-bold">Booking Confirmed!</h1>
          <p className="text-muted-foreground mt-2">Your appointment has been successfully booked</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Appointment Details</CardTitle>
            <CardDescription>Booking Code: {bookingCode}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <User className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">Doctor</p>
                <p className="text-sm text-muted-foreground">{doctorName}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <MapPin className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">Chuyên khoa</p>
                <p className="text-sm text-muted-foreground">{specialty}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Calendar className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">Date</p>
                <p className="text-sm text-muted-foreground">{date}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Clock className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium">Time</p>
                <p className="text-sm text-muted-foreground">{time}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>QR Code</CardTitle>
            <CardDescription>Show this QR code at the clinic for check-in</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center space-y-4">
            <div className="h-48 w-48 rounded-lg border-2 border-dashed border-muted-foreground/25 flex items-center justify-center bg-muted/50">
              <div className="text-center">
                <p className="text-4xl font-bold tracking-widest">{bookingCode}</p>
                <p className="text-xs text-muted-foreground mt-2">QR Code Placeholder</p>
              </div>
            </div>
            <div className="flex gap-2">
              <Button variant="outline" className="gap-2">
                <Download className="h-4 w-4" />
                Download QR
              </Button>
              <Button variant="outline" className="gap-2">
                <Share2 className="h-4 w-4" />
                Share
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Important Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm text-muted-foreground">
            <p>• Please arrive 15 minutes before your appointment time</p>
            <p>• Bring a valid ID and your insurance card (if applicable)</p>
            <p>• If you need to reschedule, please do so at least 24 hours in advance</p>
            <p>• You will receive a reminder email 24 hours before your appointment</p>
          </CardContent>
        </Card>

        <div className="flex gap-3">
          <Button variant="outline" className="flex-1" onClick={() => window.print()}>
            Print Details
          </Button>
          <Button className="flex-1" onClick={() => (window.location.href = '/dashboard/appointments')}>
            View My Appointments
          </Button>
        </div>
      </div>
    </div>
  );
}
