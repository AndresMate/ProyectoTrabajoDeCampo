'use client';

import { useEffect, useState } from 'react';
import inscriptionsService from '@/services/inscriptionsService';

interface Inscription {
  id: number;
  tournamentName?: string;
  categoryName?: string;
  teamName: string;
  clubName?: string;
  delegateName: string;
  delegateEmail: string;
  delegatePhone: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  rejectionReason?: string;
}

export default function AdminInscripcionesPage() {
  const [inscriptions, setInscriptions] = useState<Inscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [selectedInscription, setSelectedInscription] = useState<Inscription | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');

  useEffect(() => {
    fetchInscriptions();
  }, []);

  const fetchInscriptions = async () => {
    try {
      const data = await inscriptionsService.getAll();
      setInscriptions(data);
    } catch (error) {
      console.error('Error al cargar inscripciones:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id: number) => {
    if (!confirm('¿Aprobar esta inscripción?')) return;

    try {
      await inscriptionsService.approve(id);
      alert('Inscripción aprobada exitosamente');
      fetchInscriptions();
    } catch (error: unknown) {
      alert(error.response?.data?.message || 'Error al aprobar inscripción');
    }
  };

  const handleReject = async () => {
    if (!selectedInscription || !rejectionReason.trim()) {
      alert('Debes proporcionar un motivo de rechazo');
      return;
    }

    try {
      await inscriptionsService.reject(selectedInscription.id, rejectionReason);
      alert('Inscripción rechazada exitosamente');
      setShowRejectModal(false);
      setRejectionReason('');
      setSelectedInscription(null);
      fetchInscriptions();
    } catch (error: unknown) {
      alert(error.response?.data?.message || 'Error al rechazar inscripción');
    }
  };

  const getStatusBadge = (status: string) => {
    const colors = {
      PENDING: 'bg-yellow-100 text-yellow-700',
      APPROVED: 'bg-green-100 text-green-700',
      REJECTED: 'bg-red-100 text-red-700'
    };
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts = {
      PENDING: 'Pendiente',
      APPROVED: 'Aprobada',
      REJECTED: 'Rechazada'
    };
    return texts[status as keyof typeof texts] || status;
  };

  const filteredInscriptions = filterStatus === 'ALL'
    ? inscriptions
    : inscriptions.filter(i => i.status === filterStatus);

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
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Inscripciones</h1>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Total Inscripciones</div>
          <div className="text-2xl font-bold text-gray-900">{inscriptions.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Pendientes</div>
          <div className="text-2xl font-bold text-yellow-600">
            {inscriptions.filter(i => i.status === 'PENDING').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Aprobadas</div>
          <div className="text-2xl font-bold text-green-600">
            {inscriptions.filter(i => i.status === 'APPROVED').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Rechazadas</div>
          <div className="text-2xl font-bold text-red-600">
            {inscriptions.filter(i => i.status === 'REJECTED').length}
          </div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex gap-2">
          {['ALL', 'PENDING', 'APPROVED', 'REJECTED'].map(status => (
            <button
              key={status}
              onClick={() => setFilterStatus(status)}
              className={`px-4 py-2 rounded-lg transition ${
                filterStatus === status
                  ? 'bg-blue-900 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {status === 'ALL' ? 'Todas' : getStatusText(status)}
            </button>
          ))}
        </div>
      </div>

      {/* Lista de inscripciones */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Equipo</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Torneo</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Delegado</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Fecha</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Acciones</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredInscriptions.map((inscription) => (
              <tr key={inscription.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="font-medium text-gray-900">{inscription.teamName}</div>
                  <div className="text-sm text-gray-500">{inscription.clubName || 'Sin club'}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">{inscription.tournamentName || 'N/A'}</div>
                  <div className="text-sm text-gray-500">{inscription.categoryName || 'N/A'}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">{inscription.delegateName}</div>
                  <div className="text-sm text-gray-500">{inscription.delegateEmail}</div>
                  <div className="text-sm text-gray-500">{inscription.delegatePhone}</div>
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${
                    getStatusBadge(inscription.status)
                  }`}>
                    {getStatusText(inscription.status)}
                  </span>
                  {inscription.status === 'REJECTED' && inscription.rejectionReason && (
                    <div className="text-xs text-red-600 mt-1" title={inscription.rejectionReason}>
                      Motivo: {inscription.rejectionReason.substring(0, 30)}...
                    </div>
                  )}
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">
                  {new Date(inscription.createdAt).toLocaleDateString('es-ES')}
                </td>
                <td className="px-6 py-4 text-right text-sm font-medium">
                  {inscription.status === 'PENDING' && (
                    <>
                      <button
                        onClick={() => handleApprove(inscription.id)}
                        className="text-green-600 hover:text-green-900 mr-3"
                      >
                        ✓ Aprobar
                      </button>
                      <button
                        onClick={() => {
                          setSelectedInscription(inscription);
                          setShowRejectModal(true);
                        }}
                        className="text-red-600 hover:text-red-900"
                      >
                        ✗ Rechazar
                      </button>
                    </>
                  )}
                  {inscription.status !== 'PENDING' && (
                    <button className="text-blue-600 hover:text-blue-900">
                      Ver detalles
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredInscriptions.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center mt-6">
          <p className="text-gray-500">No hay inscripciones con este estado</p>
        </div>
      )}

      {/* Modal de rechazo */}
      {showRejectModal && selectedInscription && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 max-w-md w-full">
            <h2 className="text-2xl font-bold mb-4">Rechazar Inscripción</h2>
            <p className="text-gray-700 mb-4">
              <strong>Equipo:</strong> {selectedInscription.teamName}
            </p>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">
                Motivo del rechazo *
              </label>
              <textarea
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
                rows={4}
                placeholder="Explica por qué se rechaza esta inscripción..."
              />
            </div>
            <div className="flex gap-2">
              <button
                onClick={handleReject}
                className="flex-1 bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition"
              >
                Rechazar
              </button>
              <button
                onClick={() => {
                  setShowRejectModal(false);
                  setRejectionReason('');
                  setSelectedInscription(null);
                }}
                className="flex-1 bg-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-400 transition"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}