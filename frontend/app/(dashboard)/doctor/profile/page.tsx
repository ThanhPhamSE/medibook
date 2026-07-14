'use client';

import { useAuth } from '@/contexts/auth-context';
import { useUpdateProfile } from '@/hooks/use-api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'sonner';
import { useState } from 'react';
import { RoleGuard } from '@/components/role-guard';
import { extractApiError } from '@/services/api';

export default function DoctorProfilePage() {
  const { user } = useAuth();
  const updateProfile = useUpdateProfile();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    fullName: user?.fullName || '',
    phone: user?.phone || '',
    bio: '',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfile.mutate(formData, {
      onSuccess: () => {
        setIsEditing(false);
        toast.success('Cập nhật hồ sơ thành công');
      },
    });
  };

  const handleCancel = () => {
    setFormData({
      fullName: user?.fullName || '',
      phone: user?.phone || '',
      bio: '',
    });
    setIsEditing(false);
  };

  return (
    <RoleGuard roles={['DOCTOR']}>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Doctor Profile</h1>
          <p className="text-muted-foreground">Manage your personal information and professional details</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Personal Information</CardTitle>
            <CardDescription>Update your personal details and contact information</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="firstName">First Name</Label>
                  <Input
                    id="firstName"
                    value={formData.fullName}
                    onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                    disabled={!isEditing}
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" value={user?.email || ''} disabled />
              </div>
              <div className="space-y-2">
                <Label htmlFor="phone">Phone</Label>
                <Input
                  id="phone"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  disabled={!isEditing}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="bio">Bio</Label>
                <Textarea
                  id="bio"
                  value={formData.bio}
                  onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                  disabled={!isEditing}
                  rows={4}
                />
              </div>
              <div className="flex gap-2">
                {isEditing ? (
                  <>
                    <Button type="submit" disabled={updateProfile.isPending}>
                      {updateProfile.isPending ? 'Saving...' : 'Save Changes'}
                    </Button>
                    <Button type="button" variant="outline" onClick={handleCancel}>
                      Cancel
                    </Button>
                  </>
                ) : (
                  <Button type="button" onClick={() => setIsEditing(true)}>
                    Edit Profile
                  </Button>
                )}
              </div>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Professional Information</CardTitle>
            <CardDescription>Your professional details are managed by administrators</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label>Specialization</Label>
              <p className="text-sm text-muted-foreground mt-1">
                {user?.roleName === 'DOCTOR' ? 'Cardiology' : 'N/A'}
              </p>
            </div>
            <div>
              <Label>Years of Experience</Label>
              <p className="text-sm text-muted-foreground mt-1">
                {user?.roleName === 'DOCTOR' ? '5+ years' : 'N/A'}
              </p>
            </div>
            <div>
              <Label>Consultation Fee</Label>
              <p className="text-sm text-muted-foreground mt-1">
                {user?.roleName === 'DOCTOR' ? '$50.00' : 'N/A'}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </RoleGuard>
  );
}
