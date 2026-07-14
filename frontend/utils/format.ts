import { format, formatDistanceToNow, isValid, parse, parseISO } from 'date-fns';
import type { AppointmentStatus } from '@/types';

export function formatCurrency(amount: number, currency = 'USD'): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatNumber(value: number): string {
  return new Intl.NumberFormat('en-US').format(value);
}

export function formatDate(date: string | Date, pattern = 'MMM d, yyyy'): string {
  try {
    const d = typeof date === 'string' ? parseISO(date) : date;
    return format(d, pattern);
  } catch {
    return 'Invalid date';
  }
}

export function formatDateTime(date: string | Date): string {
  try {
    const d = typeof date === 'string' ? parseISO(date) : date;
    return format(d, 'MMM d, yyyy · h:mm a');
  } catch {
    return 'Invalid date';
  }
}

export function formatTime(value: string | Date): string {
  try {
    if (value instanceof Date) {
      return format(value, 'h:mm a');
    }

    // Trường hợp chỉ có giờ: "09:30" hoặc "09:30:00"
    if (/^\d{2}:\d{2}(:\d{2})?$/.test(value)) {
      const parsed = parse(
        value,
        value.length === 5 ? 'HH:mm' : 'HH:mm:ss',
        new Date()
      );
      return format(parsed, 'h:mm a');
    }

    // Trường hợp ISO
    const parsed = parseISO(value);

    if (!isValid(parsed)) return 'Invalid time';

    return format(parsed, 'h:mm a');
  } catch {
    return 'Invalid time';
  }
}

export function timeAgo(date: string | Date): string {
  try {
    const d = typeof date === 'string' ? parseISO(date) : date;
    return formatDistanceToNow(d, { addSuffix: true });
  } catch {
    return 'Invalid date';
  }
}

export function initials(first: string, last: string): string {
  return `${first?.[0] ?? ''}${last?.[0] ?? ''}`.toUpperCase();
}

export function fullName(first: string, last: string): string {
  return `${first} ${last}`.trim();
}

export function exportToCsv<T extends Record<string, unknown>>(
  filename: string,
  rows: T[]
): void {
  if (!rows.length) return;
  const headers = Object.keys(rows[0]);
  const escape = (val: unknown) => {
    const s = val == null ? '' : String(val);
    return `"${s.replace(/"/g, '""')}"`;
  };
  const csv = [
    headers.join(','),
    ...rows.map((r) => headers.map((h) => escape(r[h])).join(',')),
  ].join('\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export function classNamesFromStatus(status: AppointmentStatus): {
  label: string;
  variant: 'default' | 'secondary' | 'destructive' | 'outline' | 'success' | 'warning';
  dot: string;
} {
  const map: Record<AppointmentStatus, { label: string; variant: any; dot: string }> = {
    PENDING: { label: 'Pending', variant: 'warning', dot: 'bg-warning' },
    CONFIRMED: { label: 'Confirmed', variant: 'success', dot: 'bg-success' },
    COMPLETED: { label: 'Completed', variant: 'default', dot: 'bg-primary' },
    CANCELLED: { label: 'Cancelled', variant: 'destructive', dot: 'bg-destructive' },
    NO_SHOW: { label: 'No Show', variant: 'secondary', dot: 'bg-muted-foreground' },
  };
  return map[status];
}
