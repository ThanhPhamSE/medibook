import { ErrorPage } from '@/components/error-page';

export default function ServerErrorPage() {
  return (
    <ErrorPage
      code="500"
      title="Server error"
      description="Something went wrong on our end. Our team has been notified. Please try again in a moment."
      icon="ServerCrash"
    />
  );
}
