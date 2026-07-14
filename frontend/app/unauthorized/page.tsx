import { ErrorPage } from '@/components/error-page';

export default function UnauthorizedPage() {
  return (
    <ErrorPage
      code="401"
      title="Unauthorized"
      description="You need to sign in to access this page. Please log in and try again."
      icon="ShieldAlert"
      action={{ label: 'Sign in', href: '/login' }}
    />
  );
}
