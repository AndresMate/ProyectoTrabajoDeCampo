// frontend-uptc/src/components/InscriptionDetailsModal.tsx
'use client';

import { useEffect, useState } from 'react';
import inscriptionsService from '@/services/inscriptionsService';
import { getStatusBadge, getStatusText } from '@/utils/inscriptionStatusUtils';

interface InscriptionDetailsModalProps {
  inscriptionId: number;
  onClose: () => void;
  onStatusUpdated: () => void;
}

export default function InscriptionDetailsModal({
  inscriptionId,
  onClose,
  onStatusUpdated
}: InscriptionDetailsModalProps) {
  const [inscription, setInscription] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectionReason, setRejectionReason] = useState('');
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    fetchInscriptionDetails();
  }, [inscriptionId]);

  const fetchInscriptionDetails = async () => {
    try {
      setLoading(true);
      // 🔥 SOLO UNA PETICIÓN: La inscripción ya trae los jugadores
      const inscriptionData = await inscriptionsService.getById(inscriptionId);

      console.log('📋 Inscripción recibida:', inscriptionData);
      console.log('👥 Jugadores en inscripción:', inscriptionData.players);

      setInscription(inscriptionData);
    } catch (error) {
      console.error('Error al cargar detalles:', error);
      alert('Error al cargar los detalles de la inscripción');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async () => {
    if (!confirm('¿Está seguro de aprobar esta inscripción?')) return;

    try {
      setProcessing(true);
      await inscriptionsService.approve(inscriptionId);
      alert('Inscripción aprobada exitosamente');
      onStatusUpdated();
      onClose();
    } catch (error: any) {
      console.error('Error al aprobar:', error);
      alert(error.response?.data?.message || 'Error al aprobar la inscripción');
    } finally {
      setProcessing(false);
    }
  };

  const handleReject = async () => {
    if (!rejectionReason.trim()) {
      alert('Debe proporcionar un motivo de rechazo');
      return;
    }

    try {
      setProcessing(true);
      await inscriptionsService.reject(inscriptionId, rejectionReason);
      alert('Inscripción rechazada');
      onStatusUpdated();
      onClose();
    } catch (error: any) {
      console.error('Error al rechazar:', error);
      alert(error.response?.data?.message || 'Error al rechazar la inscripción');
    } finally {
      setProcessing(false);
      setShowRejectModal(false);
    }
  };

  const canModifyStatus = inscription?.status === 'PENDING';

  // 🔥 USAR LOS JUGADORES QUE YA VIENEN EN LA INSCRIPCIÓN
  const players = inscription?.players || [];

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow mx-auto"></div>
          <p className="text-center mt-4 text-gray-600">Cargando detalles...</p>
        </div>
      </div>
    );
  }

  if (!inscription) {
    return null;
  }

  return (
    <>
      {/* Modal Principal */}
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-lg shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
          {/* Header */}
          <div className="sticky top-0 bg-uptc-black text-white p-6 rounded-t-lg z-10">
            <div className="flex justify-between items-start">
              <div>
                <h2 className="text-2xl font-bold text-uptc-yellow">
                  Detalles de Inscripción
                </h2>
                <p className="text-gray-300 mt-1">ID: {inscription.id}</p>
              </div>
              <button
                onClick={onClose}
                className="text-white hover:text-uptc-yellow transition text-2xl font-bold"
              >
                ×
              </button>
            </div>
          </div>

          {/* Body */}
          <div className="p-6 space-y-6">
            {/* Información del Equipo */}
            <section>
              <h3 className="text-lg font-bold text-gray-800 mb-3 border-b-2 border-uptc-yellow pb-2">
                📋 Información del Equipo
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Nombre del Equipo</label>
                  <p className="text-gray-900 font-medium">{inscription.teamName || '—'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Club</label>
                  <p className="text-gray-900 font-medium">{inscription.club?.name || 'Sin club'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Torneo</label>
                  <p className="text-gray-900 font-medium">{inscription.tournament?.name || '—'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Categoría</label>
                  <p className="text-gray-900 font-medium">{inscription.category?.name || '—'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Estado</label>
                  <span
                    className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getStatusBadge(
                      inscription.status
                    )}`}
                  >
                    {getStatusText(inscription.status)}
                  </span>
                </div>
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Fecha de Inscripción</label>
                  <p className="text-gray-900 font-medium">
                    {new Date(inscription.createdAt).toLocaleDateString('es-CO')}
                  </p>
                </div>
              </div>
            </section>

            {/* Información del Delegado */}
            <section>
              <h3 className="text-lg font-bold text-gray-800 mb-3 border-b-2 border-uptc-yellow pb-2">
                👤 Información del Delegado
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Nombre Completo</label>
                  <p className="text-gray-900 font-medium">{inscription.delegateName || '—'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Teléfono</label>
                  <p className="text-gray-900 font-medium">{inscription.delegatePhone || '—'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 font-semibold">Email</label>
                  <p className="text-gray-900 font-medium">{inscription.delegateEmail || '—'}</p>
                </div>
              </div>
            </section>

            {/* Lista de Jugadores */}
            <section>
              <h3 className="text-lg font-bold text-gray-800 mb-3 border-b-2 border-uptc-yellow pb-2">
                ⚽ Jugadores Inscritos ({players.length}/{inscription.category?.membersPerTeam || 0})
              </h3>

              {players.length === 0 ? (
                <div className="text-center py-8">
                  <div className="text-gray-400 mb-2">
                    <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                  </div>
                  <p className="text-gray-500 font-medium">No hay jugadores registrados</p>
                  <p className="text-gray-400 text-sm mt-1">
                    Se requieren {inscription.category?.membersPerTeam || 0} jugadores para completar la inscripción
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  {players.map((player: any, index: number) => (
                    <div
                      key={player.id || index}
                      className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition"
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1 grid grid-cols-1 md:grid-cols-3 gap-3">
                          <div>
                            <label className="text-xs text-gray-600 font-semibold">
                              Jugador #{index + 1}
                            </label>
                            <p className="text-gray-900 font-medium">
                              {player.fullName || '—'}
                            </p>
                          </div>
                          <div>
                            <label className="text-xs text-gray-600 font-semibold">
                              Código Estudiantil
                            </label>
                            <p className="text-gray-900 font-medium">
                              {player.studentCode || '—'}
                            </p>
                          </div>
                          <div>
                            <label className="text-xs text-gray-600 font-semibold">Documento</label>
                            <p className="text-gray-900 font-medium">
                              {player.documentNumber || '—'}
                            </p>
                          </div>
                        </div>

                        {/* Botón para ver carné */}
                        {player.idCardImageUrl && (
                            <a
                            href={player.idCardImageUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="ml-4 px-3 py-1 bg-blue-500 hover:bg-blue-600 text-white text-xs font-semibold rounded transition whitespace-nowrap"
                          >
                            Ver Carné
                          </a>
                        )}
                      </div>

                      {/* Email adicional */}
                      {player.institutionalEmail && (
                        <div className="mt-2 pt-2 border-t border-gray-100">
                          <label className="text-xs text-gray-600 font-semibold">Email Institucional</label>
                          <p className="text-gray-700 text-sm break-all">
                            {player.institutionalEmail}
                          </p>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* Motivo de Rechazo (si aplica) */}
            {inscription.status === 'REJECTED' && inscription.rejectionReason && (
              <section>
                <h3 className="text-lg font-bold text-red-600 mb-3 border-b-2 border-red-600 pb-2">
                  ❌ Motivo de Rechazo
                </h3>
                <p className="text-gray-700 bg-red-50 p-3 rounded border border-red-200">
                  {inscription.rejectionReason}
                </p>
              </section>
            )}
          </div>

          {/* Footer con Acciones */}
          {canModifyStatus && (
            <div className="sticky bottom-0 bg-gray-50 border-t p-6 rounded-b-lg flex justify-end gap-3">
              <button
                onClick={() => setShowRejectModal(true)}
                disabled={processing}
                className="px-6 py-2 bg-red-500 hover:bg-red-600 text-white font-semibold rounded transition disabled:opacity-50"
              >
                Rechazar
              </button>
              <button
                onClick={handleApprove}
                disabled={processing || players.length < (inscription.category?.membersPerTeam || 0)}
                className="px-6 py-2 bg-green-500 hover:bg-green-600 text-white font-semibold rounded transition disabled:opacity-50"
                title={
                  players.length < (inscription.category?.membersPerTeam || 0)
                    ? `Se requieren ${inscription.category?.membersPerTeam} jugadores`
                    : 'Aprobar inscripción'
                }
              >
                {processing ? 'Procesando...' : 'Aprobar'}
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Modal de Rechazo */}
      {showRejectModal && (
        <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-[60]">
          <div className="bg-white rounded-lg shadow-2xl p-6 max-w-md w-full mx-4">
            <h3 className="text-xl font-bold text-gray-800 mb-4">Rechazar Inscripción</h3>
            <p className="text-gray-600 mb-4">
              Por favor, proporcione el motivo del rechazo:
            </p>
            <textarea
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              className="w-full border border-gray-300 rounded p-3 focus:ring-2 focus:ring-red-500 focus:outline-none"
              rows={4}
              placeholder="Ejemplo: No se cumplen los requisitos mínimos..."
            />
            <div className="flex justify-end gap-3 mt-4">
              <button
                onClick={() => {
                  setShowRejectModal(false);
                  setRejectionReason('');
                }}
                className="px-4 py-2 bg-gray-300 hover:bg-gray-400 text-gray-800 font-semibold rounded transition"
              >
                Cancelar
              </button>
              <button
                onClick={handleReject}
                disabled={processing || !rejectionReason.trim()}
                className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white font-semibold rounded transition disabled:opacity-50"
              >
                {processing ? 'Procesando...' : 'Confirmar Rechazo'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}