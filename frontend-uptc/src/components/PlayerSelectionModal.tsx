'use client';

import { useEffect, useState } from 'react';
import playersService from '@/services/playersService';
import inscriptionPlayerService from '@/services/inscriptionPlayerService';

interface PlayerSelectionModalProps {
  inscriptionId: number;
  maxPlayers: number;
  onClose: () => void;
  onPlayersUpdated: () => void;
}

export default function PlayerSelectionModal({
  inscriptionId,
  maxPlayers,
  onClose,
  onPlayersUpdated
}: PlayerSelectionModalProps) {
  const [availablePlayers, setAvailablePlayers] = useState<any[]>([]);
  const [assignedPlayers, setAssignedPlayers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPlayers();
  }, [inscriptionId]);

  const fetchPlayers = async () => {
    setLoading(true);
    try {
      // 🔹 Jugadores disponibles (no inscritos aún)
      const available = await playersService.getAvailablePlayers();
      // 🔹 Jugadores ya asignados a esta inscripción
      const assigned = await inscriptionPlayerService.getByInscription(inscriptionId);

      setAvailablePlayers(available);
      setAssignedPlayers(assigned);
    } catch (error) {
      console.error('Error al cargar jugadores:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddPlayer = async (playerId: number) => {
    if (assignedPlayers.length >= maxPlayers) {
      alert(`No puedes agregar más de ${maxPlayers} jugadores.`);
      return;
    }
    try {
      await inscriptionPlayerService.addPlayer(inscriptionId, playerId);
      await fetchPlayers();
      onPlayersUpdated();
    } catch (error: any) {
      alert(error.message || 'Error al agregar jugador.');
      console.error('Error al agregar jugador:', error);
    }
  };

  const handleRemovePlayer = async (playerId: number) => {
    try {
      await inscriptionPlayerService.removePlayer(inscriptionId, playerId);
      await fetchPlayers();
      onPlayersUpdated();
    } catch (error) {
      console.error('Error al eliminar jugador:', error);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-3xl p-6 relative">
        <h2 className="text-xl font-semibold mb-4 text-blue-900">Gestión de Jugadores</h2>

        {loading ? (
          <p>Cargando jugadores...</p>
        ) : (
          <div className="grid grid-cols-2 gap-6">
            {/* Jugadores disponibles */}
            <div>
              <h3 className="font-semibold mb-2">Disponibles</h3>
              <ul className="border rounded p-2 max-h-64 overflow-y-auto">
                {availablePlayers.map((p) => (
                  <li key={p.id} className="flex justify-between items-center border-b py-1">
                    <span>{p.fullName}</span>
                    <button
                      onClick={() => handleAddPlayer(p.id)}
                      className="text-sm bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700"
                    >
                      Agregar
                    </button>
                  </li>
                ))}
              </ul>
            </div>

            {/* Jugadores asignados */}
            <div>
              <h3 className="font-semibold mb-2">
                Asignados ({assignedPlayers.length}/{maxPlayers})
              </h3>
              <ul className="border rounded p-2 max-h-64 overflow-y-auto">
                {assignedPlayers.map((p) => (
                  <li key={p.id} className="flex justify-between items-center border-b py-1">
                    <span>{p.fullName}</span>
                    <button
                      onClick={() => handleRemovePlayer(p.id)}
                      className="text-sm bg-red-600 text-white px-2 py-1 rounded hover:bg-red-700"
                    >
                      Quitar
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        <button
          onClick={onClose}
          className="mt-6 bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500 w-full"
        >
          Cerrar
        </button>
      </div>
    </div>
  );
}
