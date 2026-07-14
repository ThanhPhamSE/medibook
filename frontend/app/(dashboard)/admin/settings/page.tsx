'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Switch } from '@/components/ui/switch';
import { RoleGuard } from '@/components/role-guard';
import { toast } from 'sonner';
import { useState } from 'react';
import { Mail, Clock, Calendar, Settings as SettingsIcon } from 'lucide-react';
import { extractApiError } from '@/services/api';

export default function AdminSettingsPage() {
  const [emailSettings, setEmailSettings] = useState({
    smtpHost: 'smtp.gmail.com',
    smtpPort: '587',
    smtpUser: '',
    smtpPassword: '',
    fromEmail: 'noreply@medibook.health',
    fromName: 'MediBook',
  });

  const [scheduleSettings, setScheduleSettings] = useState({
    slotDuration: 30,
    workingHoursStart: '08:00',
    workingHoursEnd: '17:00',
    lunchBreakStart: '12:00',
    lunchBreakEnd: '13:00',
  });

  const [holidaySettings, setHolidaySettings] = useState({
    holidays: '2024-01-01,2024-12-25',
  });

  const handleSaveEmailSettings = (e: React.FormEvent) => {
    e.preventDefault();
    toast.success('Lưu cài đặt email thành công');
  };

  const handleSaveScheduleSettings = (e: React.FormEvent) => {
    e.preventDefault();
    toast.success('Lưu cài đặt lịch làm việc thành công');
  };

  const handleSaveHolidaySettings = (e: React.FormEvent) => {
    e.preventDefault();
    toast.success('Lưu cài đặt ngày nghỉ thành công');
  };

  return (
    <RoleGuard roles={['ADMIN']}>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">System Settings</h1>
          <p className="text-muted-foreground">Configure system-wide settings and preferences</p>
        </div>

        <Tabs defaultValue="email">
          <TabsList>
            <TabsTrigger value="email">
              <Mail className="mr-2 h-4 w-4" /> Email Configuration
            </TabsTrigger>
            <TabsTrigger value="schedule">
              <Clock className="mr-2 h-4 w-4" /> Schedule Settings
            </TabsTrigger>
            <TabsTrigger value="holidays">
              <Calendar className="mr-2 h-4 w-4" /> Holidays
            </TabsTrigger>
            <TabsTrigger value="general">
              <SettingsIcon className="mr-2 h-4 w-4" /> General
            </TabsTrigger>
          </TabsList>

          <TabsContent value="email" className="mt-6">
            <Card>
              <CardHeader>
                <CardTitle>Email Configuration</CardTitle>
                <CardDescription>Configure SMTP settings for sending emails</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSaveEmailSettings} className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="smtpHost">SMTP Host</Label>
                      <Input
                        id="smtpHost"
                        value={emailSettings.smtpHost}
                        onChange={(e) => setEmailSettings({ ...emailSettings, smtpHost: e.target.value })}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="smtpPort">SMTP Port</Label>
                      <Input
                        id="smtpPort"
                        value={emailSettings.smtpPort}
                        onChange={(e) => setEmailSettings({ ...emailSettings, smtpPort: e.target.value })}
                      />
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="smtpUser">SMTP Username</Label>
                      <Input
                        id="smtpUser"
                        value={emailSettings.smtpUser}
                        onChange={(e) => setEmailSettings({ ...emailSettings, smtpUser: e.target.value })}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="smtpPassword">SMTP Password</Label>
                      <Input
                        id="smtpPassword"
                        type="password"
                        value={emailSettings.smtpPassword}
                        onChange={(e) => setEmailSettings({ ...emailSettings, smtpPassword: e.target.value })}
                      />
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="fromEmail">From Email</Label>
                      <Input
                        id="fromEmail"
                        value={emailSettings.fromEmail}
                        onChange={(e) => setEmailSettings({ ...emailSettings, fromEmail: e.target.value })}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="fromName">From Name</Label>
                      <Input
                        id="fromName"
                        value={emailSettings.fromName}
                        onChange={(e) => setEmailSettings({ ...emailSettings, fromName: e.target.value })}
                      />
                    </div>
                  </div>
                  <Button type="submit">Save Email Settings</Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="schedule" className="mt-6">
            <Card>
              <CardHeader>
                <CardTitle>Schedule Settings</CardTitle>
                <CardDescription>Configure default working hours and appointment duration</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSaveScheduleSettings} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="slotDuration">Default Slot Duration (minutes)</Label>
                    <Input
                      id="slotDuration"
                      type="number"
                      value={scheduleSettings.slotDuration}
                      onChange={(e) => setScheduleSettings({ ...scheduleSettings, slotDuration: parseInt(e.target.value) })}
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="workingHoursStart">Working Hours Start</Label>
                      <Input
                        id="workingHoursStart"
                        type="time"
                        value={scheduleSettings.workingHoursStart}
                        onChange={(e) => setScheduleSettings({ ...scheduleSettings, workingHoursStart: e.target.value })}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="workingHoursEnd">Working Hours End</Label>
                      <Input
                        id="workingHoursEnd"
                        type="time"
                        value={scheduleSettings.workingHoursEnd}
                        onChange={(e) => setScheduleSettings({ ...scheduleSettings, workingHoursEnd: e.target.value })}
                      />
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="lunchBreakStart">Lunch Break Start</Label>
                      <Input
                        id="lunchBreakStart"
                        type="time"
                        value={scheduleSettings.lunchBreakStart}
                        onChange={(e) => setScheduleSettings({ ...scheduleSettings, lunchBreakStart: e.target.value })}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="lunchBreakEnd">Lunch Break End</Label>
                      <Input
                        id="lunchBreakEnd"
                        type="time"
                        value={scheduleSettings.lunchBreakEnd}
                        onChange={(e) => setScheduleSettings({ ...scheduleSettings, lunchBreakEnd: e.target.value })}
                      />
                    </div>
                  </div>
                  <Button type="submit">Save Schedule Settings</Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="holidays" className="mt-6">
            <Card>
              <CardHeader>
                <CardTitle>Holiday Settings</CardTitle>
                <CardDescription>Configure system holidays (comma-separated dates in YYYY-MM-DD format)</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSaveHolidaySettings} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="holidays">Holidays</Label>
                    <Textarea
                      id="holidays"
                      value={holidaySettings.holidays}
                      onChange={(e) => setHolidaySettings({ ...holidaySettings, holidays: e.target.value })}
                      rows={4}
                      placeholder="2024-01-01,2024-12-25,2024-07-04"
                    />
                    <p className="text-sm text-muted-foreground">
                      Enter holiday dates separated by commas
                    </p>
                  </div>
                  <Button type="submit">Save Holiday Settings</Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="general" className="mt-6">
            <Card>
              <CardHeader>
                <CardTitle>General Settings</CardTitle>
                <CardDescription>Configure general system preferences</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="flex items-center justify-between">
                  <div>
                    <Label>Enable Email Notifications</Label>
                    <p className="text-sm text-muted-foreground">Send email notifications for appointments</p>
                  </div>
                  <Switch defaultChecked />
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    <Label>Enable SMS Notifications</Label>
                    <p className="text-sm text-muted-foreground">Send SMS notifications for appointments</p>
                  </div>
                  <Switch />
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    <Label>Require Email Verification</Label>
                    <p className="text-sm text-muted-foreground">Require users to verify their email address</p>
                  </div>
                  <Switch defaultChecked />
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    <Label>Auto-Confirm Appointments</Label>
                    <p className="text-sm text-muted-foreground">Automatically confirm new appointments</p>
                  </div>
                  <Switch />
                </div>
                <Button>Save General Settings</Button>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </RoleGuard>
  );
}
