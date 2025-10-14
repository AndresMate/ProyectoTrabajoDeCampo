"use client";

import { useEffect, useState } from "react";
import { tournamentsService } from "@/services/tournamentsService";
import { Swiper, SwiperSlide } from "swiper/react";
import { Navigation, Pagination, Autoplay } from "swiper/modules";

export default function TournamentCarousel() {
  const [tournaments, setTournaments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTournaments = async () => {
      try {
        const data = await tournamentsService.getActive();
        setTournaments(data);
      } catch (error) {
        console.error("Error cargando torneos:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchTournaments();
  }, []);

  if (loading) return <p className="text-center py-10">Cargando torneos...</p>;

  if (tournaments.length === 0)
    return <p className="text-center py-10 text-gray-500">No hay torneos activos.</p>;

  return (
    <div className="w-full max-w-5xl mx-auto py-10">
      <h2 className="text-2xl font-bold text-center mb-6">Torneos en Inscripción</h2>

      <Swiper
        modules={[Navigation, Pagination, Autoplay]}
        spaceBetween={30}
        slidesPerView={1}
        navigation
        pagination={{ clickable: true }}
        autoplay={{ delay: 4000 }}
        className="rounded-lg shadow-lg"
      >
        {tournaments.map((t) => (
          <SwiperSlide key={t.id}>
            <div
              className="relative h-64 bg-cover bg-center rounded-lg"
              style={{ backgroundImage: `url(${t.imageUrl || "/default-tournament.jpg"})` }}
            >
              <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                <div className="text-center text-white">
                  <h3 className="text-3xl font-semibold">{t.name}</h3>
                  <p className="mt-2">{t.description}</p>
                  <p className="mt-1 text-sm italic">
                    {t.status === "ACTIVE" ? "Inscripciones abiertas" : "En curso"}
                  </p>
                </div>
              </div>
            </div>
          </SwiperSlide>
        ))}
      </Swiper>
    </div>
  );
}