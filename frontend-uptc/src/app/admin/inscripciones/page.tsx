// frontend-uptc/src/app/admin/inscripciones/page.tsx - VERSIÓN CORREGIDA
'use client';

import { useEffect, useState } from 'react';
import PlayerSelectionModal from '@/components/PlayerSelectionModal';
import inscriptionsService from '@/services/inscriptionsService';
import { getStatusBadge, getStatusText } from '@/utils/inscriptionStatusUtils';

export default function InscripcionesAdminPage() {
  const [inscriptions, setInscriptions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const [showPlayersModal, setShowPlayersModal] = useState(false);
  const [selectedInscriptionForPlayers, setSelectedInscriptionForPlayers] = useState<number | null>(null);

  useEffect(() => {
    fetchInscriptions();
  }, []);

  const fetchInscriptions = async () => {
    try {
      setLoading(true);
      const data = await inscriptionsService.getAll();
      setInscriptions(data);
    } catch (error) {
      console.error('Error al cargar inscripciones:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewPlayers = (inscriptionId: number) => {
    setSelectedInscriptionForPlayers(inscriptionId);
    setShowPlayersModal(true);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Inscripciones</h1>
        <p className="text-gray-600 mt-2">Administra las inscripciones de equipos a torneos</p>
      </div>

      {/* Estadísticas rápidas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Total Inscripciones</div>
          <div className="text-2xl font-bold text-gray-900">{inscriptions.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Pendientes</div>
          <div className="text-2xl font-bold text-yellow-600">
            {inscriptions.filter(i => i.status === 'PENDING').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Aprobadas</div>
          <div className="text-2xl font-bold text-green-600">
            {inscriptions.filter(i => i.status === 'APPROVED').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Rechazadas</div>
          <div className="text-2xl font-bold text-red-600">
            {inscriptions.filter(i => i.status === 'REJECTED').length}
          </div>
        </div>
      </div>

      {/* Tabla de inscripciones */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full border">
          <thead className="bg-uptc-black">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Equipo</th>
              <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Torneo</th>
              <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Categoría</th>
              <th className="px-6 py-3 text-left text-sm font-semibold text-uptc-yellow">Estado</th>
              <th className="px-6 py-3 text-center text-sm font-semibold text-uptc-yellow">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {inscriptions.length > 0 ? (
              inscriptions.map((inscription) => (
                <tr key={inscription.id} className="border-t hover:bg-gray-50 transition">
                  <td className="px-6 py-4">
                    <div className="font-semibold text-gray-900">{inscription.team?.name || '—'}</div>
                    <div className="text-sm text-gray-600">Delegado: {inscription.delegate?.fullName || 'N/A'}</div>
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
                      className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getStatusBadge(inscription.status)}`}
                    >
                      {getStatusText(inscription.status)}
                    </span>

                    {inscription.status === 'PENDING' && (
                      <div className="mt-2">
                        <span
                          className={`text-xs font-semibold ${
                            inscription.playerCount >= inscription.category.membersPerTeam
                              ? 'text-green-600'
                              : 'text-orange-600'
                          }`}
                        >
                          {inscription.playerCount || 0} / {inscription.category.membersPerTeam} jugadores
                        </span>

                        <button
                          onClick={() => handleViewPlayers(inscription.id)}
                          className="text-blue-600 hover:text-blue-800 ml-3 text-xs font-semibold"
                        >
                          Ver jugadores
                        </button>
                      </div>
                    )}

                    {inscription.status === 'REJECTED' && inscription.rejectionReason && (
                      <div className="text-xs text-red-600 mt-2 font-medium">
                        Motivo: {inscription.rejectionReason.substring(0, 30)}...
                      </div>
                    )}
                  </td>

                  <td className="px-6 py-4 text-center">
                    <button
                      onClick={() => alert('Funcionalidad pendiente')}
                      className="text-uptc-black hover:text-uptc-yellow font-semibold text-sm"
                    >
                      Ver detalles
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={5} className="px-6 py-8 text-center text-gray-500 font-medium">
                  No hay inscripciones registradas.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal de jugadores */}
      {showPlayersModal && selectedInscriptionForPlayers && (
        <PlayerSelectionModal
          inscriptionId={selectedInscriptionForPlayers}
          maxPlayers={
            inscriptions.find((i) => i.id === selectedInscriptionForPlayers)?.category.membersPerTeam || 0
          }
          onClose={() => {
            setShowPlayersModal(false);
            setSelectedInscriptionForPlayers(null);
          }}
          onPlayersUpdated={fetchInscriptions}
        />
      )}
    </div>
  );
}