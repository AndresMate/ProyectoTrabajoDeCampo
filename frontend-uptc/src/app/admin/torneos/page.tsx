// src/app/admin/torneos/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { tournamentsService } from '@/services/tournamentsService';
import Modal from '@/components/Modal';
import TournamentForm from '@/components/forms/TournamentForm';
import Link from 'next/link';
import { toastSuccess, toastPromise } from '@/utils/toast';

interface Tournament {
  id: number;
  name: string;
  description?: string;
  startDate: string;
  endDate: string;
  status: string;
  sport: {
    name: string;
  };
  category?: {
    name: string;
  };
}

function getErrorMessage(err: unknown): string {
  if (err instanceof Error) return err.message;
  if (typeof err === 'object' && err !== null && 'response' in err) {
    const maybeResp = (err as { response?: { data?: { message?: string } } }).response;
    return maybeResp?.data?.message ?? String(err);
  }
  return String(err);
}

export default function AdminTorneosPage() {
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedTournament, setSelectedTournament] = useState<number | undefined>();
  const [filterStatus, setFilterStatus] = useState<string>('ALL');

  useEffect(() => {
    fetchTournaments();
  }, []);

  const fetchTournaments = async () => {
    try {
      const data = await tournamentsService.getAll(0, 100);
      setTournaments(data);
    } catch (error: unknown) {
      console.error('Error al cargar torneos:', getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  const handleChangeStatus = async (id: number, action: 'start' | 'complete' | 'cancel') => {
    try {
      let promise;
      if (action === 'start') promise = tournamentsService.startTournament(id);
      else if (action === 'complete') promise = tournamentsService.completeTournament(id);
      else promise = tournamentsService.cancelTournament(id);

      await toastPromise(
        promise!,
        {
          loading: 'Actualizando estado...',
          success: 'Estado actualizado exitosamente',
          error: (error: unknown) => getErrorMessage(error) || 'Error al cambiar el estado'
        }
      );
      fetchTournaments();
    } catch (error: unknown) {
      // El error ya se muestra en el toastPromise
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await toastPromise(
        tournamentsService.delete(id),
        {
          loading: 'Eliminando torneo...',
          success: 'Torneo eliminado exitosamente',
          error: (error: unknown) => getErrorMessage(error) || 'Error al eliminar el torneo'
        }
      );
      fetchTournaments();
    } catch (error: unknown) {
      // El error ya se muestra en el toastPromise
    }
  };

  const getStatusBadge = (status: string) => {
    const colors = {
      PLANNING: 'bg-gray-200 text-gray-700',
      REGISTRATION: 'bg-blue-200 text-blue-700',
      OPEN_FOR_INSCRIPTION: 'bg-blue-200 text-blue-700',
      IN_PROGRESS: 'bg-green-200 text-green-700',
      FINISHED: 'bg-purple-200 text-purple-700',
      CANCELLED: 'bg-red-200 text-red-700'
    };
    return colors[status as keyof typeof colors] || 'bg-gray-200 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts = {
      PLANNING: 'Planificación',
      OPEN_FOR_INSCRIPTION: 'Inscripción',
      IN_PROGRESS: 'En curso',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado'
    };
    return texts[status as keyof typeof texts] || status;
  };

  const filteredTournaments = filterStatus === 'ALL'
    ? tournaments
    : tournaments.filter(t => t.status === filterStatus);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Torneos</h1>
        <button
          onClick={() => {
            setSelectedTournament(undefined);
            setShowModal(true);
          }}
          className="bg-uptc-black text-uptc-yellow px-6 py-2 rounded-lg hover:bg-gray-800 transition font-semibold"
        >
          + Nuevo Torneo
        </button>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Total</div>
          <div className="text-2xl font-bold text-gray-900">{tournaments.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Planificación</div>
          <div className="text-2xl font-bold text-gray-600">
            {tournaments.filter(t => t.status === 'PLANNING').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Inscripción</div>
          <div className="text-2xl font-bold text-blue-600">
            {tournaments.filter(t => t.status === 'OPEN_FOR_INSCRIPTION').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">En Curso</div>
          <div className="text-2xl font-bold text-green-600">
            {tournaments.filter(t => t.status === 'IN_PROGRESS').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Finalizados</div>
          <div className="text-2xl font-bold text-purple-600">
            {tournaments.filter(t => t.status === 'FINISHED').length}
          </div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex flex-wrap gap-2">
          {['ALL', 'PLANNING', 'OPEN_FOR_INSCRIPTION', 'IN_PROGRESS', 'FINISHED'].map(status => (
            <button
              key={status}
              onClick={() => setFilterStatus(status)}
              className={`px-4 py-2 rounded-lg transition font-semibold ${
                filterStatus === status
                  ? 'bg-uptc-black text-uptc-yellow'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {status === 'ALL' ? 'Todos' : getStatusText(status)}
            </button>
          ))}
        </div>
      </div>

      {/* Lista de torneos */}
      {filteredTournaments.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500">No hay torneos con este estado</p>
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {filteredTournaments.map((tournament) => (
            <div
              key={tournament.id}
              className="bg-white rounded-lg shadow hover:shadow-lg transition"
            >
              <div className="p-6">
                <div className="flex justify-between items-start mb-4">
                  <h3 className="text-xl font-semibold text-gray-800 flex-1">
                    {tournament.name}
                  </h3>
                  <span
                    className={`text-xs px-3 py-1 rounded-full font-semibold ${getStatusBadge(
                      tournament.status
                    )}`}
                  >
                    {getStatusText(tournament.status)}
                  </span>
                </div>

                <p className="text-gray-600 text-sm mb-4 line-clamp-2">
                  {tournament.description || 'Sin descripción'}
                </p>

                <div className="space-y-2 text-sm text-gray-700 mb-4 font-medium">
                  <p>
                    <strong className="text-gray-900">Deporte:</strong> {tournament.sport?.name || 'N/A'}
                  </p>
                  <p>
                    <strong className="text-gray-900">Inicio:</strong>{' '}
                    {new Date(tournament.startDate).toLocaleDateString('es-ES')}
                  </p>
                  <p>
                    <strong className="text-gray-900">Fin:</strong>{' '}
                    {new Date(tournament.endDate).toLocaleDateString('es-ES')}
                  </p>
                </div>

                {/* Acciones según estado */}
                <div className="space-y-2">
                  {tournament.status === 'PLANNING' && (
                    <button
                      onClick={() => handleChangeStatus(tournament.id, 'start')}
                      className="w-full bg-green-600 text-white py-2 rounded hover:bg-green-700 transition text-sm font-semibold"
                    >
                      Iniciar Torneo
                    </button>
                  )}

                  {tournament.status === 'IN_PROGRESS' && (
                    <button
                      onClick={() => handleChangeStatus(tournament.id, 'complete')}
                      className="w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700 transition text-sm font-semibold"
                    >
                      Finalizar Torneo
                    </button>
                  )}

                  <div className="flex gap-2">
                    {/* Redirige a la pestaña de equipos del torneo */}
                    <Link
                      href={`/admin/torneos/${tournament.id}/equipos`}
                      className="flex-1 bg-uptc-black text-uptc-yellow text-center py-2 rounded hover:bg-gray-800 transition text-sm font-semibold"
                    >
                      Ver detalles
                    </Link>

                    <button
                      onClick={() => {
                        setSelectedTournament(tournament.id);
                        setShowModal(true);
                      }}
                      className="px-4 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition font-semibold"
                      title="Editar"
                    >
                      ✏️
                    </button>

                    {tournament.status === 'PLANNING' && (
                      <button
                        onClick={() => handleDelete(tournament.id)}
                        className="px-4 bg-red-600 text-white rounded hover:bg-red-700 transition font-semibold"
                        title="Eliminar"
                      >
                        🗑️
                      </button>
                    )}

                    {(tournament.status === 'PLANNING' || tournament.status === 'REGISTRATION') && (
                      <button
                        onClick={() => handleChangeStatus(tournament.id, 'cancel')}
                        className="px-4 bg-gray-600 text-white rounded hover:bg-gray-700 transition font-semibold"
                        title="Cancelar"
                      >
                        ✖️
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal para crear/editar torneo */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setSelectedTournament(undefined);
        }}
        title={selectedTournament ? 'Editar Torneo' : 'Crear Nuevo Torneo'}
        size="lg"
      >
        <TournamentForm
          tournamentId={selectedTournament}
          onSuccess={() => {
            setShowModal(false);
            setSelectedTournament(undefined);
            fetchTournaments();
          }}
          onCancel={() => {
            setShowModal(false);
            setSelectedTournament(undefined);
          }}
        />
      </Modal>
    </div>
  );
}