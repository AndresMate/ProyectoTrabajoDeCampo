'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import matchesService, { Match } from '@/services/matchesService';
import Modal from '@/components/Modal';
import MatchForm from '@/components/forms/MatchForm';
import FixtureGenerator from '@/components/FixtureGenerator';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function AdminPartidosPage() {
  const router = useRouter();
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [showMatchModal, setShowMatchModal] = useState<boolean>(false);
  const [editMatchId, setEditMatchId] = useState<number | null>(null);
  const [showFixtureGenerator, setShowFixtureGenerator] = useState<boolean>(false);

  useEffect(() => {
    fetchMatches();
  }, []);

  const fetchMatches = async () => {
    setLoading(true);
    try {
      const data = await matchesService.getAll();
      setMatches(data);
    } catch (error) {
      console.error('Error al cargar partidos:', error);
      alert('❌ Error al cargar partidos');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('🗑️ ¿Estás seguro de eliminar este partido?')) return;
    try {
      await matchesService.delete(id);
      alert('✅ Partido eliminado exitosamente');
      fetchMatches();
    } catch (error) {
      console.error('Error al eliminar partido:', error);
      alert('❌ Error al eliminar partido');
    }
  };

  const handleStartMatch = async (id: number, match: Match) => {
    if (!confirm(`⚽ ¿Deseas iniciar el partido ${match.homeTeam.name} vs ${match.awayTeam.name}?`)) return;
    try {
      await matchesService.startMatch(id);
      alert(`✅ Partido ${match.homeTeam.name} vs ${match.awayTeam.name} iniciado`);
      fetchMatches();
    } catch (error) {
      console.error('Error al iniciar partido:', error);
      alert('❌ Error al iniciar partido');
    }
  };

  const handleFinishMatch = async (id: number, match: Match) => {
    if (!confirm(`🏁 ¿Confirmas que el partido ${match.homeTeam.name} vs ${match.awayTeam.name} ha finalizado?`)) return;
    try {
      await matchesService.finishMatch(id);
      alert(`✅ Partido ${match.homeTeam.name} vs ${match.awayTeam.name} finalizado`);
      fetchMatches();
    } catch (error) {
      console.error('Error al finalizar partido:', error);
      alert('❌ Error al finalizar partido');
    }
  };

  const openEditModal = (matchId?: number) => {
    setEditMatchId(matchId ?? null);
    setShowMatchModal(true);
  };

  const filteredMatches = filterStatus === 'ALL'
    ? matches
    : matches.filter(m => m.status === filterStatus);

  const sortedMatches = [...filteredMatches].sort((a, b) => {
  const toTime = (v: string | Date | null | undefined) => {
    if (!v) return Number.POSITIVE_INFINITY;
    return typeof v === 'string' ? Date.parse(v) : (v instanceof Date ? v.getTime() : Number.POSITIVE_INFINITY);
  };

  const ta = toTime(a.startsAt as any);
  const tb = toTime(b.startsAt as any);

  return ta - tb;
  });


  const getStatusBadge = (status?: string) => {
    switch (status) {
      case 'SCHEDULED': return 'bg-blue-100 text-blue-700';
      case 'IN_PROGRESS': return 'bg-green-100 text-green-700 animate-pulse';
      case 'FINISHED': return 'bg-gray-100 text-gray-700';
      case 'CANCELLED': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const getStatusText = (status?: string) => {
    switch (status) {
      case 'SCHEDULED': return 'Programado';
      case 'IN_PROGRESS': return '🟢 En curso';
      case 'FINISHED': return '⚪ Finalizado';
      case 'CANCELLED': return '🔴 Cancelado';
      default: return status || 'N/A';
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Encabezado */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-800">Gestión de Partidos</h1>
          <p className="text-sm text-gray-600">Administra los partidos del sistema</p>
        </div>

        <div className="flex gap-3">
          <button
            onClick={() => setShowFixtureGenerator(true)}
            className="bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition font-semibold"
          >
            ⚡ Generar Fixture
          </button>

          <button
            onClick={() => openEditModal(undefined)}
            className="bg-uptc-black text-uptc-yellow px-4 py-2 rounded-lg hover:bg-gray-800 transition font-semibold"
          >
            + Nuevo Partido
          </button>
        </div>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4 text-center">
          <div className="text-sm text-gray-600 font-semibold">Total Partidos</div>
          <div className="text-2xl font-bold text-gray-900">{matches.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4 text-center">
          <div className="text-sm text-gray-600 font-semibold">Programados</div>
          <div className="text-2xl font-bold text-blue-600">
            {matches.filter(m => m.status === 'SCHEDULED').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4 text-center">
          <div className="text-sm text-gray-600 font-semibold">En Curso</div>
          <div className="text-2xl font-bold text-green-600">
            {matches.filter(m => m.status === 'IN_PROGRESS').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4 text-center">
          <div className="text-sm text-gray-600 font-semibold">Finalizados</div>
          <div className="text-2xl font-bold text-gray-900">
            {matches.filter(m => m.status === 'FINISHED').length}
          </div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex gap-2 flex-wrap">
          {['ALL', 'SCHEDULED', 'IN_PROGRESS', 'FINISHED', 'CANCELLED'].map(status => (
            <button
              key={status}
              onClick={() => setFilterStatus(status)}
              className={`px-3 py-2 rounded-lg text-sm font-semibold ${
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

      {/* Tabla */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-uptc-black">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">Partido</th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">Torneo</th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">Fecha</th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">Lugar</th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">Estado</th>
              <th className="px-6 py-3 text-right text-xs font-semibold text-uptc-yellow uppercase">Acciones</th>
            </tr>
          </thead>

          <tbody className="bg-white divide-y divide-gray-200">
            {sortedMatches.map(match => (
              <tr key={match.id} className="hover:bg-gray-50 transition">
                {/* Partido */}
                <td className="px-6 py-4">
                  <div className="flex flex-col">
                    <div className="font-semibold text-gray-900">
                      {match.homeTeam?.name || 'Equipo Local'}
                      <span className="mx-2 text-gray-500 font-normal">vs</span>
                      {match.awayTeam?.name || 'Equipo Visitante'}
                    </div>
                    <div className="text-sm text-gray-500">
                      {match.homeTeam?.club?.name && match.awayTeam?.club?.name
                        ? `${match.homeTeam.club.name} vs ${match.awayTeam.club.name}`
                        : 'Sin clubes asignados'}
                    </div>
                  </div>
                </td>

                {/* Torneo */}
                <td className="px-6 py-4">
                  <div className="text-sm font-medium text-gray-900">{match.tournament?.name || 'Sin torneo'}</div>
                  <div className="text-sm text-gray-600">{match.category?.name || 'Sin categoría'}</div>
                </td>

                {/* Fecha */}
                <td className="px-6 py-4 text-sm text-gray-700">
                  {match.startsAt
                    ? format(new Date(match.startsAt), "dd/MM/yyyy ' - ' HH:mm", { locale: es })
                    : <span className="text-gray-400">Fecha no definida</span>}
                </td>

                {/* Lugar */}
                <td className="px-6 py-4 text-sm text-gray-700">
                  {match.scenario?.name || 'N/A'}
                </td>

                {/* Estado */}
                <td className="px-6 py-4">
                  <span className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getStatusBadge(match.status)}`}>
                    {getStatusText(match.status)}
                  </span>
                </td>

                {/* Acciones */}
                <td className="px-6 py-4 text-right text-sm font-semibold">
                  <div className="flex items-center justify-end gap-3">
                    {(match.status === 'SCHEDULED' || match.status === 'IN_PROGRESS') && (
                      <button
                        onClick={() => router.push(`/admin/partidos/${match.id}/live`)}
                        className="text-purple-600 hover:text-purple-900 font-bold"
                      >
                        🎮 En Vivo
                      </button>
                    )}

                    {match.status === 'SCHEDULED' && (
                      <>
                        <button
                          onClick={() => handleStartMatch(match.id, match)}
                          className="text-green-600 hover:text-green-900"
                        >
                          ▶️ Iniciar
                        </button>
                        <button
                          onClick={() => openEditModal(match.id)}
                          className="text-indigo-600 hover:text-indigo-900"
                        >
                          ✏️ Editar
                        </button>
                      </>
                    )}

                    {match.status === 'IN_PROGRESS' && (
                      <button
                        onClick={() => handleFinishMatch(match.id, match)}
                        className="text-orange-600 hover:text-orange-900"
                      >
                        🏁 Finalizar
                      </button>
                    )}

                    {match.status === 'FINISHED' && (
                      <button
                        onClick={() => router.push(`/admin/partidos/${match.id}/live`)}
                        className="text-blue-600 hover:text-blue-800"
                      >
                        📊 Ver detalles
                      </button>
                    )}

                    <button
                      onClick={() => handleDelete(match.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      🗑️
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {sortedMatches.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center mt-6">
          <p className="text-gray-500 font-medium">No hay partidos con ese estado</p>
        </div>
      )}

      {/* Modal crear/editar partido */}
      <Modal
        isOpen={showMatchModal}
        onClose={() => {
          setShowMatchModal(false);
          setEditMatchId(null);
        }}
        title={editMatchId ? 'Editar Partido' : 'Crear Partido'}
        size="lg"
      >
        <MatchForm
          matchId={editMatchId ?? undefined}
          onSuccess={() => {
            setShowMatchModal(false);
            setEditMatchId(null);
            fetchMatches();
          }}
          onCancel={() => {
            setShowMatchModal(false);
            setEditMatchId(null);
          }}
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
