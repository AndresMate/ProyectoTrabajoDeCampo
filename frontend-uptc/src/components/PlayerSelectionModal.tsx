// frontend-uptc/src/components/PlayerSelectionModal.tsx
'use client';

import { useEffect, useState } from 'react';
import inscriptionPlayerService from '@/services/inscriptionPlayerService';
import playersService from '@/services/playersService';

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
  const [currentPlayers, setCurrentPlayers] = useState<any[]>([]);
  const [availablePlayers, setAvailablePlayers] = useState<any[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [inscriptionId]);

  const loadData = async () => {
    try {
      const [playersData, availableData] = await Promise.all([
        inscriptionPlayerService.getByInscription(inscriptionId),
        playersService.search({ isActive: true })
      ]);

      setCurrentPlayers(playersData);
      setAvailablePlayers(availableData.content || availableData);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddPlayer = async (playerId: number) => {
    // ⚡ VALIDAR LÍMITE EN EL FRONTEND
    if (currentPlayers.length >= maxPlayers) {
      alert(`Ya se alcanzó el límite de ${maxPlayers} jugadores`);
      return;
    }

    try {
      await inscriptionPlayerService.addPlayer(inscriptionId, playerId);
      alert('✅ Jugador agregado');
      loadData();
      onPlayersUpdated();
    } catch (error: any) {
      alert(error.message || 'Error al agregar jugador');
    }
  };

  const handleRemovePlayer = async (playerId: number) => {
    if (!confirm('¿Eliminar este jugador?')) return;

    try {
      await inscriptionPlayerService.removePlayer(inscriptionId, playerId);
      alert('✅ Jugador eliminado');
      loadData();
      onPlayersUpdated();
    } catch (error) {
      alert('Error al eliminar jugador');
    }
  };

  const filteredPlayers = availablePlayers.filter(p =>
    p.fullName.toLowerCase().includes(searchTerm.toLowerCase()) &&
    !currentPlayers.some(cp => cp.id === p.id)
  );

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold">Gestionar Jugadores</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-700 text-2xl">
            ×
          </button>
        </div>

        {/* ⚡ CONTADOR CON LÍMITE */}
        <div className="bg-blue-50 border-l-4 border-yellow-400 p-4 mb-4">
          <p className="font-semibold">
            Jugadores: {currentPlayers.length} / {maxPlayers}
          </p>
          {currentPlayers.length >= maxPlayers && (
            <p className="text-red-600 text-sm mt-1">
              ⚠️ Se alcanzó el límite de jugadores
            </p>
          )}
        </div>

        {/* Jugadores actuales */}
        <div className="mb-6">
          <h3 className="font-semibold mb-3">Jugadores Actuales</h3>
          {currentPlayers.length === 0 ? (
            <p className="text-gray-500">No hay jugadores agregados</p>
          ) : (
            <div className="space-y-2">
              {currentPlayers.map(player => (
                <div key={player.id} className="flex justify-between items-center p-3 bg-gray-50 rounded">
                  <div>
                    <p className="font-semibold">{player.fullName}</p>
                    <p className="text-sm text-gray-600">{player.documentNumber}</p>
                  </div>
                  <button
                    onClick={() => handleRemovePlayer(player.id)}
                    className="text-red-600 hover:text-red-800 font-semibold"
                  >
                    Eliminar
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Búsqueda de jugadores */}
        {currentPlayers.length < maxPlayers && (
          <div>
            <h3 className="font-semibold mb-3">Agregar Jugador</h3>
            <input
              type="text"
              placeholder="Buscar por nombre..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-4 py-2 border rounded-lg mb-3"
            />

            <div className="space-y-2 max-h-64 overflow-y-auto">
              {filteredPlayers.map(player => (
                <div key={player.id} className="flex justify-between items-center p-3 border rounded">
                  <div>
                    <p className="font-semibold">{player.fullName}</p>
                    <p className="text-sm text-gray-600">{player.studentCode}</p>
                  </div>
                  <button
                    onClick={() => handleAddPlayer(player.id)}
                    className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
                  >
                    Agregar
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}