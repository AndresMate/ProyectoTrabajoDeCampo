// src/app/admin/torneos/[id]/inscripciones/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';
import inscriptionsService from '@/services/inscriptionsService';
import TournamentTabs from '@/components/TournamentTabs';
import PlayerSelectionModal from '@/components/PlayerSelectionModal';
import InscriptionDetailsModal from '@/components/InscriptionDetailsModal';
import PermissionGate from '@/components/PermissionGate';
import { getStatusBadge, getStatusText } from '@/utils/inscriptionStatusUtils';
import { toastPromise } from '@/utils/toast';

type FilterStatus = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED';

export default function TournamentInscriptionsPage() {
  const params = useParams();
  const tournamentId = Number(params.id);

  const [tournament, setTournament] = useState<any>(null);
  const [inscriptions, setInscriptions] = useState<any[]>([]);
  const [filteredInscriptions, setFilteredInscriptions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<FilterStatus>('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const [showPlayersModal, setShowPlayersModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedInscriptionId, setSelectedInscriptionId] = useState<number | null>(null);

  useEffect(() => {
    fetchData();
  }, [tournamentId]);

  useEffect(() => {
    applyFilters();
  }, [inscriptions, filterStatus, searchTerm]);

  const fetchData = async () => {
    try {
      setLoading(true);

      const [tournamentData, inscriptionsData] = await Promise.all([
        tournamentsService.getById(tournamentId),
        inscriptionsService.getByTournamentId(tournamentId)
      ]);

      setTournament(tournamentData);
      setInscriptions(inscriptionsData);
    } catch (error) {
      console.error('Error al cargar inscripciones:', error);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let result = [...inscriptions];

    if (filterStatus !== 'ALL') {
      result = result.filter(i => i.status === filterStatus);
    }

    if (searchTerm.trim()) {
      const search = searchTerm.toLowerCase();
      result = result.filter(i =>
        i.teamName?.toLowerCase().includes(search) ||
        i.delegateName?.toLowerCase().includes(search) ||
        i.category?.name?.toLowerCase().includes(search)
      );
    }

    setFilteredInscriptions(result);
  };

  const handleViewPlayers = (inscriptionId: number) => {
    setSelectedInscriptionId(inscriptionId);
    setShowPlayersModal(true);
  };

  const handleViewDetails = (inscriptionId: number) => {
    setSelectedInscriptionId(inscriptionId);
    setShowDetailsModal(true);
  };

  const handleQuickApprove = async (inscriptionId: number) => {
    try {
      await toastPromise(
        inscriptionsService.approve(inscriptionId),
        {
          loading: 'Aprobando inscripción...',
          success: 'Inscripción aprobada exitosamente',
          error: (error: any) => error.response?.data?.message || 'Error al aprobar la inscripción'
        }
      );
      fetchData();
    } catch (error: any) {
      console.error('Error al aprobar:', error);
    }
  };

  const getStatusCount = (status: FilterStatus): number => {
    if (status === 'ALL') return inscriptions.length;
    return inscriptions.filter(i => i.status === status).length;
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-uptc-yellow mx-auto"></div>
          <p className="text-gray-600 mt-4 font-semibold">Cargando inscripciones...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      <TournamentTabs tournamentId={tournamentId} tournament={tournament} activeTab="inscripciones" />

      <div className="bg-white rounded-lg shadow-lg p-6">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-800">Inscripciones del Torneo</h2>
          <p className="text-gray-600 mt-1">Administra las solicitudes de inscripción de equipos</p>
        </div>

        {/* Estadísticas Rápidas */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div
            className={`bg-white rounded-lg shadow p-4 cursor-pointer transition hover:shadow-lg ${
              filterStatus === 'ALL' ? 'ring-2 ring-uptc-yellow' : ''
            }`}
            onClick={() => setFilterStatus('ALL')}
          >
            <div className="text-gray-600 text-sm font-semibold">Total Inscripciones</div>
            <div className="text-3xl font-bold text-gray-900">{getStatusCount('ALL')}</div>
          </div>
          <div
            className={`bg-white rounded-lg shadow p-4 cursor-pointer transition hover:shadow-lg ${
              filterStatus === 'PENDING' ? 'ring-2 ring-yellow-500' : ''
            }`}
            onClick={() => setFilterStatus('PENDING')}
          >
            <div className="text-gray-600 text-sm font-semibold">Pendientes</div>
            <div className="text-3xl font-bold text-yellow-600">{getStatusCount('PENDING')}</div>
          </div>
          <div
            className={`bg-white rounded-lg shadow p-4 cursor-pointer transition hover:shadow-lg ${
              filterStatus === 'APPROVED' ? 'ring-2 ring-green-500' : ''
            }`}
            onClick={() => setFilterStatus('APPROVED')}
          >
            <div className="text-gray-600 text-sm font-semibold">Aprobadas</div>
            <div className="text-3xl font-bold text-green-600">{getStatusCount('APPROVED')}</div>
          </div>
          <div
            className={`bg-white rounded-lg shadow p-4 cursor-pointer transition hover:shadow-lg ${
              filterStatus === 'REJECTED' ? 'ring-2 ring-red-500' : ''
            }`}
            onClick={() => setFilterStatus('REJECTED')}
          >
            <div className="text-gray-600 text-sm font-semibold">Rechazadas</div>
            <div className="text-3xl font-bold text-red-600">{getStatusCount('REJECTED')}</div>
          </div>
        </div>

        {/* Barra de Búsqueda */}
        <div className="mb-6">
          <div className="flex gap-4">
            <input
              type="text"
              placeholder="Buscar por equipo, delegado o categoría..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:outline-none"
            />
            <button
              onClick={() => {
                setFilterStatus('ALL');
                setSearchTerm('');
              }}
              className="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 font-semibold rounded-lg transition"
            >
              Limpiar Filtros
            </button>
          </div>
        </div>

        {/* Tabla de Inscripciones */}
        <div className="overflow-x-auto">
          <table className="min-w-full border">
            <thead className="bg-uptc-black">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">ID</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Equipo</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Categoría</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Estado</th>
                <th className="px-6 py-3 text-center text-sm font-semibold text-uptc-yellow">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredInscriptions.length > 0 ? (
                filteredInscriptions.map((inscription) => (
                  <tr key={inscription.id} className="border-t hover:bg-gray-50 transition">
                    <td className="px-6 py-4">
                      <span className="text-gray-600 font-mono text-sm">#{inscription.id}</span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-semibold text-gray-900">{inscription.teamName || '—'}</div>
                      <div className="text-sm text-gray-600">Delegado: {inscription.delegateName || 'N/A'}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-900 font-medium">{inscription.category?.name || '—'}</div>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getStatusBadge(
                          inscription.status
                        )}`}
                      >
                        {getStatusText(inscription.status)}
                      </span>

                      {inscription.status === 'PENDING' && (
                        <div className="mt-2">
                          <span
                            className={`text-xs font-semibold ${
                              inscription.playerCount >= inscription.category?.membersPerTeam
                                ? 'text-green-600'
                                : 'text-orange-600'
                            }`}
                          >
                            {inscription.playerCount || 0} / {inscription.category?.membersPerTeam || 0} jugadores
                          </span>

                          <button
                            onClick={() => handleViewPlayers(inscription.id)}
                            className="text-blue-600 hover:text-blue-800 ml-3 text-xs font-semibold underline"
                          >
                            Ver jugadores
                          </button>
                        </div>
                      )}
                    </td>

                    <td className="px-6 py-4">
                      <div className="flex justify-center gap-2">
                        {/* ✅ VER DETALLES - Todos pueden verlo */}
                        <button
                          onClick={() => handleViewDetails(inscription.id)}
                          className="px-3 py-1 bg-uptc-black hover:bg-uptc-yellow hover:text-uptc-black text-uptc-yellow font-semibold text-sm rounded transition"
                        >
                          Ver Detalles
                        </button>

                        {/* 🔒 APROBAR - Solo con permiso */}
                        {inscription.status === 'PENDING' &&
                         inscription.playerCount >= inscription.category?.membersPerTeam && (
                          <PermissionGate permission="inscriptions.approve">
                            <button
                              onClick={() => handleQuickApprove(inscription.id)}
                              className="px-3 py-1 bg-green-500 hover:bg-green-600 text-white font-semibold text-sm rounded transition"
                            >
                              Aprobar
                            </button>
                          </PermissionGate>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center">
                    <div className="text-gray-400">
                      <p className="text-gray-500 font-medium">
                        {searchTerm || filterStatus !== 'ALL'
                          ? 'No se encontraron inscripciones con los filtros aplicados'
                          : 'No hay inscripciones en este torneo'}
                      </p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal de Jugadores */}
      {showPlayersModal && selectedInscriptionId && (
        <PlayerSelectionModal
          inscriptionId={selectedInscriptionId}
          maxPlayers={
            inscriptions.find((i) => i.id === selectedInscriptionId)?.category?.membersPerTeam || 0
          }
          onClose={() => {
            setShowPlayersModal(false);
            setSelectedInscriptionId(null);
          }}
          onPlayersUpdated={fetchData}
        />
      )}

      {/* Modal de Detalles */}
      {showDetailsModal && selectedInscriptionId && (
        <InscriptionDetailsModal
          inscriptionId={selectedInscriptionId}
          onClose={() => {
            setShowDetailsModal(false);
            setSelectedInscriptionId(null);
          }}
          onStatusUpdated={fetchData}
        />
      )}
    </div>
  );
}