'use client';

import * as React from 'react';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Bell, Globe, Moon, Shield } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useTheme } from 'next-themes';
import { toast } from 'sonner';

export default function SettingsPage() {
  const { theme, setTheme } = useTheme();
  const [emailNotif, setEmailNotif] = React.useState(true);
  const [bookingNotif, setBookingNotif] = React.useState(true);
  const [reminderNotif, setReminderNotif] = React.useState(true);
  const [marketingNotif, setMarketingNotif] = React.useState(false);

  return (
    <PageContainer>
      <PageHeader title="Settings" description="Manage your preferences and account configuration." breadcrumbs={[{ label: 'Home', href: '/dashboard' }, { label: 'Settings' }]} />

      <div className="space-y-6">
        <Card>
          <CardHeader><CardTitle className="flex items-center gap-2 text-base"><Globe className="h-4 w-4 text-primary" /> Appearance</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div><Label>Theme</Label><p className="text-sm text-muted-foreground">Choose your preferred color scheme.</p></div>
              <Select value={theme} onValueChange={setTheme}>
                <SelectTrigger className="w-36"><Moon className="mr-2 h-4 w-4" /><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="light">Light</SelectItem>
                  <SelectItem value="dark">Dark</SelectItem>
                  <SelectItem value="system">System</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="flex items-center gap-2 text-base"><Bell className="h-4 w-4 text-primary" /> Notifications</CardTitle></CardHeader>
          <CardContent className="space-y-1">
            <ToggleRow label="Email notifications" description="Receive booking and system updates by email." checked={emailNotif} onCheckedChange={setEmailNotif} />
            <Separator />
            <ToggleRow label="Booking notifications" description="Get notified about appointment changes." checked={bookingNotif} onCheckedChange={setBookingNotif} />
            <Separator />
            <ToggleRow label="Visit reminders" description="Receive a reminder before each appointment." checked={reminderNotif} onCheckedChange={setReminderNotif} />
            <Separator />
            <ToggleRow label="Marketing & news" description="Occasional product updates and health tips." checked={marketingNotif} onCheckedChange={setMarketingNotif} />
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="flex items-center gap-2 text-base"><Shield className="h-4 w-4 text-primary" /> Security</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div><Label>Two-factor authentication</Label><p className="text-sm text-muted-foreground">Add an extra layer of security to your account.</p></div>
              <Button variant="outline" onClick={() => toast.info('2FA setup coming soon')}>Enable</Button>
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <div><Label>Active sessions</Label><p className="text-sm text-muted-foreground">Manage devices logged into your account.</p></div>
              <Button variant="outline" onClick={() => toast.info('Session management coming soon')}>View sessions</Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </PageContainer>
  );
}

function ToggleRow({ label, description, checked, onCheckedChange }: { label: string; description: string; checked: boolean; onCheckedChange: (v: boolean) => void }) {
  return (
    <div className="flex items-center justify-between py-3">
      <div><Label>{label}</Label><p className="text-sm text-muted-foreground">{description}</p></div>
      <Switch checked={checked} onCheckedChange={onCheckedChange} />
    </div>
  );
}

