'use client';

import { useState, useEffect } from 'react';
import teamsService from '@/services/teamsService';

interface Player {
  id: number;
  fullName: string;
  documentNumber: string;
  jerseyNumber?: number;
  isCaptain?: boolean;
}

interface TeamRosterModalProps {
  teamId: number;
  teamName: string;
  onClose: () => void;
}

export default function TeamRosterModal({ teamId, teamName, onClose }: TeamRosterModalProps) {
  const [roster, setRoster] = useState<Player[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRoster();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [teamId]);

  const normalizePlayer = (p: any): Player => {
    const fullName =
      p.fullName ||
      [p.firstName, p.lastName].filter(Boolean).join(' ') ||
      p.name ||
      p.username ||
      'Sin nombre';
    const documentNumber = p.documentNumber ?? p.document ?? p.dni ?? '';
    const jerseyNumber = p.jerseyNumber ?? p.number ?? p.shirtNumber;
    const isCaptain = !!(p.isCaptain ?? p.captain ?? p.is_team_captain);
    const id = p.id ?? p.playerId ?? 0;
    return { id, fullName, documentNumber, jerseyNumber, isCaptain };
  };

  const fetchRoster = async () => {
    setLoading(true);
    try {
      const data: any[] = await teamsService.getRoster(teamId);
      const normalized = Array.isArray(data) ? data.map(normalizePlayer) : [];
      setRoster(normalized);
    } catch (error) {
      console.error('Error al cargar roster:', error);
      alert('Error al cargar el roster del equipo');
    } finally {
      setLoading(false);
    }
  };

  const handleSetCaptain = async (playerId: number) => {
    if (!confirm('¿Asignar este jugador como capitán?')) return;

    try {
      await teamsService.setCaptain(teamId, playerId);
      alert('Capitán asignado exitosamente');
      fetchRoster();
    } catch (error: any) {
      alert(error?.response?.data?.message || 'Error al asignar capitán');
    }
  };

  const handleRemovePlayer = async (playerId: number) => {
    if (!confirm('¿Eliminar este jugador del equipo?')) return;

    try {
      await teamsService.removePlayer(teamId, playerId);
      alert('Jugador eliminado del roster');
      fetchRoster();
    } catch (error: any) {
      alert(error?.response?.data?.message || 'Error al eliminar jugador');
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

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <div>
            <h2 className="text-2xl font-bold text-gray-800">{teamName}</h2>
            <p className="text-sm text-gray-600">
              {roster.length} jugador{roster.length !== 1 ? 'es' : ''} en el roster
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

        <div className="p-6">
          {roster.length === 0 ? (
            <p className="text-gray-600">No hay jugadores en el roster.</p>
          ) : (
            <ul className="space-y-4">
              {roster.map((player) => (
                <li key={player.id} className="flex items-center justify-between p-3 border rounded">
                  <div>
                    <div className="font-medium text-gray-800">{player.fullName}</div>
                    <div className="text-sm text-gray-500">Documento: {player.documentNumber || '—'}</div>
                    {player.jerseyNumber != null && (
                      <div className="text-sm text-gray-500">Camiseta: #{player.jerseyNumber}</div>
                    )}
                  </div>

                  <div className="flex items-center gap-3">
                    {player.isCaptain && (
                      <span className="text-sm bg-yellow-100 text-yellow-800 px-2 py-1 rounded">Capitán</span>
                    )}
                    <button
                      onClick={() => handleSetCaptain(player.id)}
                      className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-uptc-black text-sm"
                    >
                      Asignar cap.
                    </button>
                    <button
                      onClick={() => handleRemovePlayer(player.id)}
                      className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 text-sm"
                    >
                      Eliminar
                    </button>
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
