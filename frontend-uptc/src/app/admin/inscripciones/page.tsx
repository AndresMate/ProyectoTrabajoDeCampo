// frontend-uptc/src/app/admin/inscripciones/page.tsx - VERSIÓN CORREGIDA
'use client';

import { useEffect, useState } from 'react';
import PlayerSelectionModal from '@/components/PlayerSelectionModal';
import InscriptionDetailsModal from '@/components/InscriptionDetailsModal';
import inscriptionsService from '@/services/inscriptionsService';
import { getStatusBadge, getStatusText } from '@/utils/inscriptionStatusUtils';

type FilterStatus = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED';

export default function InscripcionesAdminPage() {
  const [inscriptions, setInscriptions] = useState<any[]>([]);
  const [filteredInscriptions, setFilteredInscriptions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<FilterStatus>('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const [showPlayersModal, setShowPlayersModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedInscriptionId, setSelectedInscriptionId] = useState<number | null>(null);

  useEffect(() => {
    fetchInscriptions();
  }, []);

  useEffect(() => {
    applyFilters();
  }, [inscriptions, filterStatus, searchTerm]);

  const fetchInscriptions = async () => {
    try {
      setLoading(true);
      const data = await inscriptionsService.getAll();
      setInscriptions(data);
    } catch (error) {
      console.error('Error al cargar inscripciones:', error);
      alert('Error al cargar las inscripciones');
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let result = [...inscriptions];

    // Filtrar por estado
    if (filterStatus !== 'ALL') {
      result = result.filter(i => i.status === filterStatus);
    }

    // Filtrar por búsqueda - CORREGIDO con los campos reales del DTO
    if (searchTerm.trim()) {
      const search = searchTerm.toLowerCase();
      result = result.filter(i =>
        i.teamName?.toLowerCase().includes(search) ||
        i.delegateName?.toLowerCase().includes(search) ||
        i.tournament?.name?.toLowerCase().includes(search) ||
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
    if (!confirm('¿Está seguro de aprobar esta inscripción?')) return;

    try {
      await inscriptionsService.approve(inscriptionId);
      alert('Inscripción aprobada exitosamente');
      fetchInscriptions();
    } catch (error: any) {
      console.error('Error al aprobar:', error);
      alert(error.response?.data?.message || 'Error al aprobar la inscripción');
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
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Inscripciones</h1>
        <p className="text-gray-600 mt-2">Administra las inscripciones de equipos a torneos</p>
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

      {/* Barra de Búsqueda y Filtros */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <input
              type="text"
              placeholder="Buscar por equipo, delegado, torneo o categoría..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:outline-none"
            />
          </div>
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
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full border">
            <thead className="bg-uptc-black">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">ID</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Equipo</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Torneo</th>
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
                    {/* ✅ CORREGIDO: Usar teamName y delegateName directamente */}
                    <td className="px-6 py-4">
                      <div className="font-semibold text-gray-900">{inscription.teamName || '—'}</div>
                      <div className="text-sm text-gray-600">Delegado: {inscription.delegateName || 'N/A'}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-900 font-medium">{inscription.tournament?.name || '—'}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-900 font-medium">{inscription.category?.name || '—'}</div>
                    </td>

                    {/* Estado con contador y botón */}
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

                      {inscription.status === 'REJECTED' && inscription.rejectionReason && (
                        <div className="text-xs text-red-600 mt-2 font-medium" title={inscription.rejectionReason}>
                          Motivo: {inscription.rejectionReason.substring(0, 30)}...
                        </div>
                      )}
                    </td>

                    <td className="px-6 py-4">
                      <div className="flex justify-center gap-2">
                        <button
                          onClick={() => handleViewDetails(inscription.id)}
                          className="px-3 py-1 bg-uptc-black hover:bg-uptc-yellow hover:text-uptc-black text-uptc-yellow font-semibold text-sm rounded transition"
                        >
                          Ver Detalles
                        </button>
                        {inscription.status === 'PENDING' &&
                         inscription.playerCount >= inscription.category?.membersPerTeam && (
                          <button
                            onClick={() => handleQuickApprove(inscription.id)}
                            className="px-3 py-1 bg-green-500 hover:bg-green-600 text-white font-semibold text-sm rounded transition"
                          >
                            Aprobar
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center">
                    <div className="text-gray-400">
                      <svg
                        className="mx-auto h-12 w-12 mb-4"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
                        />
                      </svg>
                      <p className="text-gray-500 font-medium">
                        {searchTerm || filterStatus !== 'ALL'
                          ? 'No se encontraron inscripciones con los filtros aplicados'
                          : 'No hay inscripciones registradas'}
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
          onPlayersUpdated={fetchInscriptions}
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
          onStatusUpdated={fetchInscriptions}
        />
      )}
    </div>
  );
}