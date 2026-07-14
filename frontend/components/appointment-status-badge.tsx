'use client';

import { Badge } from '@/components/ui/badge';
import { classNamesFromStatus } from '@/utils/format';
import type { AppointmentStatus } from '@/types';

export function AppointmentStatusBadge({ status }: { status: AppointmentStatus }) {
  const meta = classNamesFromStatus(status);
  return (
    <Badge variant={meta.variant} className="gap-1.5">
      <span className={`h-1.5 w-1.5 rounded-full ${meta.dot}`} />
      {meta.label}
    </Badge>
  );
}
