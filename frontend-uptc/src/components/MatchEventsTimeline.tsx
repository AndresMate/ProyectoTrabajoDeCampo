// frontend-uptc/src/components/MatchEventsTimeline.tsx
'use client';

import { MatchEvent } from '@/services/matchEventsService';

interface MatchEventsTimelineProps {
  events: MatchEvent[];
  match: any;
  onDeleteEvent: (eventId: number) => void;
}

export default function MatchEventsTimeline({
  events,
  match,
  onDeleteEvent
}: MatchEventsTimelineProps) {
  const sortedEvents = [...events].sort((a, b) => b.minute - a.minute);

  const getEventIcon = (type: string) => {
    switch (type) {
      case 'GOAL': return '⚽';
      case 'PENALTY': return '🎯';
      case 'OWN_GOAL': return '😔';
      case 'YELLOW_CARD': return '🟨';
      case 'RED_CARD': return '🟥';
      case 'SUBSTITUTION': return '🔄';
      default: return '📝';
    }
  };

  const getEventColor = (type: string) => {
    switch (type) {
      case 'GOAL':
      case 'PENALTY': return 'border-green-500 bg-green-50';
      case 'OWN_GOAL': return 'border-orange-500 bg-orange-50';
      case 'YELLOW_CARD': return 'border-yellow-500 bg-yellow-50';
      case 'RED_CARD': return 'border-red-500 bg-red-50';
      case 'SUBSTITUTION': return 'border-blue-500 bg-blue-50';
      default: return 'border-gray-300 bg-gray-50';
    }
  };

  const getEventLabel = (type: string) => {
    switch (type) {
      case 'GOAL': return 'Gol';
      case 'PENALTY': return 'Penal';
      case 'OWN_GOAL': return 'Autogol';
      case 'YELLOW_CARD': return 'Tarjeta Amarilla';
      case 'RED_CARD': return 'Tarjeta Roja';
      case 'SUBSTITUTION': return 'Sustitución';
      default: return type;
    }
  };

  if (events.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow p-6 text-center">
        <p className="text-gray-500">No hay eventos registrados aún</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-xl font-bold text-gray-800 mb-4">
        📜 Timeline del Partido
      </h2>

      <div className="space-y-3">
        {sortedEvents.map((event) => (
          <div
            key={event.id}
            className={`border-l-4 rounded-lg p-4 ${getEventColor(event.eventType)}`}
          >
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  <span className="text-2xl">{getEventIcon(event.eventType)}</span>
                  <div>
                    <span className="font-bold text-gray-800">
                      {event.minute}' - {getEventLabel(event.eventType)}
                    </span>
                    <div className="text-sm text-gray-600">
                      {event.player?.fullName} ({event.team?.name})
                    </div>
                    {event.description && (
                      <div className="text-sm text-gray-500 italic mt-1">
                        {event.description}
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {match.status === 'IN_PROGRESS' && (
                <button
                  onClick={() => event.id && onDeleteEvent(event.id)}
                  className="text-red-500 hover:text-red-700 text-sm font-medium"
                >
                  Eliminar
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}