'use client';

import { useEffect, useState } from 'react';
import PlayerSelectionModal from '@/components/PlayerSelectionModal';
import  inscriptionsService  from '@/services/inscriptionsService';
import { getStatusBadge, getStatusText } from '@/utils/inscriptionStatusUtils';

export default function InscripcionesAdminPage() {
  const [inscriptions, setInscriptions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  // 🔥 Estados para el modal de jugadores
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

  if (loading) return <p className="text-center py-10">Cargando inscripciones...</p>;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-semibold mb-6 text-uptc-black">Gestión de Inscripciones</h1>

      <div className="overflow-x-auto">
        <table className="min-w-full border rounded-lg bg-white shadow">
          <thead className="bg-uptc-black text-white">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-semibold">Equipo</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Torneo</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Categoría</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Estado</th>
              <th className="px-6 py-3 text-center text-sm font-semibold">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {inscriptions.length > 0 ? (
              inscriptions.map((inscription) => (
                <tr key={inscription.id} className="border-t hover:bg-gray-50 transition">
                  <td className="px-6 py-4">{inscription.team?.name || '—'}</td>
                  <td className="px-6 py-4">{inscription.tournament?.name || '—'}</td>
                  <td className="px-6 py-4">{inscription.category?.name || '—'}</td>

                  {/* Estado con contador y botón */}
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getStatusBadge(inscription.status)}`}
                    >
                      {getStatusText(inscription.status)}
                    </span>

                    {inscription.status === 'PENDING' && (
                      <div className="mt-1">
                        <span
                          className={`text-xs ${
                            inscription.playerCount >= inscription.category.membersPerTeam
                              ? 'text-green-600 font-semibold'
                              : 'text-orange-600'
                          }`}
                        >
                          {inscription.playerCount || 0} / {inscription.category.membersPerTeam} jugadores
                        </span>

                        <button
                          onClick={() => handleViewPlayers(inscription.id)}
                          className="text-uptc-black hover:underline ml-2 text-xs"
                        >
                          Ver jugadores
                        </button>
                      </div>
                    )}

                    {inscription.status === 'REJECTED' && inscription.rejectionReason && (
                      <div className="text-xs text-red-600 mt-1">
                        Motivo: {inscription.rejectionReason.substring(0, 30)}...
                      </div>
                    )}
                  </td>

                  <td className="px-6 py-4 text-center">
                    <button
                      onClick={() => alert('Funcionalidad pendiente')}
                      className="text-uptc-black hover:underline"
                    >
                      Ver detalles
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                  No hay inscripciones registradas.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* 🔥 Modal de jugadores */}
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
