'use client';

import { useState, useEffect } from 'react';
import teamsService from '@/services/teamsService';
import { toastError } from '@/utils/toast';

interface ClubInfo {
  id: number;
  name: string;
}

interface TournamentInfo {
  id: number;
  name: string;
}

interface PlayerSummary {
  id: number;
  fullName: string;
  documentNumber: string;
}

interface RosterItem {
  player: PlayerSummary;
  jerseyNumber?: number;
  isCaptain?: boolean;
}

interface TeamData {
  id: number;
  name: string;
  club?: ClubInfo;
  tournament?: TournamentInfo;
  roster?: RosterItem[];
}

interface TeamRosterModalProps {
  teamId: number;
  teamName: string;
  onClose: () => void;
}

export default function TeamRosterModal({ teamId, teamName, onClose }: TeamRosterModalProps) {
  const [teamData, setTeamData] = useState<TeamData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRoster();
  }, [teamId]);

  const fetchRoster = async () => {
    setLoading(true);
    try {
      const data = await teamsService.getRoster(teamId);
      setTeamData(data);
    } catch (error) {
      console.error('❌ Error al cargar roster:', error);
      // El error ya se muestra en el interceptor de axios
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow mx-auto" />
          <p className="text-center mt-4 text-gray-600">Cargando roster...</p>
        </div>
      </div>
    );
  }

  if (!teamData) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        {/* Encabezado */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <div>
            <h2 className="text-2xl font-bold text-gray-800">{teamData.name}</h2>
            <p className="text-sm text-gray-600">
              {teamData.club ? `Club: ${teamData.club.name}` : 'Sin club asignado'}
            </p>
            <p className="text-sm text-gray-600">
              {teamData.tournament ? `Torneo: ${teamData.tournament.name}` : 'Sin torneo'}
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-2xl"
            aria-label="Cerrar modal"
            type="button"
          >
            ×
          </button>
        </div>

        {/* Cuerpo */}
        <div className="p-6">
          {!teamData.roster || teamData.roster.length === 0 ? (
            <p className="text-gray-600 text-center">No hay jugadores en el roster.</p>
          ) : (
            <ul className="space-y-4">
              {teamData.roster.map((r, index) => (
                <li
                  key={`${r.player?.id || index}`}
                  className="flex items-center justify-between p-3 border rounded"
                >
                  <div>
                    <div className="font-medium text-gray-800">
                      {r.player?.fullName || 'Sin nombre'}
                    </div>
                    <div className="text-sm text-gray-500">
                      Documento: {r.player?.documentNumber || '—'}
                    </div>
                    {r.jerseyNumber != null && (
                      <div className="text-sm text-gray-500">
                        Camiseta: #{r.jerseyNumber}
                      </div>
                    )}
                  </div>

                  <div className="flex items-center gap-3">
                    {r.isCaptain && (
                      <span className="text-sm bg-yellow-100 text-yellow-800 px-2 py-1 rounded">
                        Capitán
                      </span>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
