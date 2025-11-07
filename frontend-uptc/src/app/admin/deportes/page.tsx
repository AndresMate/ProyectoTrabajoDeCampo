// frontend-uptc/src/app/admin/deportes/page.tsx
'use client';

import { useEffect, useState } from 'react';
import sportsService, { Sport } from '@/services/sportsService';
import Modal from '@/components/Modal';
import SportForm from '@/components/forms/SportForm';
import PermissionGate from '@/components/PermissionGate';
import { toastPromise } from '@/utils/toast';

export default function AdminDeportesPage() {
  const [sports, setSports] = useState<Sport[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedSport, setSelectedSport] = useState<number | undefined>();
  const [filterStatus, setFilterStatus] = useState<string>('ALL');

  useEffect(() => {
    fetchSports();
  }, []);

  const fetchSports = async () => {
    try {
      const data = await sportsService.getAll(0, 100);
      setSports(data.content || data);
    } catch (error) {
      console.error('Error al cargar deportes:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await toastPromise(
        sportsService.delete(id),
        {
          loading: 'Desactivando deporte...',
          success: 'Deporte desactivado exitosamente',
          error: 'Error al desactivar el deporte'
        }
      );
      fetchSports();
    } catch (error) {
      // El error ya se muestra en el toastPromise
    }
  };

  const handleReactivate = async (id: number) => {
    try {
      await toastPromise(
        sportsService.reactivate(id),
        {
          loading: 'Reactivando deporte...',
          success: 'Deporte reactivado exitosamente',
          error: 'Error al reactivar el deporte'
        }
      );
      fetchSports();
    } catch (error) {
      // El error ya se muestra en el toastPromise
    }
  };

  const filteredSports = sports.filter(sport => {
    const matchesSearch =
      sport.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (sport.description && sport.description.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesStatus = filterStatus === 'ALL' ||
      (filterStatus === 'ACTIVE' && sport.isActive) ||
      (filterStatus === 'INACTIVE' && !sport.isActive);

    return matchesSearch && matchesStatus;
  });

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
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Deportes</h1>

        {/* 🔒 PROTEGER BOTÓN DE CREAR */}
        <PermissionGate permission="tournaments.create">
          <button
            onClick={() => {
              setSelectedSport(undefined);
              setShowModal(true);
            }}
            className="bg-uptc-black text-uptc-yellow px-6 py-2 rounded-lg hover:bg-gray-800 transition font-semibold"
          >
            + Nuevo Deporte
          </button>
        </PermissionGate>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Total</div>
          <div className="text-2xl font-bold text-gray-900">{sports.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Activos</div>
          <div className="text-2xl font-bold text-green-600">
            {sports.filter(s => s.isActive).length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Inactivos</div>
          <div className="text-2xl font-bold text-red-600">
            {sports.filter(s => !s.isActive).length}
          </div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <input
            type="text"
            placeholder="Buscar por nombre o descripción..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1 px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900 font-medium"
          />
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow bg-white text-gray-900 font-medium"
          >
            <option value="ALL">Todos</option>
            <option value="ACTIVE">Activos</option>
            <option value="INACTIVE">Inactivos</option>
          </select>
        </div>
      </div>

      {/* Tabla de deportes */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-uptc-black">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Nombre
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Descripción
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Estado
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Fecha de creación
              </th>
              <th className="px-6 py-3 text-right text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredSports.map((sport) => (
              <tr key={sport.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="font-semibold text-gray-900">{sport.name}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-600 max-w-md truncate">
                    {sport.description || 'Sin descripción'}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${
                      sport.isActive
                        ? 'bg-green-100 text-green-700'
                        : 'bg-red-100 text-red-700'
                    }`}
                  >
                    {sport.isActive ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700 font-medium">
                  {sport.createdAt ? new Date(sport.createdAt).toLocaleDateString('es-ES') : 'N/A'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-semibold">
                  {/* 🔒 PROTEGER BOTONES DE ACCIÓN */}
                  <PermissionGate permission="tournaments.edit">
                    <button
                      onClick={() => {
                        setSelectedSport(sport.id);
                        setShowModal(true);
                      }}
                      className="text-indigo-600 hover:text-indigo-900 mr-3 transition-colors"
                    >
                      Editar
                    </button>
                  </PermissionGate>

                  {sport.isActive ? (
                    <PermissionGate permission="tournaments.delete">
                      <button
                        onClick={() => handleDelete(sport.id)}
                        className="text-red-600 hover:text-red-800 transition-colors"
                      >
                        Desactivar
                      </button>
                    </PermissionGate>
                  ) : (
                    <PermissionGate permission="tournaments.edit">
                      <button
                        onClick={() => handleReactivate(sport.id)}
                        className="text-green-600 hover:text-green-800 transition-colors"
                      >
                        Reactivar
                      </button>
                    </PermissionGate>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredSports.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center mt-6">
          <p className="text-gray-500 text-lg font-medium">No se encontraron deportes</p>
          <p className="text-gray-400 text-sm mt-2">
            {sports.length === 0 ? 'No hay deportes en el sistema' : 'No hay coincidencias con tu búsqueda'}
          </p>
        </div>
      )}

      {/* 🔒 PROTEGER MODAL */}
      {showModal && (
        <PermissionGate permission="tournaments.create">
          <Modal
            isOpen={showModal}
            onClose={() => {
              setShowModal(false);
              setSelectedSport(undefined);
            }}
            title={selectedSport ? 'Editar Deporte' : 'Crear Nuevo Deporte'}
            size="md"
          >
            <SportForm
              sportId={selectedSport}
              onSuccess={() => {
                setShowModal(false);
                setSelectedSport(undefined);
                fetchSports();
              }}
              onCancel={() => {
                setShowModal(false);
                setSelectedSport(undefined);
              }}
            />
          </Modal>
        </PermissionGate>
      )}
    </div>
  );
}