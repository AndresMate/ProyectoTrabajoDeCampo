// src/components/TournamentTabs.tsx
'use client';

import Link from 'next/link';

interface TournamentTabsProps {
  tournamentId: number;
  tournament?: any;
  activeTab: 'equipos' | 'inscripciones' | 'partidos';
}

export default function TournamentTabs({ tournamentId, tournament, activeTab }: TournamentTabsProps) {
  const tabs = [
    { id: 'equipos', href: `/admin/torneos/${tournamentId}/equipos`, label: 'Equipos', icon: '⚽' },
    { id: 'inscripciones', href: `/admin/torneos/${tournamentId}/inscripciones`, label: 'Inscripciones', icon: '📋' },
    { id: 'partidos', href: `/admin/torneos/${tournamentId}/partidos`, label: 'Partidos', icon: '📅' },
  ];

  const getStatusBadge = (status: string) => {
    const colors: Record<string, string> = {
      PLANNING: 'bg-gray-200 text-gray-700',
      OPEN_FOR_INSCRIPTION: 'bg-blue-200 text-blue-700',
      IN_PROGRESS: 'bg-green-200 text-green-700',
      FINISHED: 'bg-purple-200 text-purple-700',
      CANCELLED: 'bg-red-200 text-red-700',
    };
    return colors[status] || 'bg-gray-200 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts: Record<string, string> = {
      PLANNING: 'Planificación',
      OPEN_FOR_INSCRIPTION: 'Inscripción',
      IN_PROGRESS: 'En curso',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado',
    };
    return texts[status] || status;
  };

  return (
    <div className="mb-6">
      {/* Breadcrumb */}
      <div className="mb-4">
        <Link
          href="/admin/torneos"
          className="text-uptc-yellow hover:text-yellow-600 font-semibold inline-flex items-center gap-2"
        >
          ← Volver a torneos
        </Link>
      </div>

      {/* Información del Torneo */}
      {tournament && (
        <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
          <div className="flex justify-between items-start">
            <div className="flex-1">
              <h1 className="text-2xl font-bold text-gray-800 mb-2">{tournament.name}</h1>

              {tournament.description && (
                <p className="text-gray-600 text-sm mb-3">{tournament.description}</p>
              )}

              <div className="flex flex-wrap gap-4 text-sm text-gray-700">
                <div>
                  <span className="font-semibold">Deporte:</span> {tournament.sport?.name || 'N/A'}
                </div>
                <div>
                  <span className="font-semibold">Inicio:</span>{' '}
                  {new Date(tournament.startDate).toLocaleDateString('es-ES')}
                </div>
                <div>
                  <span className="font-semibold">Fin:</span>{' '}
                  {new Date(tournament.endDate).toLocaleDateString('es-ES')}
                </div>
              </div>
            </div>

            <span
              className={`inline-flex px-4 py-2 rounded-full text-sm font-semibold whitespace-nowrap ${getStatusBadge(
                tournament.status
              )}`}
            >
              {getStatusText(tournament.status)}
            </span>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="bg-white rounded-lg shadow">
        <nav className="flex border-b border-gray-200">
          {tabs.map((tab) => {
            const isActive = activeTab === tab.id;
            return (
              <Link
                key={tab.id}
                href={tab.href}
                className={`flex-1 inline-flex items-center justify-center gap-2 px-6 py-4 border-b-2 transition-all ${
                  isActive
                    ? 'border-uptc-yellow text-uptc-black font-bold bg-yellow-50'
                    : 'border-transparent text-gray-600 hover:border-uptc-yellow hover:text-gray-900 hover:bg-gray-50'
                }`}
              >
                <span className="text-xl">{tab.icon}</span>
                <span className="font-semibold">{tab.label}</span>
              </Link>
            );
          })}
        </nav>
      </div>
    </div>
  );
}
