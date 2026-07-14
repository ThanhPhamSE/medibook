'use client';

import { useRoles, usePermissions } from '@/hooks/use-api';
import { RoleGuard } from '@/components/role-guard';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/skeletons';
import { Shield, Check, Lock } from 'lucide-react';

function RolesPermissions() {
  const { data: roles, isLoading: rLoading } = useRoles();
  const { data: permissions, isLoading: pLoading } = usePermissions();

  return (
    <PageContainer>
      <PageHeader title="Roles & Permissions" description="Define access levels across the platform." breadcrumbs={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Roles & Permissions' }]} />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader><CardTitle className="flex items-center gap-2 text-base"><Shield className="h-4 w-4 text-primary" /> Roles</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {rLoading ? Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-20" />) :
              (roles ?? []).map((r: any) => (
                <div key={r.id} className="flex items-center justify-between rounded-lg border p-4">
                  <div><p className="text-sm font-semibold">{r.name}</p><p className="text-xs text-muted-foreground">{r.description}</p></div>
                  <div className="flex items-center gap-3 text-right">
                    <div><p className="text-sm font-semibold">{r.usersCount}</p><p className="text-[10px] text-muted-foreground">users</p></div>
                    <div><p className="text-sm font-semibold">{r.permissions}</p><p className="text-[10px] text-muted-foreground">perms</p></div>
                  </div>
                </div>
              ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="flex items-center gap-2 text-base"><Lock className="h-4 w-4 text-primary" /> Permissions</CardTitle></CardHeader>
          <CardContent className="space-y-2">
            {pLoading ? Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-16" />) :
              (permissions ?? []).map((p: any) => (
                <div key={p.id} className="rounded-lg border p-4">
                  <div className="flex items-center justify-between">
                    <div><p className="text-sm font-semibold">{p.module}</p><p className="text-xs text-muted-foreground capitalize">{p.action} access</p></div>
                    <Badge variant="info">{p.roles.length} roles</Badge>
                  </div>
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {p.roles.map((role: string) => <Badge key={role} variant="secondary" className="font-normal"><Check className="mr-1 h-3 w-3 text-success" />{role}</Badge>)}
                  </div>
                </div>
              ))}
          </CardContent>
        </Card>
      </div>
    </PageContainer>
  );
}

export default function Page() {
  return <RoleGuard roles={['ADMIN']}><RolesPermissions /></RoleGuard>;
}
