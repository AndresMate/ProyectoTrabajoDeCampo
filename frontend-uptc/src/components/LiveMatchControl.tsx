// frontend-uptc/src/components/LiveMatchControl.tsx
'use client';

import { useState } from 'react';
import { MatchEvent } from '@/services/matchEventsService';

interface LiveMatchControlProps {
  match: any;
  homeRoster: any[];
  awayRoster: any[];
  currentMinute: number;
  onAddEvent: (event: MatchEvent) => void;
}

export default function LiveMatchControl({
  match,
  homeRoster,
  awayRoster,
  currentMinute,
  onAddEvent
}: LiveMatchControlProps) {
  const [selectedTeam, setSelectedTeam] = useState<'home' | 'away'>('home');
  const [selectedPlayer, setSelectedPlayer] = useState<number>(0);
  const [eventType, setEventType] = useState<string>('GOAL');
  const [minute, setMinute] = useState<number>(currentMinute);
  const [description, setDescription] = useState<string>('');

  const eventTypes = [
    { value: 'GOAL', label: '⚽ Gol', color: 'bg-green-600' },
    { value: 'PENALTY', label: '🎯 Penal', color: 'bg-green-700' },
    { value: 'OWN_GOAL', label: '😔 Autogol', color: 'bg-orange-600' },
    { value: 'YELLOW_CARD', label: '🟨 Tarjeta Amarilla', color: 'bg-yellow-500' },
    { value: 'RED_CARD', label: '🟥 Tarjeta Roja', color: 'bg-red-600' },
    { value: 'SUBSTITUTION', label: '🔄 Sustitución', color: 'bg-blue-600' }
  ];

  const currentRoster = selectedTeam === 'home' ? homeRoster : awayRoster;
  const teamId = selectedTeam === 'home' ? match.homeTeam.id : match.awayTeam.id;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedPlayer) {
      alert('Selecciona un jugador');
      return;
    }

    // ✅ Estructura correcta esperada por el padre
    const newEvent: MatchEvent = {
      matchId: match.id,
      team: { id: teamId },
      player: { id: selectedPlayer },
      type: eventType as any,
      minute: minute || currentMinute,
      description: description || '',
    };

    onAddEvent(newEvent);

    // Resetear formulario
    setSelectedPlayer(0);
    setDescription('');
    setMinute(currentMinute);
  };

  return (
    <div className="bg-white rounded-lg shadow p-6 mb-6">
      <h2 className="text-xl font-bold text-gray-800 mb-4">
        🎮 Registrar Evento
      </h2>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Selector de equipo */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Equipo
          </label>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => {
                setSelectedTeam('home');
                setSelectedPlayer(0);
              }}
              className={`p-3 rounded-lg font-medium transition ${
                selectedTeam === 'home'
                  ? 'bg-uptc-black text-uptc-yellow'
                  : 'bg-gray-100 hover:bg-gray-200'
              }`}
            >
              🏠 {match.homeTeam.name}
            </button>
            <button
              type="button"
              onClick={() => {
                setSelectedTeam('away');
                setSelectedPlayer(0);
              }}
              className={`p-3 rounded-lg font-medium transition ${
                selectedTeam === 'away'
                  ? 'bg-uptc-black text-uptc-yellow'
                  : 'bg-gray-100 hover:bg-gray-200'
              }`}
            >
              ✈️ {match.awayTeam.name}
            </button>
          </div>
        </div>

        {/* Tipo de evento */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Tipo de Evento
          </label>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
            {eventTypes.map(type => (
              <button
                key={type.value}
                type="button"
                onClick={() => setEventType(type.value)}
                className={`p-3 rounded-lg text-white font-medium transition ${
                  eventType === type.value
                    ? type.color + ' ring-2 ring-offset-2 ring-uptc-yellow'
                    : type.color + ' opacity-50 hover:opacity-75'
                }`}
              >
                {type.label}
              </button>
            ))}
          </div>
        </div>

        {/* Jugador */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Jugador *
          </label>
          <select
            value={selectedPlayer}
            onChange={(e) => setSelectedPlayer(Number(e.target.value))}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow"
            required
          >
            <option value="">-- Selecciona un jugador --</option>
            {currentRoster.map((item: any) => (
              <option key={item.player?.id} value={item.player?.id}>
                {item.jerseyNumber ? `#${item.jerseyNumber}` : ''} {item.player?.fullName}
                {item.isCaptain ? ' (C)' : ''}
              </option>
            ))}
          </select>
        </div>

        {/* Minuto y descripción */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Minuto
            </label>
            <input
              type="number"
              value={minute}
              onChange={(e) => setMinute(Number(e.target.value))}
              min="0"
              max="120"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Descripción (opcional)
            </label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Ej: Gol de cabeza"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow"
            />
          </div>
        </div>

        {/* Botón de enviar */}
        <button
          type="submit"
          className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 rounded-lg transition"
        >
          ➕ Registrar Evento
        </button>
      </form>
    </div>
  );
}
