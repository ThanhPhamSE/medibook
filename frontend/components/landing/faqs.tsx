'use client';

import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/accordion';

const faqs = [
  { q: 'How do I book an appointment?', a: 'Create a free account, browse doctors by specialty, pick a date and time slot that works for you, and confirm. You will receive a booking code and instant confirmation.' },
  { q: 'Can I consult a doctor online?', a: 'Yes. Many of our doctors offer video consultations. Look for the "Video" badge when selecting a time slot.' },
  { q: 'Are my medical records private?', a: 'Absolutely. MediBook is HIPAA-compliant and uses bank-grade encryption. Only you and your authorized doctors can access your records.' },
  { q: 'How do I reschedule or cancel?', a: 'Go to "My Appointments" in your dashboard, open the appointment, and choose reschedule or cancel. Changes are reflected instantly.' },
  { q: 'Do you accept insurance?', a: 'We partner with major insurance providers. Check the doctor profile or contact support to confirm coverage before booking.' },
  { q: 'What if I forget my password?', a: 'Click "Forgot password" on the login page. We will email you a secure reset link valid for 30 minutes.' },
];

export function LandingFaqs() {
  return (
    <section id="faqs" className="py-20 sm:py-28">
      <div className="mx-auto max-w-3xl px-4 sm:px-6 lg:px-8">
        <div className="text-center">
          <p className="text-sm font-semibold uppercase tracking-wider text-primary">FAQs</p>
          <h2 className="mt-2 text-3xl font-semibold tracking-tight text-balance sm:text-4xl">
            Frequently asked questions
          </h2>
        </div>

        <Accordion type="single" collapsible className="mt-10 space-y-3">
          {faqs.map((f, i) => (
            <AccordionItem
              key={i}
              value={`item-${i}`}
              className="rounded-xl border bg-card px-5 shadow-card data-[state=open]:shadow-soft"
            >
              <AccordionTrigger className="text-left text-sm font-semibold hover:no-underline">
                {f.q}
              </AccordionTrigger>
              <AccordionContent className="text-sm text-muted-foreground">
                {f.a}
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </div>
    </section>
  );
}
