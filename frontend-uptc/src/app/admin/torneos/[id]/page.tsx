// src/app/admin/torneos/
//
// [matchId]/equipos/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import teamsService from '@/services/teamsService';
import { tournamentsService } from '@/services/tournamentsService';
import TournamentTabs from '@/components/TournamentTabs';
import Modal from '@/components/Modal';
import TeamForm from '@/components/forms/TeamForm';
import TeamRosterModal from '@/components/TeamRosterModal';
import axios from "axios";

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

export default function TournamentTeamsPage() {
  const params = useParams();
  const tournamentId = Number(params.id);

  const [tournament, setTournament] = useState<any>(null);
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedTeam, setSelectedTeam] = useState<number | undefined>();
  const [showRosterModal, setShowRosterModal] = useState(false);
  const [rosterTeam, setRosterTeam] = useState<{ id: number; name: string } | null>(null);

  useEffect(() => {
    fetchData();
  }, [tournamentId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [tournamentData, teamsData] = await Promise.all([
        tournamentsService.getById(tournamentId),
        teamsService.getAll()
      ]);

      setTournament(tournamentData);

      // Filtrar equipos por este torneo
      const allTeams = teamsData.content || teamsData;
      const filteredTeams = allTeams.filter((t: Team) =>
        t.tournamentName === tournamentData.name
      );

      setTeams(filteredTeams);
    } catch (error) {
      console.error('Error al cargar datos:', error);
      alert('Error al cargar los datos del torneo');
    } finally {
      setLoading(false);
    }
  };

  const handleViewRoster = (team: Team) => {
    setRosterTeam({ id: team.id, name: team.name });
    setShowRosterModal(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Estás seguro de eliminar este equipo?')) return;

    try {
      await teamsService.delete(id);
      alert('Equipo eliminado exitosamente');
      fetchData();
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        const message = error.response?.data?.message ?? error.message;
        alert(message || 'Error al eliminar equipo');
      } else if (error instanceof Error) {
        alert(error.message);
      } else {
        alert('Error al eliminar equipo');
      }
      console.error('Error eliminando equipo:', error);
    }
  };

  const filteredTeams = teams.filter(
    (team) =>
      team.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (team.delegateName && team.delegateName.toLowerCase().includes(searchTerm.toLowerCase()))
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
      <TournamentTabs tournamentId={tournamentId} tournament={tournament} activeTab="equipos" />

      <div className="bg-white rounded-lg shadow-lg p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">Equipos Inscritos</h2>
          <button
            onClick={() => {
              setSelectedTeam(undefined);
              setShowModal(true);
            }}
            className="bg-uptc-black text-uptc-yellow px-6 py-2 rounded-lg hover:bg-gray-800 transition font-semibold"
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
            className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900 font-medium"
          />
        </div>

        {/* Estadísticas rápidas */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg shadow p-4 border-l-4 border-blue-500">
            <div className="text-blue-600 text-sm font-semibold">Total Equipos</div>
            <div className="text-3xl font-bold text-blue-900">{teams.length}</div>
          </div>
          <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-lg shadow p-4 border-l-4 border-green-500">
            <div className="text-green-600 text-sm font-semibold">Equipos Activos</div>
            <div className="text-3xl font-bold text-green-900">
              {teams.filter(t => t.isActive).length}
            </div>
          </div>
          <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-lg shadow p-4 border-l-4 border-red-500">
            <div className="text-red-600 text-sm font-semibold">Equipos Inactivos</div>
            <div className="text-3xl font-bold text-red-900">
              {teams.filter(t => !t.isActive).length}
            </div>
          </div>
        </div>

        {/* Lista de equipos */}
        {filteredTeams.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-uptc-black">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">Equipo</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">Delegado</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase">Estado</th>
                  <th className="px-6 py-3 text-right text-xs font-semibold text-uptc-yellow uppercase">Acciones</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredTeams.map((team) => (
                  <tr key={team.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="font-semibold text-gray-900">{team.name}</div>
                      <div className="text-sm text-gray-600 font-medium">{team.clubName || 'Sin club'}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-900 font-medium">{team.delegateName || 'N/A'}</div>
                      <div className="text-sm text-gray-600">{team.delegateEmail || 'N/A'}</div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${
                        team.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                      }`}>
                        {team.isActive ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right text-sm font-semibold">
                      <button
                        onClick={() => handleViewRoster(team)}
                        className="text-blue-600 hover:text-blue-800 mr-3"
                      >
                        👥 Ver Roster
                      </button>
                      <button
                        onClick={() => {
                          setSelectedTeam(team.id);
                          setShowModal(true);
                        }}
                        className="text-indigo-600 hover:text-indigo-900 mr-3"
                      >
                        ✏️ Editar
                      </button>
                      <button
                        onClick={() => handleDelete(team.id)}
                        className="text-red-600 hover:text-red-900"
                      >
                        🗑️ Eliminar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-gray-500 font-medium">
              {searchTerm ? 'No se encontraron equipos con ese criterio' : 'No hay equipos inscritos en este torneo'}
            </p>
          </div>
        )}
      </div>

      {/* Modal crear/editar equipo */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setSelectedTeam(undefined);
        }}
        title={selectedTeam ? 'Editar Equipo' : 'Crear Nuevo Equipo'}
        size="lg"
      >
        <TeamForm
          teamId={selectedTeam}
          tournamentId={tournamentId}
          onSuccess={() => {
            setShowModal(false);
            setSelectedTeam(undefined);
            fetchData();
          }}
          onCancel={() => {
            setShowModal(false);
            setSelectedTeam(undefined);
          }}
        />
      </Modal>

      {/* Modal ver roster */}
      {showRosterModal && rosterTeam && (
        <TeamRosterModal
          teamId={rosterTeam.id}
          teamName={rosterTeam.name}
          onClose={() => {
            setShowRosterModal(false);
            setRosterTeam(null);
          }}
        />
      )}
    </div>
  );
}