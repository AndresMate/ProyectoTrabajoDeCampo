'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';

export default function TournamentDetailPage() {
  const { id } = useParams();
  const [tournament, setTournament] = useState<any>(null);
  const [inscriptions, setInscriptions] = useState<any[]>([]);
  const [matches, setMatches] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState<'info' | 'inscriptions' | 'matches'>('info');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      fetchTournamentData();
    }
  }, [id]);

  const fetchTournamentData = async () => {
    try {
      const [tournamentData, inscriptionsData, matchesData] = await Promise.all([
        tournamentsService.getById(id as string),
        tournamentsService.getInscriptions(id as string).catch(() => []),
        tournamentsService.getMatches(id as string).catch(() => [])
      ]);
      
      setTournament(tournamentData);
      setInscriptions(inscriptionsData);
      setMatches(matchesData);
    } catch (error) {
      console.error('Error al obtener datos del torneo:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-900"></div>
      </div>
    );
  }

  if (!tournament) {
    return (
      <main className="flex flex-col items-center min-h-screen py-10 bg-gray-50">
        <div className="max-w-4xl w-full bg-white p-8 rounded-lg shadow text-center">
          <h1 className="text-2xl font-bold text-red-600 mb-4">Torneo no encontrado</h1>
          <p className="text-gray-600">No se pudo encontrar la información del torneo solicitado.</p>
          <a href="/torneos" className="mt-4 inline-block bg-blue-900 text-white px-6 py-2 rounded hover:bg-blue-800">
            Volver a torneos
          </a>
        </div>
      </main>
    );
  }

  const getStatusColor = (status: string) => {
    const colors: any = {
      PLANNING: 'bg-gray-200 text-gray-700',
      REGISTRATION: 'bg-blue-200 text-blue-700',
      IN_PROGRESS: 'bg-green-200 text-green-700',
      FINISHED: 'bg-purple-200 text-purple-700',
      CANCELLED: 'bg-red-200 text-red-700'
    };
    return colors[status] || 'bg-gray-200 text-gray-700';
  };

  return (
    <main className="min-h-screen py-10 bg-gray-50">
      <div className="max-w-6xl mx-auto px-4">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-lg p-8 mb-6">
          <div className="flex justify-between items-start mb-4">
            <div>
              <h1 className="text-4xl font-bold text-blue-900 mb-2">
                {tournament.name}
              </h1>
              <p className="text-gray-600">
                {tournament.sport?.name} • {tournament.category?.name}
              </p>
            </div>
            <span className={`px-4 py-2 rounded-full text-sm font-semibold ${getStatusColor(tournament.status)}`}>
              {tournament.status}
            </span>
          </div>

          <p className="text-gray-700 mb-6">{tournament.description}</p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-blue-50 p-4 rounded">
              <div className="text-sm text-blue-600 font-medium">Fecha de inicio</div>
              <div className="text-lg font-semibold text-gray-800">
                {new Date(tournament.startDate).toLocaleDateString('es-ES', {
                  day: 'numeric',
                  month: 'long',
                  year: 'numeric'
                })}
              </div>
            </div>
            <div className="bg-blue-50 p-4 rounded">
              <div className="text-sm text-blue-600 font-medium">Fecha de finalización</div>
              <div className="text-lg font-semibold text-gray-800">
                {new Date(tournament.endDate).toLocaleDateString('es-ES', {
                  day: 'numeric',
                  month: 'long',
                  year: 'numeric'
                })}
              </div>
            </div>
            <div className="bg-blue-50 p-4 rounded">
              <div className="text-sm text-blue-600 font-medium">Inscripciones</div>
              <div className="text-lg font-semibold text-gray-800">
                {inscriptions.length} equipos
              </div>
            </div>
          </div>

          {tournament.status === 'REGISTRATION' && (
            <button className="mt-6 w-full bg-blue-900 text-white py-3 rounded-lg font-semibold hover:bg-blue-800 transition">
              Inscribirse al torneo
            </button>
          )}
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          <div className="flex border-b">
            <button
              onClick={() => setActiveTab('info')}
              className={`flex-1 px-6 py-4 font-medium transition ${
                activeTab === 'info'
                  ? 'bg-blue-900 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Información
            </button>
            <button
              onClick={() => setActiveTab('inscriptions')}
              className={`flex-1 px-6 py-4 font-medium transition ${
                activeTab === 'inscriptions'
                  ? 'bg-blue-900 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Equipos ({inscriptions.length})
            </button>
            <button
              onClick={() => setActiveTab('matches')}
              className={`flex-1 px-6 py-4 font-medium transition ${
                activeTab === 'matches'
                  ? 'bg-blue-900 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Partidos ({matches.length})
            </button>
          </div>

          <div className="p-6">
            {activeTab === 'info' && (
              <div className="space-y-4">
                <div>
                  <h3 className="font-semibold text-gray-800 mb-2">Reglas del torneo</h3>
                  <p className="text-gray-600">{tournament.rules || 'No se han especificado reglas aún.'}</p>
                </div>
                <div>
                  <h3 className="font-semibold text-gray-800 mb-2">Formato</h3>
                  <p className="text-gray-600">Información del formato del torneo.</p>
                </div>
              </div>
            )}

            {activeTab === 'inscriptions' && (
              <div>
                {inscriptions.length === 0 ? (
                  <p className="text-center text-gray-500 py-8">No hay equipos inscritos aún.</p>
                ) : (
                  <div className="grid gap-4">
                    {inscriptions.map((inscription: any, index: number) => (
                      <div key={index} className="border rounded-lg p-4 hover:shadow-md transition">
                        <h4 className="font-semibold text-gray-800">{inscription.teamName}</h4>
                        <p className="text-sm text-gray-600">{inscription.club?.name}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {activeTab === 'matches' && (
              <div>
                {matches.length === 0 ? (
                  <p className="text-center text-gray-500 py-8">No hay partidos programados aún.</p>
                ) : (
                  <div className="space-y-4">
                    {matches.map((match: any, index: number) => (
                      <div key={index} className="border rounded-lg p-4 hover:shadow-md transition">
                        <div className="flex justify-between items-center">
                          <div className="flex-1 text-right">
                            <p className="font-semibold">{match.teamA}</p>
                          </div>
                          <div className="px-6 text-center">
                            <p className="text-2xl font-bold text-gray-800">vs</p>
                          </div>
                          <div className="flex-1">
                            <p className="font-semibold">{match.teamB}</p>
                          </div>
                        </div>
                        <p className="text-center text-sm text-gray-600 mt-2">
                          {new Date(match.date).toLocaleString('es-ES')}
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </main>
  );
}