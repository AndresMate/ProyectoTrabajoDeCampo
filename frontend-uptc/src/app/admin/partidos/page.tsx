'use client';

import { useEffect, useState } from 'react';
import matchesService from '@/services/matchesService';
import Modal from '@/components/Modal';
import MatchForm from '@/components/forms/MatchForm';
import FixtureGenerator from '@/components/FixtureGenerator';

interface Match {
  id: number;
  tournament?: { id: number; name: string } | null;
  category?: { id: number; name: string } | null;
  teamA?: { id: number; name: string } | null;
  teamB?: { id: number; name: string } | null;
  matchDate?: string | null;
  status: string;
  venue?: { name: string } | null;
  scenario?: { name: string } | null;
}

export default function AdminPartidosPage() {
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [showModal, setShowModal] = useState(false);
  const [showFixtureGenerator, setShowFixtureGenerator] = useState(false);

  useEffect(() => {
    fetchMatches();
  }, []);

  const fetchMatches = async () => {
    setLoading(true);
    try {
      const data = await matchesService.getAll();
      setMatches(data || []);
    } catch (error) {
      console.error('❌ Error al cargar partidos:', error);
      alert('Error al cargar los partidos');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Estás seguro de eliminar este partido?')) return;

    try {
      await matchesService.delete(id);
      alert('Partido eliminado exitosamente');
      fetchMatches();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Error al eliminar partido');
    }
  };

  const getStatusBadge = (status: string) => {
    const colors: Record<string, string> = {
      SCHEDULED: 'bg-blue-100 text-blue-700',
      IN_PROGRESS: 'bg-green-100 text-green-700',
      FINISHED: 'bg-gray-100 text-gray-700',
      CANCELLED: 'bg-red-100 text-red-700'
    };
    return colors[status] || 'bg-gray-100 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts: Record<string, string> = {
      SCHEDULED: 'Programado',
      IN_PROGRESS: 'En curso',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado'
    };
    return texts[status] || status;
  };

  const filteredMatches =
    filterStatus === 'ALL'
      ? matches
      : matches.filter((m) => m.status === filterStatus);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      {/* Título y acciones */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Partidos</h1>
        <div className="flex gap-3">
          <button
            onClick={() => setShowFixtureGenerator(true)}
            className="bg-purple-600 text-white px-6 py-2 rounded-lg hover:bg-purple-700 transition font-semibold"
          >
            ⚡ Generar Fixture
          </button>
          <button
            onClick={() => setShowModal(true)}
            className="bg-uptc-black text-uptc-yellow px-6 py-2 rounded-lg hover:bg-gray-800 transition font-semibold"
          >
            + Nuevo Partido
          </button>
        </div>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Total Partidos</div>
          <div className="text-2xl font-bold text-gray-900">{matches.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Programados</div>
          <div className="text-2xl font-bold text-blue-600">
            {matches.filter((m) => m.status === 'SCHEDULED').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">En Curso</div>
          <div className="text-2xl font-bold text-green-600">
            {matches.filter((m) => m.status === 'IN_PROGRESS').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Finalizados</div>
          <div className="text-2xl font-bold text-gray-600">
            {matches.filter((m) => m.status === 'FINISHED').length}
          </div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex gap-2">
          {['ALL', 'SCHEDULED', 'IN_PROGRESS', 'FINISHED'].map((status) => (
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

      {/* Lista de partidos */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-uptc-black">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">
                Partido
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">
                Torneo
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">
                Fecha
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">
                Lugar
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">
                Estado
              </th>
              <th className="px-6 py-3 text-right text-xs font-semibold text-uptc-yellow uppercase">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredMatches.map((match) => (
              <tr key={match.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-gray-900">
                      {match.teamA?.name || 'Equipo A'}
                    </span>
                    <span className="mx-4 text-gray-600 font-bold">vs</span>
                    <span className="font-semibold text-gray-900">
                      {match.teamB?.name || 'Equipo B'}
                    </span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900 font-medium">
                    {match.tournament?.name || 'Sin torneo'}
                  </div>
                  <div className="text-sm text-gray-600">
                    {match.category?.name || 'Sin categoría'}
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-gray-700 font-medium">
                  {match.matchDate
                    ? new Date(match.matchDate).toLocaleString('es-ES', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                      })
                    : 'Fecha no definida'}
                </td>
                <td className="px-6 py-4 text-sm text-gray-700 font-medium">
                  <div>{match.venue?.name || 'N/A'}</div>
                  <div className="text-xs text-gray-500">
                    {match.scenario?.name || ''}
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span
                    className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getStatusBadge(
                      match.status
                    )}`}
                  >
                    {getStatusText(match.status)}
                  </span>
                </td>
                <td className="px-6 py-4 text-right text-sm font-semibold">
                  {match.status === 'SCHEDULED' && (
                    <>
                      <button className="text-green-600 hover:text-green-900 mr-3">
                        ▶️ Iniciar
                      </button>
                      <button className="text-indigo-600 hover:text-indigo-900 mr-3">
                        ✏️ Editar
                      </button>
                      <button
                        onClick={() => handleDelete(match.id)}
                        className="text-red-600 hover:text-red-900"
                      >
                        🗑️ Eliminar
                      </button>
                    </>
                  )}
                  {match.status === 'FINISHED' && (
                    <button className="text-blue-600 hover:text-blue-800 font-semibold">
                      📊 Ver resultado
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredMatches.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center mt-6">
          <p className="text-gray-500 font-medium">
            No hay partidos con este estado
          </p>
        </div>
      )}

      {/* Modal crear partido */}
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="Crear Nuevo Partido"
        size="lg"
      >
        <MatchForm
          onSuccess={() => {
            setShowModal(false);
            fetchMatches();
          }}
          onCancel={() => setShowModal(false)}
        />
      </Modal>

      {/* Modal generador de fixture */}
      {showFixtureGenerator && (
        <FixtureGenerator
          onClose={() => setShowFixtureGenerator(false)}
          onSuccess={() => {
            setShowFixtureGenerator(false);
            fetchMatches();
          }}
        />
      )}
    </div>
  );
}