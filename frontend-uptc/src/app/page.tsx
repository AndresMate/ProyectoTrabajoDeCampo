'use client';

import TournamentCarousel from '@/components/home/TournamentCarousel';
import HeroSection from '@/components/home/HeroSection';
import InfoSection from '@/components/home/InfoSection';
import Footer from '@/components/home/Footer';

export default function HomePage() {
  return (
    <main className="flex flex-col items-center justify-center min-h-screen bg-gray-50 text-gray-900">
      <HeroSection />
      <section className="w-full max-w-6xl mt-10 px-4">
        <h2 className="text-2xl font-semibold mb-4 text-center">Torneos Activos</h2>
        <TournamentCarousel />
      </section>
      <InfoSection />
      <Footer />
    </main>
  );
}
