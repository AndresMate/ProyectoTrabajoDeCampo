'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';

export default function TournamentDetailPage() {
  const { id } = useParams();
  const [tournament, setTournament] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      const fetchData = async () => {
        try {
          const data = await tournamentsService.getById(id as string);
          setTournament(data);
        } catch (error) {
          console.error('Error al obtener torneo:', error);
        } finally {
          setLoading(false);
        }
      };
      fetchData();
    }
  }, [id]);

  if (loading) return <p className="text-center mt-10">Cargando información...</p>;

  if (!tournament)
    return (
      <p className="text-center text-red-600 mt-10">
        No se encontró información del torneo.
      </p>
    );

  return (
    <main className="flex flex-col items-center min-h-screen py-10 bg-gray-50 text-gray-900">
      <div className="max-w-4xl w-full bg-white p-8 rounded-lg shadow">
        <h1 className="text-3xl font-bold mb-4 text-center text-blue-700">
          {tournament.name}
        </h1>
        <p className="text-gray-600 mb-6 text-center italic">
          Estado: {tournament.status}
        </p>

        <div className="space-y-4 text-gray-700">
          <p>
            <strong>Descripción:</strong> {tournament.description || 'Sin descripción'}
          </p>
          <p>
            <strong>Fecha de inicio:</strong>{' '}
            {tournament.startDate || 'No definida'}
          </p>
          <p>
            <strong>Fecha de finalización:</strong>{' '}
            {tournament.endDate || 'No definida'}
          </p>
          <p>
            <strong>Categoría:</strong> {tournament.category || 'General'}
          </p>
        </div>
      </div>
    </main>
  );
}
