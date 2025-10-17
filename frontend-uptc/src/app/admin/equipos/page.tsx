'use client';

import { useEffect, useState } from 'react';
import teamsService from '@/services/teamsService';

interface Team {
  id: number;
  name: string;
  clubName?: string;
  tournamentName?: string;
  categoryName?: string;
  delegateName: string;
  delegateEmail: string;
  isActive: boolean;
}

export default function AdminEquiposPage() {
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);

  useEffect(() => {
    fetchTeams();
  }, []);

  const fetchTeams = async () => {
    try {
      const data = await teamsService.getAll();
      setTeams(data.content || data);
    } catch (error) {
      console.error('Error al cargar equipos:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewRoster = async (teamId: number) => {
    try {
      const roster = await teamsService.getRoster(teamId);
      alert(`Jugadores: ${JSON.stringify(roster, null, 2)}`);
    } catch (error) {
      console.error('Error al cargar roster:', error);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Estás seguro de eliminar este equipo?')) return;

    try {
      await teamsService.delete(id);
      alert('Equipo eliminado exitosamente');
      fetchTeams();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Error al eliminar equipo');
    }
  };

  const filteredTeams = teams.filter(
    (team) =>
      team.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      team.delegateName.toLowerCase().includes(searchTerm.toLowerCase())
  );

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
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Equipos</h1>
        <button
          onClick={() => setShowModal(true)}
          className="bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800 transition"
        >
          + Nuevo Equipo
        </button>
      </div>

      {/* Barra de búsqueda */}
      <div className="mb-6">
        <input
          type="text"
          placeholder="Buscar por nombre de equipo o delegado..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* Estadísticas rápidas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Total Equipos</div>
          <div className="text-2xl font-bold text-blue-900">{teams.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Equipos Activos</div>
          <div className="text-2xl font-bold text-green-600">
            {teams.filter(t => t.isActive).length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Equipos Inactivos</div>
          <div className="text-2xl font-bold text-red-600">
            {teams.filter(t => !t.isActive).length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Sin Torneo</div>
          <div className="text-2xl font-bold text-gray-600">
            {teams.filter(t => !t.tournamentName).length}
          </div>
        </div>
      </div>

      {/* Lista de equipos */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Equipo</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Torneo</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Delegado</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Acciones</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredTeams.map((team) => (
              <tr key={team.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="font-medium text-gray-900">{team.name}</div>
                  <div className="text-sm text-gray-500">{team.clubName || 'Sin club'}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">{team.tournamentName || 'N/A'}</div>
                  <div className="text-sm text-gray-500">{team.categoryName || 'N/A'}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">{team.delegateName}</div>
                  <div className="text-sm text-gray-500">{team.delegateEmail}</div>
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${
                    team.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                  }`}>
                    {team.isActive ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td className="px-6 py-4 text-right text-sm font-medium">
                  <button
                    onClick={() => handleViewRoster(team.id)}
                    className="text-blue-600 hover:text-blue-900 mr-3"
                  >
                    Ver Roster
                  </button>
                  <button
                    onClick={() => setSelectedTeam(team)}
                    className="text-indigo-600 hover:text-indigo-900 mr-3"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => handleDelete(team.id)}
                    className="text-red-600 hover:text-red-900"
                  >
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredTeams.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center mt-6">
          <p className="text-gray-500">No se encontraron equipos</p>
        </div>
      )}

      {/* Modal crear/editar (placeholder) */}
      {(showModal || selectedTeam) && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 max-w-2xl w-full">
            <h2 className="text-2xl font-bold mb-4">
              {selectedTeam ? 'Editar Equipo' : 'Crear Nuevo Equipo'}
            </h2>
            <p className="text-gray-600 mb-4">Formulario por implementar</p>
            <button
              onClick={() => {
                setShowModal(false);
                setSelectedTeam(null);
              }}
              className="bg-gray-300 px-4 py-2 rounded hover:bg-gray-400"
            >
              Cerrar
            </button>
          </div>
        </div>
      )}
    </div>
  );
}