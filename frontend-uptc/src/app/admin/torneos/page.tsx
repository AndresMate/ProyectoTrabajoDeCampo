'use client';

import { useEffect, useState } from 'react';
import { tournamentsService } from '@/services/tournamentsService';
import Modal from '@/components/Modal';
import TournamentForm from '@/components/forms/TournamentForm';
import Link from 'next/link';

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
  category?: {
    name: string;
  };
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
    } catch (error) {
      console.error('Error al cargar torneos:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChangeStatus = async (id: number, action: 'start' | 'complete' | 'cancel') => {
    const confirmMessages = {
      start: '¿Iniciar este torneo?',
      complete: '¿Marcar este torneo como finalizado?',
      cancel: '¿Cancelar este torneo?'
    };

    if (!confirm(confirmMessages[action])) return;

    try {
      if (action === 'start') await tournamentsService.startTournament(id);
      if (action === 'complete') await tournamentsService.completeTournament(id);
      if (action === 'cancel') await tournamentsService.cancelTournament(id);

      alert('Estado actualizado exitosamente');
      fetchTournaments();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Error al cambiar el estado');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Estás seguro de eliminar este torneo? Esta acción no se puede deshacer.')) return;

    try {
      await tournamentsService.delete(id);
      alert('Torneo eliminado exitosamente');
      fetchTournaments();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Error al eliminar el torneo');
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
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-900"></div>
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
          className="bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800 transition"
        >
          + Nuevo Torneo
        </button>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Total</div>
          <div className="text-2xl font-bold text-gray-900">{tournaments.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Planificación</div>
          <div className="text-2xl font-bold text-gray-600">
            {tournaments.filter(t => t.status === 'PLANNING').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Inscripción</div>
          <div className="text-2xl font-bold text-blue-600">
            {tournaments.filter(t => t.status === 'OPEN_FOR_INSCRIPTION').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">En Curso</div>
          <div className="text-2xl font-bold text-green-600">
            {tournaments.filter(t => t.status === 'IN_PROGRESS').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Finalizados</div>
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
              className={`px-4 py-2 rounded-lg transition ${
                filterStatus === status
                  ? 'bg-blue-900 text-white'
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
                    <strong>Inicio:</strong>{' '}
                    {new Date(tournament.startDate).toLocaleDateString('es-ES')}
                  </p>
                  <p>
                    <strong>Fin:</strong>{' '}
                    {new Date(tournament.endDate).toLocaleDateString('es-ES')}
                  </p>
                </div>

                {/* Acciones según estado */}
                <div className="space-y-2">
                  {tournament.status === 'PLANNING' && (
                    <button
                      onClick={() => handleChangeStatus(tournament.id, 'start')}
                      className="w-full bg-green-600 text-white py-2 rounded hover:bg-green-700 transition text-sm"
                    >
                      Iniciar Torneo
                    </button>
                  )}

                  {tournament.status === 'IN_PROGRESS' && (
                    <button
                      onClick={() => handleChangeStatus(tournament.id, 'complete')}
                      className="w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700 transition text-sm"
                    >
                      Finalizar Torneo
                    </button>
                  )}

                  <div className="flex gap-2">
                    <Link
                      href={`/torneos/${tournament.id}`}
                      className="flex-1 bg-blue-900 text-white text-center py-2 rounded hover:bg-blue-800 transition text-sm"
                    >
                      Ver detalles
                    </Link>

                    <button
                      onClick={() => {
                        setSelectedTournament(tournament.id);
                        setShowModal(true);
                      }}
                      className="px-4 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition"
                      title="Editar"
                    >
                      ✏️
                    </button>

                    {tournament.status === 'PLANNING' && (
                      <button
                        onClick={() => handleDelete(tournament.id)}
                        className="px-4 bg-red-600 text-white rounded hover:bg-red-700 transition"
                        title="Eliminar"
                      >
                        🗑️
                      </button>
                    )}

                    {(tournament.status === 'PLANNING' || tournament.status === 'REGISTRATION') && (
                      <button
                        onClick={() => handleChangeStatus(tournament.id, 'cancel')}
                        className="px-4 bg-gray-600 text-white rounded hover:bg-gray-700 transition"
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