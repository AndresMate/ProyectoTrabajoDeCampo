// frontend-uptc/src/app/admin/partidos/page.tsx - VERSIÓN COMPLETA
'use client';

import { useEffect, useState } from 'react';
import matchesService from '@/services/matchesService';
import Modal from '@/components/Modal';
import MatchForm from '@/components/forms/MatchForm';
import FixtureGenerator from '@/components/FixtureGenerator';

interface Match {
  id: number;
  tournament: { id: number; name: string };
  category: { id: number; name: string };
  teamA: { id: number; name: string };
  teamB: { id: number; name: string };
  matchDate: string;
  status: string;
  venue?: { name: string };
  scenario?: { name: string };
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
    try {
      const data = await matchesService.getAll();
      setMatches(data);
    } catch (error) {
      console.error('Error al cargar partidos:', error);
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
    const colors: any = {
      SCHEDULED: 'bg-blue-100 text-blue-700',
      IN_PROGRESS: 'bg-green-100 text-green-700',
      FINISHED: 'bg-gray-100 text-gray-700',
      CANCELLED: 'bg-red-100 text-red-700'
    };
    return colors[status] || 'bg-gray-100 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts: any = {
      SCHEDULED: 'Programado',
      IN_PROGRESS: 'En curso',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado'
    };
    return texts[status] || status;
  };

  const filteredMatches = filterStatus === 'ALL'
    ? matches
    : matches.filter(m => m.status === filterStatus);

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
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Partidos</h1>
        <div className="flex gap-3">
          <button
            onClick={() => setShowFixtureGenerator(true)}
            className="bg-purple-600 text-white px-6 py-2 rounded-lg hover:bg-purple-700 transition"
          >
            ⚡ Generar Fixture
          </button>
          <button
            onClick={() => setShowModal(true)}
            className="bg-uptc-black text-white px-6 py-2 rounded-lg hover:bg-gray-800 transition"
          >
            + Nuevo Partido
          </button>
        </div>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Total Partidos</div>
          <div className="text-2xl font-bold text-gray-900">{matches.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Programados</div>
          <div className="text-2xl font-bold text-blue-600">
            {matches.filter(m => m.status === 'SCHEDULED').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">En Curso</div>
          <div className="text-2xl font-bold text-green-600">
            {matches.filter(m => m.status === 'IN_PROGRESS').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Finalizados</div>
          <div className="text-2xl font-bold text-gray-600">
            {matches.filter(m => m.status === 'FINISHED').length}
          </div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex gap-2">
          {['ALL', 'SCHEDULED', 'IN_PROGRESS', 'FINISHED'].map(status => (
            <button
              key={status}
              onClick={() => setFilterStatus(status)}
              className={`px-4 py-2 rounded-lg transition ${
                filterStatus === status
                  ? 'bg-uptc-black text-white'
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
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Partido</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Torneo</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Fecha</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Lugar</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Acciones</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredMatches.map((match) => (
              <tr key={match.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-gray-900">{match.teamA.name}</span>
                    <span className="mx-4 text-gray-500">vs</span>
                    <span className="font-medium text-gray-900">{match.teamB.name}</span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">{match.tournament.name}</div>
                  <div className="text-sm text-gray-500">{match.category.name}</div>
                </td>
                <td className="px-6 py-4 text-sm text-gray-700">
                  {new Date(match.matchDate).toLocaleString('es-ES', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </td>
                <td className="px-6 py-4 text-sm text-gray-700">
                  <div>{match.venue?.name || 'N/A'}</div>
                  <div className="text-xs text-gray-500">{match.scenario?.name || ''}</div>
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getStatusBadge(match.status)}`}>
                    {getStatusText(match.status)}
                  </span>
                </td>
                <td className="px-6 py-4 text-right text-sm font-medium">
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
                    <button className="text-blue-600 hover:text-uptc-black">
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
          <p className="text-gray-500">No hay partidos con este estado</p>
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