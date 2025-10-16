'use client';

import { useEffect, useState } from 'react';
import { tournamentsService } from '@/services/tournamentsService';

interface Tournament {
  id: number;
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  status: string;
  sport: {
    name: string;
  };
  category: {
    name: string;
  };
}

export default function AdminTorneosPage() {
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    fetchTournaments();
  }, []);

  const fetchTournaments = async () => {
    try {
      const data = await tournamentsService.getAll();
      setTournaments(data);
    } catch (error) {
      console.error('Error al cargar torneos:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const colors = {
      PLANNING: 'bg-gray-200 text-gray-700',
      REGISTRATION: 'bg-blue-200 text-blue-700',
      IN_PROGRESS: 'bg-green-200 text-green-700',
      FINISHED: 'bg-purple-200 text-purple-700',
      CANCELLED: 'bg-red-200 text-red-700'
    };
    return colors[status as keyof typeof colors] || 'bg-gray-200 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts = {
      PLANNING: 'Planificación',
      REGISTRATION: 'Inscripción',
      IN_PROGRESS: 'En curso',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado'
    };
    return texts[status as keyof typeof texts] || status;
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-900"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Torneos</h1>
        <button
          onClick={() => setShowModal(true)}
          className="bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800 transition"
        >
          + Nuevo Torneo
        </button>
      </div>

      {tournaments.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500">No hay torneos registrados</p>
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {tournaments.map((tournament) => (
            <div
              key={tournament.id}
              className="bg-white rounded-lg shadow hover:shadow-lg transition p-6"
            >
              <div className="flex justify-between items-start mb-4">
                <h3 className="text-xl font-semibold text-gray-800 flex-1">
                  {tournament.name}
                </h3>
                <span
                  className={`text-xs px-3 py-1 rounded-full ${getStatusBadge(
                    tournament.status
                  )}`}
                >
                  {getStatusText(tournament.status)}
                </span>
              </div>

              <p className="text-gray-600 text-sm mb-4 line-clamp-2">
                {tournament.description}
              </p>

              <div className="space-y-2 text-sm text-gray-600 mb-4">
                <p>
                  <strong>Deporte:</strong> {tournament.sport?.name || 'N/A'}
                </p>
                <p>
                  <strong>Categoría:</strong> {tournament.category?.name || 'N/A'}
                </p>
                <p>
                  <strong>Inicio:</strong>{' '}
                  {new Date(tournament.startDate).toLocaleDateString()}
                </p>
                <p>
                  <strong>Fin:</strong>{' '}
                  {new Date(tournament.endDate).toLocaleDateString()}
                </p>
              </div>

              <div className="flex gap-2">
                <button className="flex-1 bg-blue-900 text-white py-2 rounded hover:bg-blue-800 transition text-sm">
                  Ver detalles
                </button>
                <button className="px-4 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition">
                  ⋯
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal para crear torneo (placeholder) */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 max-w-2xl w-full">
            <h2 className="text-2xl font-bold mb-4">Crear Nuevo Torneo</h2>
            <p className="text-gray-600 mb-4">Formulario de creación (por implementar)</p>
            <button
              onClick={() => setShowModal(false)}
              className="bg-gray-300 px-4 py-2 rounded hover:bg-gray-400"
            >
              Cerrar
            </button>
          </div>
        </div>
      )}
    </div>
  );
}