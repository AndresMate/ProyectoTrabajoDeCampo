// frontend-uptc/src/components/SanctionsManager.tsx
'use client';

import { useState, useEffect } from 'react';
import { toastWarning, toastSuccess } from '@/utils/toast';

interface Sanction {
  id: number;
  type: string;
  reason: string;
  dateIssued: string;
  validUntil?: string;
  player?: {
    id: number;
    fullName: string;
  };
  team?: {
    id: number;
    name: string;
  };
  match?: {
    id: number;
  };
}

interface SanctionsManagerProps {
  entityType: 'player' | 'team' | 'match';
  entityId: number;
  onClose: () => void;
}

export default function SanctionsManager({ entityType, entityId, onClose }: SanctionsManagerProps) {
  const [sanctions, setSanctions] = useState<Sanction[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddForm, setShowAddForm] = useState(false);

  const [formData, setFormData] = useState({
    type: 'WARNING',
    reason: '',
    validUntil: ''
  });

  useEffect(() => {
    fetchSanctions();
  }, [entityType, entityId]);

  const fetchSanctions = async () => {
    setLoading(true);
    try {
      // Aquí iría la llamada al servicio
      // const data = await sanctionsService.getByEntity(entityType, entityId);
      // setSanctions(data);

      // Simulación
      setSanctions([]);
    } catch (error) {
      console.error('Error al cargar sanciones:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddSanction = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.reason.trim()) {
      toastWarning('Debes proporcionar un motivo');
      return;
    }

    try {
      const payload = {
        ...formData,
        [`${entityType}Id`]: entityId
      };

      // await sanctionsService.create(payload);
      toastSuccess('✅ Sanción agregada exitosamente');
      setShowAddForm(false);
      setFormData({ type: 'WARNING', reason: '', validUntil: '' });
      fetchSanctions();
    } catch (error: any) {
      // El error ya se muestra en el interceptor de axios
    }
  };

  const handleDeleteSanction = async (id: number) => {
    try {
      // await sanctionsService.delete(id);
      toastSuccess('Sanción eliminada');
      fetchSanctions();
    } catch (error: any) {
      // El error ya se muestra en el interceptor de axios
    }
  };

  const getSanctionTypeBadge = (type: string) => {
    const badges: any = {
      WARNING: 'bg-yellow-100 text-yellow-700',
      YELLOW_CARD: 'bg-yellow-200 text-yellow-800',
      RED_CARD: 'bg-red-100 text-red-700',
      SUSPENSION: 'bg-red-200 text-red-800',
      FINE: 'bg-orange-100 text-orange-700'
    };
    return badges[type] || 'bg-gray-100 text-gray-700';
  };

  const getSanctionTypeText = (type: string) => {
    const texts: any = {
      WARNING: 'Advertencia',
      YELLOW_CARD: 'Tarjeta Amarilla',
      RED_CARD: 'Tarjeta Roja',
      SUSPENSION: 'Suspensión',
      FINE: 'Multa'
    };
    return texts[type] || type;
  };

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow mx-auto"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h2 className="text-2xl font-bold text-gray-800">
            Sanciones ({sanctions.length})
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-2xl"
          >
            ×
          </button>
        </div>

        <div className="p-6">
          {/* Botón agregar sanción */}
          {!showAddForm && (
            <button
              onClick={() => setShowAddForm(true)}
              className="mb-4 bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700"
            >
              + Agregar Sanción
            </button>
          )}

          {/* Formulario de nueva sanción */}
          {showAddForm && (
            <form onSubmit={handleAddSanction} className="mb-6 bg-gray-50 p-4 rounded-lg border">
              <h3 className="font-semibold mb-3">Nueva Sanción</h3>

              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium mb-1">Tipo de Sanción</label>
                  <select
                    value={formData.type}
                    onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  >
                    <option value="WARNING">Advertencia</option>
                    <option value="YELLOW_CARD">Tarjeta Amarilla</option>
                    <option value="RED_CARD">Tarjeta Roja</option>
                    <option value="SUSPENSION">Suspensión</option>
                    <option value="FINE">Multa</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">Motivo *</label>
                  <textarea
                    value={formData.reason}
                    onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                    rows={3}
                    placeholder="Describe el motivo de la sanción..."
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">Válido hasta (opcional)</label>
                  <input
                    type="date"
                    value={formData.validUntil}
                    onChange={(e) => setFormData({ ...formData, validUntil: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  />
                </div>

                <div className="flex gap-2">
                  <button
                    type="submit"
                    className="flex-1 bg-red-600 text-white py-2 rounded-lg hover:bg-red-700"
                  >
                    Agregar
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowAddForm(false)}
                    className="px-6 bg-gray-300 py-2 rounded-lg hover:bg-gray-400"
                  >
                    Cancelar
                  </button>
                </div>
              </div>
            </form>
          )}

          {/* Lista de sanciones */}
          {sanctions.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-gray-500 text-lg">No hay sanciones registradas</p>
            </div>
          ) : (
            <div className="space-y-3">
              {sanctions.map(sanction => (
                <div key={sanction.id} className="border rounded-lg p-4 bg-gray-50">
                  <div className="flex justify-between items-start mb-2">
                    <span className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getSanctionTypeBadge(sanction.type)}`}>
                      {getSanctionTypeText(sanction.type)}
                    </span>
                    <button
                      onClick={() => handleDeleteSanction(sanction.id)}
                      className="text-red-600 hover:text-red-800 text-sm"
                    >
                      Eliminar
                    </button>
                  </div>

                  <p className="text-gray-800 mb-2">{sanction.reason}</p>

                  <div className="text-sm text-gray-600">
                    <p>Fecha: {new Date(sanction.dateIssued).toLocaleDateString('es-ES')}</p>
                    {sanction.validUntil && (
                      <p>Válido hasta: {new Date(sanction.validUntil).toLocaleDateString('es-ES')}</p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="border-t p-6 flex justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2 bg-uptc-black text-white rounded-lg hover:bg-gray-800"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}