'use client';

import * as React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

import {
  profileSchema,
  type ProfileInput,
  changePasswordSchema,
  type ChangePasswordInput,
} from '@/schemas';

import { useAuth } from '@/contexts/auth-context';
import { authService } from '@/services/auth.service';

import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

import { Camera, Loader2, Lock, User as UserIcon } from 'lucide-react';
import { toast } from 'sonner';

import { initials } from '@/utils/format';
import { GENDERS } from '@/constants/medical';
import { extractApiError } from '@/services/api';

export default function ProfilePage() {
  const { user: currentUser, updateUser, logout } = useAuth();

  const profileForm = useForm<ProfileInput>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      fullName: '',
      email: '',
      phone: '',
      birthDate: '',
      gender: '',
      profileImage: '',
    },
  });

  const passwordForm = useForm<ChangePasswordInput>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  // LOAD PROFILE - use user from auth context instead of making separate API call
  React.useEffect(() => {
    if (currentUser) {
      profileForm.reset({
        fullName: currentUser.fullName ?? '',
        email: currentUser.email ?? '',
        phone: currentUser.phone ?? '',
        birthDate: currentUser.birthDate
          ? currentUser.birthDate.substring(0, 10)
          : '',
        gender: currentUser.gender ?? '',
        profileImage: currentUser.profileImage ?? '',
      });
    }
  }, [currentUser, profileForm]);

  // UPDATE PROFILE
  const onProfile = async (values: ProfileInput) => {
    try {
      const { gender, profileImage, email, ...rest } = values;

      const updated = await authService.updateProfile({
        ...rest,
        gender: gender || undefined,
        profileImage: profileImage || undefined,
      });

      updateUser(updated);

      profileForm.reset({
        fullName: updated.fullName,
        email: updated.email,
        phone: updated.phone ?? '',
        birthDate: updated.birthDate?.substring(0, 10) ?? '',
        gender: updated.gender ?? '',
        profileImage: updated.profileImage ?? '',
      });

      toast.success('Cập nhật hồ sơ thành công');
    } catch (e) {
      toast.error(extractApiError(e, 'Không thể cập nhật hồ sơ'));
    }
  };

  // CHANGE PASSWORD
    const onPassword = async (values: ChangePasswordInput) => {
    try {
      await authService.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
        confirmPassword: values.confirmPassword,
      });

      toast.success('Đổi mật khẩu thành công. Vui lòng đăng nhập lại.');

      passwordForm.reset();

      // Logout and redirect to login after password change
      setTimeout(() => {
        logout();
      }, 1500);

    } catch (e) {
      toast.error(
        extractApiError(e, 'Không thể đổi mật khẩu')
      );
    }
  };

  const user = profileForm.getValues();

  return (
    <PageContainer>
      <PageHeader
        title="Profile"
        description="Manage your personal information and security."
        breadcrumbs={[
          { label: 'Home', href: '/dashboard' },
          { label: 'Profile' },
        ]}
      />

      <div className="grid gap-6 lg:grid-cols-3">
        {/* LEFT CARD */}
        <Card className="h-fit">
          <CardContent className="flex flex-col items-center p-6 text-center">
            <div className="relative">
              <Avatar className="h-24 w-24 border-2">
                <AvatarFallback className="text-2xl font-semibold">
                  {initials(user.fullName?.split(' ')[0] || '', user.fullName?.split(' ')[1] || '')}
                </AvatarFallback>
              </Avatar>

              <button className="absolute -bottom-1 -right-1 flex h-8 w-8 items-center justify-center rounded-full bg-primary text-white">
                <Camera className="h-4 w-4" />
              </button>
            </div>

            <h2 className="mt-4 text-lg font-semibold">
              {user.fullName}
            </h2>

            <p className="text-sm text-muted-foreground">
              {user.email ?? 'No email'}
            </p>

            <p className="mt-2 rounded-full bg-primary/10 px-3 py-1 text-xs font-medium">
              {currentUser?.roleName ?? 'UNKNOWN'}
            </p>
          </CardContent>
        </Card>

        {/* RIGHT */}
        <div className="lg:col-span-2">
          <Tabs defaultValue="personal">
            <TabsList>
              <TabsTrigger value="personal">
                <UserIcon className="mr-2 h-4 w-4" />
                Personal
              </TabsTrigger>

              <TabsTrigger value="security">
                <Lock className="mr-2 h-4 w-4" />
                Security
              </TabsTrigger>
            </TabsList>

            {/* PERSONAL */}
            <TabsContent value="personal" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle>Personal information</CardTitle>
                </CardHeader>

                <CardContent>
                  <Form {...profileForm}>
                    <form
                      onSubmit={profileForm.handleSubmit(onProfile)}
                      className="space-y-4"
                    >
                      <FormField
                        control={profileForm.control}
                        name="fullName"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Full name</FormLabel>
                            <FormControl>
                              <Input {...field} />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />

                      <FormField
                        control={profileForm.control}
                        name="email"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Email</FormLabel>
                            <FormControl>
                              <Input {...field} disabled className="bg-muted" />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />

                      <FormField
                        control={profileForm.control}
                        name="phone"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Phone</FormLabel>
                            <FormControl>
                              <Input {...field} />
                            </FormControl>
                          </FormItem>
                        )}
                      />

                      <div className="grid gap-4 sm:grid-cols-2">
                        <FormField
                          control={profileForm.control}
                          name="birthDate"
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Date of birth</FormLabel>
                              <FormControl>
                                <Input type="date" {...field} />
                              </FormControl>
                            </FormItem>
                          )}
                        />

                        <FormField
                          control={profileForm.control}
                          name="gender"
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Gender</FormLabel>
                              <Select
                                onValueChange={field.onChange}
                                value={field.value}
                              >
                                <FormControl>
                                  <SelectTrigger>
                                    <SelectValue placeholder="Select" />
                                  </SelectTrigger>
                                </FormControl>

                                <SelectContent>
                                  {GENDERS.map((g) => (
                                    <SelectItem key={g.value} value={g.value}>
                                      {g.label}
                                    </SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            </FormItem>
                          )}
                        />
                      </div>

                      <Button type="submit">
                        Save changes
                      </Button>
                    </form>
                  </Form>
                </CardContent>
              </Card>
            </TabsContent>

            {/* SECURITY */}
            <TabsContent value="security" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle>Change password</CardTitle>
                </CardHeader>

                <CardContent>
                  <Form {...passwordForm}>
                    <form
                      onSubmit={passwordForm.handleSubmit(onPassword)}
                      className="space-y-4"
                    >
                      <FormField
                        control={passwordForm.control}
                        name="currentPassword"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Current password</FormLabel>
                            <FormControl>
                              <Input type="password" {...field} />
                            </FormControl>
                          </FormItem>
                        )}
                      />

                      <FormField
                        control={passwordForm.control}
                        name="newPassword"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>New password</FormLabel>
                            <FormControl>
                              <Input type="password" {...field} />
                            </FormControl>
                          </FormItem>
                        )}
                      />

                      <FormField
                        control={passwordForm.control}
                        name="confirmPassword"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Confirm password</FormLabel>
                            <FormControl>
                              <Input type="password" {...field} />
                            </FormControl>
                          </FormItem>
                        )}
                      />

                      <Button type="submit">Update password</Button>
                    </form>
                  </Form>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </PageContainer>
  );
}