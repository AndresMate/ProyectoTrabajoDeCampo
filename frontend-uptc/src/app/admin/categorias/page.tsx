// frontend-uptc/src/app/admin/categorias/page.tsx
'use client';

import { useEffect, useState } from 'react';
import categoriesService, { Category } from '@/services/categoriesService';
import sportsService from '@/services/sportsService';
import Modal from '@/components/Modal';
import CategoryForm from '@/components/forms/CategoryForm';
import PermissionGate from '@/components/PermissionGate';
import { toastPromise } from '@/utils/toast';

export default function AdminCategoriasPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>();
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [filterSport, setFilterSport] = useState<string>('ALL');
  const [sports, setSports] = useState<any[]>([]);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [categoriesData, sportsData] = await Promise.all([
        categoriesService.getAll(0, 100),
        sportsService.getActive()
      ]);
      setCategories(categoriesData.content || categoriesData);
      setSports(sportsData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await toastPromise(
        categoriesService.delete(id),
        {
          loading: 'Desactivando categoría...',
          success: 'Categoría desactivada exitosamente',
          error: 'Error al desactivar la categoría'
        }
      );
      fetchData();
    } catch (error) {
      // El error ya se muestra en el toastPromise
    }
  };

  const handleReactivate = async (id: number) => {
    try {
      await toastPromise(
        categoriesService.reactivate(id),
        {
          loading: 'Reactivando categoría...',
          success: 'Categoría reactivada exitosamente',
          error: 'Error al reactivar la categoría'
        }
      );
      fetchData();
    } catch (error) {
      // El error ya se muestra en el toastPromise
    }
  };

  const getSportName = (sportId: number) => {
    const sport = sports.find(s => s.id === sportId);
    return sport?.name || 'N/A';
  };

  const filteredCategories = categories.filter(category => {
    const matchesSearch =
      category.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (category.description && category.description.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesStatus = filterStatus === 'ALL' ||
      (filterStatus === 'ACTIVE' && category.isActive) ||
      (filterStatus === 'INACTIVE' && !category.isActive);

    const matchesSport = filterSport === 'ALL' || category.sportId.toString() === filterSport;

    return matchesSearch && matchesStatus && matchesSport;
  });

  // Agrupar categorías por deporte
  const categoriesBySport = filteredCategories.reduce((acc, category) => {
    const sportId = category.sportId;
    if (!acc[sportId]) {
      acc[sportId] = [];
    }
    acc[sportId].push(category);
    return acc;
  }, {} as Record<number, Category[]>);

  // Obtener deportes que tienen categorías filtradas
  const sportsWithCategories = sports.filter(sport =>
    categoriesBySport[sport.id] && categoriesBySport[sport.id].length > 0
  );

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
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Categorías</h1>

        {/* 🔒 PROTEGER BOTÓN DE CREAR */}
        <PermissionGate permission="tournaments.create">
          <button
            onClick={() => {
              setSelectedCategory(undefined);
              setShowModal(true);
            }}
            className="bg-uptc-black text-uptc-yellow px-6 py-2 rounded-lg hover:bg-gray-800 transition font-semibold"
          >
            + Nueva Categoría
          </button>
        </PermissionGate>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Total</div>
          <div className="text-2xl font-bold text-gray-900">{categories.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Activas</div>
          <div className="text-2xl font-bold text-green-600">
            {categories.filter(c => c.isActive).length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Inactivas</div>
          <div className="text-2xl font-bold text-red-600">
            {categories.filter(c => !c.isActive).length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Deportes</div>
          <div className="text-2xl font-bold text-blue-600">
            {new Set(categories.map(c => c.sportId)).size}
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
            <option value="ALL">Todos los estados</option>
            <option value="ACTIVE">Activas</option>
            <option value="INACTIVE">Inactivas</option>
          </select>
          <select
            value={filterSport}
            onChange={(e) => setFilterSport(e.target.value)}
            className="px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow bg-white text-gray-900 font-medium"
          >
            <option value="ALL">Todos los deportes</option>
            {sports.map(sport => (
              <option key={sport.id} value={sport.id.toString()}>
                {sport.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Categorías agrupadas por deporte */}
      {sportsWithCategories.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500 text-lg font-medium">No se encontraron categorías</p>
          <p className="text-gray-400 text-sm mt-2">
            {categories.length === 0 ? 'No hay categorías en el sistema' : 'No hay coincidencias con tu búsqueda'}
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {sportsWithCategories.map((sport) => {
            const sportCategories = categoriesBySport[sport.id];
            return (
              <div key={sport.id} className="bg-white rounded-lg shadow overflow-hidden">
                {/* Header del deporte */}
                <div className="bg-uptc-black px-6 py-4 border-b-4 border-uptc-yellow">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-xl font-bold text-uptc-yellow">{sport.name}</h3>
                      <p className="text-sm text-white mt-1">
                        {sportCategories.length} categoría{sportCategories.length !== 1 ? 's' : ''}
                      </p>
                    </div>
                    <div className="text-right">
                      <span className="inline-flex px-3 py-1 text-xs font-semibold rounded-full bg-uptc-yellow text-uptc-black">
                        {sportCategories.filter(c => c.isActive).length} activa{sportCategories.filter(c => c.isActive).length !== 1 ? 's' : ''}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Tabla de categorías del deporte */}
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                          Nombre
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                          Descripción
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                          Detalles
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                          Estado
                        </th>
                        <th className="px-6 py-3 text-right text-xs font-semibold text-gray-700 uppercase tracking-wider">
                          Acciones
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {sportCategories.map((category) => (
                        <tr key={category.id} className="hover:bg-gray-50 transition-colors">
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="font-semibold text-gray-900">{category.name}</div>
                          </td>
                          <td className="px-6 py-4">
                            <div className="text-sm text-gray-600 max-w-md truncate">
                              {category.description || 'Sin descripción'}
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="text-xs text-gray-500">
                              {category.membersPerTeam && (
                                <div className="font-medium">
                                  {category.membersPerTeam} miembro{category.membersPerTeam !== 1 ? 's' : ''} por equipo
                                </div>
                              )}
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span
                              className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${
                                category.isActive
                                  ? 'bg-green-100 text-green-700'
                                  : 'bg-red-100 text-red-700'
                              }`}
                            >
                              {category.isActive ? 'Activa' : 'Inactiva'}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-semibold">
                            {/* 🔒 PROTEGER BOTONES DE ACCIÓN */}
                            <PermissionGate permission="tournaments.edit">
                              <button
                                onClick={() => {
                                  setSelectedCategory(category.id);
                                  setShowModal(true);
                                }}
                                className="text-indigo-600 hover:text-indigo-900 mr-3 transition-colors"
                              >
                                Editar
                              </button>
                            </PermissionGate>

                            {category.isActive ? (
                              <PermissionGate permission="tournaments.delete">
                                <button
                                  onClick={() => handleDelete(category.id)}
                                  className="text-red-600 hover:text-red-800 transition-colors"
                                >
                                  Desactivar
                                </button>
                              </PermissionGate>
                            ) : (
                              <PermissionGate permission="tournaments.edit">
                                <button
                                  onClick={() => handleReactivate(category.id)}
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
              </div>
            );
          })}
        </div>
      )}

      {/* 🔒 PROTEGER MODAL */}
      {showModal && (
        <PermissionGate permission="tournaments.create">
          <Modal
            isOpen={showModal}
            onClose={() => {
              setShowModal(false);
              setSelectedCategory(undefined);
            }}
            title={selectedCategory ? 'Editar Categoría' : 'Crear Nueva Categoría'}
            size="md"
          >
            <CategoryForm
              categoryId={selectedCategory}
              sports={sports}
              onSuccess={() => {
                setShowModal(false);
                setSelectedCategory(undefined);
                fetchData();
              }}
              onCancel={() => {
                setShowModal(false);
                setSelectedCategory(undefined);
              }}
            />
          </Modal>
        </PermissionGate>
      )}
    </div>
  );
}