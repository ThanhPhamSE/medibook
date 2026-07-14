import { ErrorPage } from '@/components/error-page';

export default function ForbiddenPage() {
  return (
    <ErrorPage
      code="403"
      title="Access forbidden"
      description="You don't have permission to view this page. If you believe this is an error, contact your administrator."
      icon="Lock"
      action={{ label: 'Go to dashboard', href: '/dashboard' }}
    />
  );
}
