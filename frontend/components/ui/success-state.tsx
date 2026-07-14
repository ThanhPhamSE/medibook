import { cn } from '@/lib/utils';
import { CheckCircle2 } from 'lucide-react';

interface SuccessStateProps {
  title: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}

export function SuccessState({ title, description, action, className }: SuccessStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center p-8 text-center', className)}>
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-green-100 text-green-600">
        <CheckCircle2 className="h-8 w-8" />
      </div>
      <h3 className="text-lg font-semibold text-green-900">{title}</h3>
      {description && <p className="mt-2 text-sm text-green-700 max-w-sm">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
