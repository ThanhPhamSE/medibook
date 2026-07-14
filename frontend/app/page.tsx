import { LandingNavbar } from '@/components/landing/navbar';
import { LandingFooter } from '@/components/landing/footer';
import { LandingHero } from '@/components/landing/hero';
import { LandingServices } from '@/components/landing/services';
import { LandingSpecialties } from '@/components/landing/specialties';
import { LandingDoctors } from '@/components/landing/doctors';
import { LandingTestimonials } from '@/components/landing/testimonials';
import { LandingFaqs } from '@/components/landing/faqs';
import { LandingStats, LandingCta } from '@/components/landing/stats-cta';
import { LandingAbout } from '@/components/landing/about';
import { LandingContact } from '@/components/landing/contact';

export default function HomePage() {
  return (
    <div className="min-h-screen bg-background">
      <LandingNavbar />
      <main>
        <LandingHero />
        <LandingStats />
        <LandingServices />
        <LandingSpecialties />
        <LandingDoctors />
        <LandingAbout />
        <LandingTestimonials />
        <LandingFaqs />
        <LandingCta />
        <LandingContact />
      </main>
      <LandingFooter />
    </div>
  );
}
