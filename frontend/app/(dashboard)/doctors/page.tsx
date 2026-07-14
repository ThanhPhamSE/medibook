'use client';

import * as React from 'react';
import Link from 'next/link';
import { Star, Search, Filter } from 'lucide-react';
import { useDoctors, useSpecialties } from '@/hooks/use-api';
import { PageContainer, PageHeader } from '@/components/page-header';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/skeletons';
import { EmptyState } from '@/components/empty-state';
import { initials, formatCurrency } from '@/utils/format';
import type { Doctor, Specialty } from '@/types';

const sorts = [
  { value: 'rating', label: 'Đánh giá cao nhất' },
  { value: 'experience', label: 'Kinh nghiệm nhiều nhất' },
  { value: 'fee-low', label: 'Phí thấp nhất' },
  { value: 'fee-high', label: 'Phí cao nhất' },
];

export default function DoctorsPage() {
  const [search, setSearch] = React.useState('');
  const [specialty, setSpecialty] = React.useState('all');
  const [sort, setSort] = React.useState('rating');
  const [page, setPage] = React.useState(0);
  const { data: specialtiesData } = useSpecialties();
  const specialties: Specialty[] = (specialtiesData as any)?.items ?? specialtiesData?.content ?? [];

  const { data, isLoading } = useDoctors({ search, specialtyId: specialty === 'all' ? undefined : specialty, page, size: 12 });

  const doctors: Doctor[] = React.useMemo(() => {
    // Handle different response structures
    let list: Doctor[] = [];
    if (data && 'items' in data) {
      list = (data as any).items;
    } else if (data?.content) {
      list = data.content;
    } else if (Array.isArray(data)) {
      list = data;
    } else if (data && 'data' in data) {
      list = (data as any).data;
    }
    if (sort === 'rating') list = [...list].sort((a: Doctor, b: Doctor) => b.rating - a.rating);
    if (sort === 'experience') list = [...list].sort((a: Doctor, b: Doctor) => b.yearsOfExperience - a.yearsOfExperience);
    if (sort === 'fee-low') list = [...list].sort((a: Doctor, b: Doctor) => a.consultationFee - b.consultationFee);
    if (sort === 'fee-high') list = [...list].sort((a: Doctor, b: Doctor) => b.consultationFee - a.consultationFee);
    return list;
  }, [data, sort]);

  // Handle pagination for different response structures
  const totalElements = (data as any)?.pagination?.totalElements ?? data?.totalElements ?? 0;
  const totalPages = (data as any)?.pagination?.totalPages ?? data?.totalPages ?? 0;

  return (
    <PageContainer>
      <PageHeader
        title="Tìm bác sĩ"
        description="Tìm kiếm bác sĩ chuyên gia và đặt lịch khám."
        breadcrumbs={[{ label: 'Trang chủ', href: '/dashboard' }, { label: 'Bác sĩ' }]}
      />

      <Card className="shadow-card">
        <CardContent className="grid gap-3 p-4 sm:grid-cols-[1fr_auto_auto_auto]">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input placeholder="Tìm kiếm theo tên hoặc chuyên khoa..." className="pl-9" value={search} onChange={(e) => setSearch(e.target.value)} />
          </div>
          <Select value={specialty} onValueChange={setSpecialty}>
            <SelectTrigger className="w-full sm:w-52"><Filter className="mr-2 h-4 w-4 text-muted-foreground" /><SelectValue placeholder="Chuyên khoa" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Tất cả chuyên khoa</SelectItem>
              {specialties.map((d: Specialty) => (<SelectItem key={d.id} value={d.id}>{d.name}</SelectItem>))}
            </SelectContent>
          </Select>
          <Select value={sort} onValueChange={setSort}>
            <SelectTrigger className="w-full sm:w-44"><SelectValue /></SelectTrigger>
            <SelectContent>{sorts.map((s) => <SelectItem key={s.value} value={s.value}>{s.label}</SelectItem>)}</SelectContent>
          </Select>
          <div className="text-sm text-muted-foreground">
            {totalElements} bác sĩ
          </div>
        </CardContent>
      </Card>

      {isLoading ? (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">{Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-72 rounded-xl" />)}</div>
      ) : doctors.length === 0 ? (
        <Card><CardContent className="p-0"><EmptyState title="Không tìm thấy bác sĩ" description="Thử tìm kiếm với từ khóa khác hoặc bộ lọc khác." /></CardContent></Card>
      ) : (
        <>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {doctors.map((doc: Doctor) => (
              <Link key={doc.id} href={`/doctors/${doc.id}`} className="group">
                <Card className="h-full transition-all hover:-translate-y-1 hover:shadow-soft">
                  <CardContent className="p-6">
                    <div className="flex items-start gap-4">
                      <Avatar className="h-16 w-16 border-2 bg-gradient-to-br from-primary/15 to-accent/15"><AvatarFallback className="bg-transparent text-base font-semibold text-primary">{initials(doc.firstName, doc.lastName)}</AvatarFallback></Avatar>
                      <div className="min-w-0 flex-1">
                        <h3 className="truncate text-base font-semibold">BS. {doc.fullName}</h3>
                        <p className="truncate text-sm text-muted-foreground">{doc.specialtyName}</p>
                        <div className="mt-1.5 flex items-center gap-1 text-sm"><Star className="h-4 w-4 fill-warning text-warning" /><span className="font-medium">{doc.rating?.toFixed(1) ?? 'N/A'}</span><span className="text-muted-foreground">({doc.reviewCount ?? 0})</span></div>
                      </div>
                    </div>
                    <p className="mt-4 line-clamp-2 text-sm text-muted-foreground">{doc.bio ?? ''}</p>
                    <div className="mt-4 flex flex-wrap gap-1.5">
                      {(doc.specializations ?? []).slice(0, 2).map((s: string) => (<Badge key={s} variant="secondary" className="font-normal">{s}</Badge>))}
                      <Badge variant="outline" className="font-normal">{doc.yearsOfExperience ?? 0} năm kinh nghiệm</Badge>
                    </div>
                    <div className="mt-4 flex items-center justify-between border-t pt-4">
                      <div><p className="text-xs text-muted-foreground">Phí khám</p><p className="text-lg font-semibold text-primary">{formatCurrency(doc.consultationFee ?? 0)}</p></div>
                      <Button size="sm" className="group-hover:shadow-glow">Đặt lịch</Button>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2">
              <Button variant="outline" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>Trước</Button>
              <span className="text-sm">Trang {page + 1} / {totalPages}</span>
              <Button variant="outline" onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>Sau</Button>
            </div>
          )}
        </>
      )}
    </PageContainer>
  );
}
