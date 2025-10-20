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
    const colors: Record<string, string> = {
      PLANNING: 'bg-gray-200 text-gray-700',
      OPEN_FOR_INSCRIPTION: 'bg-uptc-yellow text-uptc-black',
      IN_PROGRESS: 'bg-green-200 text-green-700',
      FINISHED: 'bg-purple-200 text-purple-700',
      CANCELLED: 'bg-red-200 text-red-700'
    };
    return colors[status] || 'bg-gray-200 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts: Record<string, string> = {
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
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  return (
    <main className="min-h-screen py-10 bg-gray-50">
      <div className="max-w-7xl mx-auto px-4">
        {/* Header */}
        <div className="mb-8 text-center">
          <div className="flex justify-center mb-4">
            <div className="w-16 h-16 bg-uptc-yellow rounded-full flex items-center justify-center shadow-lg">
              <span className="text-uptc-black font-bold text-2xl">U</span>
            </div>
          </div>
          <h1 className="text-4xl font-bold text-uptc-black mb-2">
            Torneos Deportivos UPTC
          </h1>
          <p className="text-gray-600">
            Encuentra y participa en los torneos de la Universidad
          </p>
        </div>

        {/* Filtros */}
        <div className="bg-white rounded-xl shadow-lg p-4 mb-6 border-2 border-gray-200">
          <div className="flex flex-wrap gap-2 justify-center">
            {['ALL', 'OPEN_FOR_INSCRIPTION', 'IN_PROGRESS', 'FINISHED'].map(status => (
              <button
                key={status}
                onClick={() => setFilter(status)}
                className={`px-6 py-2 rounded-lg font-semibold transition-all ${
                  filter === status
                    ? 'bg-uptc-black text-uptc-yellow shadow-lg scale-105'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {status === 'ALL' ? 'Todos' : getStatusText(status)}
              </button>
            ))}
          </div>
        </div>

        {/* Grid de torneos */}
        {filteredTorneos.length === 0 ? (
          <div className="bg-white rounded-xl shadow-lg p-12 text-center border-2 border-gray-200">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-4xl">🏆</span>
            </div>
            <p className="text-gray-500 text-lg font-semibold">No hay torneos disponibles</p>
          </div>
        ) : (
          <div className="grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
            {filteredTorneos.map((torneo) => (
              <div
                key={torneo.id}
                className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-2xl transition-all duration-300 border-2 border-gray-200 hover:border-uptc-yellow group"
              >
                {/* Header de la card */}
                <div className="bg-gradient-to-br from-uptc-black to-gray-800 p-6 text-white border-b-4 border-uptc-yellow">
                  <h2 className="text-xl font-bold mb-2 group-hover:text-uptc-yellow transition-colors">
                    {torneo.name}
                  </h2>
                  <p className="text-sm text-gray-300">
                    {/* ✅ CORRECCIÓN: Acceder a .name de los objetos */}
                    {torneo.sport?.name || 'Deporte'} • {torneo.category?.name || 'Categoría'}
                  </p>
                </div>

                {/* Contenido */}
                <div className="p-6">
                  <p className="text-gray-600 text-sm mb-4 line-clamp-3">
                    {torneo.description}
                  </p>

                  <div className="space-y-2 text-sm text-gray-600 mb-4">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-uptc-black">📅 Inicio:</span>
                      {new Date(torneo.startDate).toLocaleDateString('es-ES', {
                        day: 'numeric',
                        month: 'long',
                        year: 'numeric'
                      })}
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-uptc-black">🏁 Fin:</span>
                      {new Date(torneo.endDate).toLocaleDateString('es-ES', {
                        day: 'numeric',
                        month: 'long',
                        year: 'numeric'
                      })}
                    </div>
                  </div>

                  {/* Estado */}
                  <div className="mb-4">
                    <span
                      className={`inline-block px-4 py-2 rounded-full text-sm font-bold ${getStatusBadge(
                        torneo.status
                      )}`}
                    >
                      {getStatusText(torneo.status)}
                    </span>
                  </div>

                  {/* Botones */}
                  <div className="flex gap-2">
                    <Link
                      href={`/torneos/${torneo.id}`}
                      className="flex-1 bg-uptc-black text-uptc-yellow py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors text-center"
                    >
                      Ver detalles
                    </Link>
                    {torneo.status === 'OPEN_FOR_INSCRIPTION' && (
                      <Link
                        href={`/torneos/${torneo.id}/inscribirse`}
                        className="px-4 bg-uptc-yellow text-uptc-black rounded-lg font-semibold hover:bg-yellow-400 transition-colors flex items-center justify-center"
                        title="Inscribirse"
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

        {/* Estadísticas */}
        <div className="mt-12 grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="bg-white rounded-xl shadow-lg p-6 text-center border-2 border-uptc-yellow">
            <div className="text-3xl font-bold text-uptc-black mb-1">{torneos.length}</div>
            <div className="text-sm text-gray-600 font-semibold">Total Torneos</div>
          </div>
          <div className="bg-white rounded-xl shadow-lg p-6 text-center border-2 border-gray-200">
            <div className="text-3xl font-bold text-uptc-yellow mb-1">
              {torneos.filter(t => t.status === 'OPEN_FOR_INSCRIPTION').length}
            </div>
            <div className="text-sm text-gray-600 font-semibold">Inscripciones Abiertas</div>
          </div>
          <div className="bg-white rounded-xl shadow-lg p-6 text-center border-2 border-gray-200">
            <div className="text-3xl font-bold text-green-600 mb-1">
              {torneos.filter(t => t.status === 'IN_PROGRESS').length}
            </div>
            <div className="text-sm text-gray-600 font-semibold">En Curso</div>
          </div>
          <div className="bg-white rounded-xl shadow-lg p-6 text-center border-2 border-gray-200">
            <div className="text-3xl font-bold text-purple-600 mb-1">
              {torneos.filter(t => t.status === 'FINISHED').length}
            </div>
            <div className="text-sm text-gray-600 font-semibold">Finalizados</div>
          </div>
        </div>
      </div>
    </main>
  );
}