// File: src/app/torneos/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { tournamentsService } from '@/services/tournamentsService';
import Link from 'next/link';

interface Tournament {
  id: number;
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  status: string;
  sport?: { name: string };
  category?: { name: string };
}

export default function TorneosPage() {
  const [torneos, setTorneos] = useState<Tournament[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<string>('ALL');

  useEffect(() => {
    fetchTorneos();
  }, []);

  const fetchTorneos = async () => {
    try {
      const data = await tournamentsService.getAll();
      setTorneos(data);
    } catch (error) {
      console.error('Error al cargar torneos:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const colors: any = {
      PLANNING: 'bg-gray-200 text-gray-700',
      REGISTRATION: 'bg-blue-200 text-blue-700',
      IN_PROGRESS: 'bg-green-200 text-green-700',
      FINISHED: 'bg-purple-200 text-purple-700',
      CANCELLED: 'bg-red-200 text-red-700'
    };
    return colors[status] || 'bg-gray-200 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts: any = {
      PLANNING: 'Planificación',
      OPEN_FOR_INSCRIPTION: 'Inscripciones Abiertas',
      IN_PROGRESS: 'En Curso',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado'
    };
    return texts[status] || status;
  };

  const filteredTorneos = filter === 'ALL'
    ? torneos
    : torneos.filter(t => t.status === filter);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-900"></div>
      </div>
    );
  }

  return (
    <main className="min-h-screen py-10 bg-gray-50">
      <div className="max-w-7xl mx-auto px-4">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-blue-900 mb-2">Torneos Deportivos</h1>
          <p className="text-gray-600">Encuentra y participa en los torneos de la UPTC</p>
        </div>

        <div className="bg-white rounded-lg shadow p-4 mb-6">
          <div className="flex flex-wrap gap-2">
            {['ALL', 'OPEN_FOR_INSCRIPTION', 'IN_PROGRESS', 'FINISHED'].map(status => (
              <button
                key={status}
                onClick={() => setFilter(status)}
                className={`px-4 py-2 rounded-lg transition ${
                  filter === status
                    ? 'bg-blue-900 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {status === 'ALL' ? 'Todos' : getStatusText(status)}
              </button>
            ))}
          </div>
        </div>

        {filteredTorneos.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-8 text-center">
            <p className="text-gray-500 text-lg">No hay torneos disponibles</p>
          </div>
        ) : (
          <div className="grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
            {filteredTorneos.map((torneo) => (
              <div
                key={torneo.id}
                className="bg-white shadow-lg rounded-lg overflow-hidden hover:shadow-xl transition"
              >
                <div className="bg-gradient-to-r from-blue-900 to-blue-700 p-4 text-white">
                  <h2 className="text-xl font-semibold mb-1">{torneo.name}</h2>
                  <p className="text-sm text-blue-100">
                    {torneo.sport?.name || 'Deporte'} • {torneo.category?.name || 'Categoría'}
                  </p>
                </div>

                <div className="p-6">
                  <p className="text-gray-600 text-sm mb-4 line-clamp-3">
                    {torneo.description}
                  </p>

                  <div className="space-y-2 text-sm text-gray-600 mb-4">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">📅 Inicio:</span>
                      {new Date(torneo.startDate).toLocaleDateString('es-ES', {
                        day: 'numeric',
                        month: 'long',
                        year: 'numeric'
                      })}
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="font-medium">🏁 Fin:</span>
                      {new Date(torneo.endDate).toLocaleDateString('es-ES', {
                        day: 'numeric',
                        month: 'long',
                        year: 'numeric'
                      })}
                    </div>
                  </div>

                  <div className="mb-4">
                    <span
                      className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getStatusBadge(
                        torneo.status
                      )}`}
                    >
                      {getStatusText(torneo.status)}
                    </span>
                  </div>

                  <div className="flex gap-2">
                    <Link
                      href={`/torneos/${torneo.id}`}
                      className="flex-1 bg-blue-900 text-white text-center py-2 rounded-lg hover:bg-blue-800 transition font-medium"
                    >
                      Ver detalles
                    </Link>
                    {torneo.status === 'REGISTRATION' && (
                      <Link
                        href={`/torneos/${torneo.id}/inscribirse`}
                        className="px-4 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-medium flex items-center justify-center"
                      >
                        📝
                      </Link>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="mt-8 grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="bg-white rounded-lg shadow p-4 text-center">
            <div className="text-2xl font-bold text-blue-900">{torneos.length}</div>
            <div className="text-sm text-gray-600">Total Torneos</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4 text-center">
            <div className="text-2xl font-bold text-blue-600">
              {torneos.filter(t => t.status === 'REGISTRATION').length}
            </div>
            <div className="text-sm text-gray-600">Inscripciones Abiertas</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4 text-center">
            <div className="text-2xl font-bold text-green-600">
              {torneos.filter(t => t.status === 'IN_PROGRESS').length}
            </div>
            <div className="text-sm text-gray-600">En Curso</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4 text-center">
            <div className="text-2xl font-bold text-purple-600">
              {torneos.filter(t => t.status === 'FINISHED').length}
            </div>
            <div className="text-sm text-gray-600">Finalizados</div>
          </div>
        </div>
      </div>
    </main>
  );
}
