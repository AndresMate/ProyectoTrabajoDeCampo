// frontend-uptc/src/app/torneos/[id]/page.tsx - ✅ VERSIÓN CORREGIDA
'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';
import inscriptionsService from '@/services/inscriptionsService';
import matchesService from '@/services/matchesService';
import Link from 'next/link';

export default function TournamentDetailPage() {
  const { id } = useParams();
  const router = useRouter();
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
      // Cargar datos del torneo
      const tournamentData = await tournamentsService.getById(id as string);
      setTournament(tournamentData);

      // ✅ Cargar SOLO inscripciones aprobadas de ESTE torneo
      try {
        const inscriptionsData = await tournamentsService.getInscriptions(Number(id));
        console.log('✅ Inscripciones cargadas:', inscriptionsData);
        setInscriptions(inscriptionsData);
      } catch (error) {
        console.error('❌ Error al cargar inscripciones:', error);
        setInscriptions([]);
      }

      // ✅ Cargar SOLO partidos de ESTE torneo
      try {
        const matchesData = await matchesService.getByTournament(Number(id));
        console.log('✅ Partidos cargados del torneo:', matchesData);
        setMatches(matchesData);
      } catch (error) {
        console.error('❌ Error al cargar partidos:', error);
        setMatches([]);
      }
    } catch (error) {
      console.error('❌ Error al obtener datos del torneo:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleInscription = () => {
    router.push(`/torneos/${id}/inscribirse`);
  };

  const getStatusColor = (status: string) => {
    const colors: any = {
      PLANNING: 'bg-gray-200 text-gray-700',
      OPEN_FOR_INSCRIPTION: 'bg-blue-200 text-blue-700',
      IN_PROGRESS: 'bg-green-200 text-green-700',
      FINISHED: 'bg-purple-200 text-purple-700',
      CANCELLED: 'bg-red-200 text-red-700'
    };
    return colors[status] || 'bg-gray-200 text-gray-700';
  };

  const getStatusText = (status: string) => {
    const texts: any = {
      PLANNING: 'Planificación',
      OPEN_FOR_INSCRIPTION: 'Inscripciones Abiertas',
      IN_PROGRESS: 'En Curso',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado'
    };
    return texts[status] || status;
  };

  const getMatchStatusBadge = (status: string) => {
    const colors: any = {
      SCHEDULED: 'bg-blue-100 text-blue-700',
      IN_PROGRESS: 'bg-green-100 text-green-700',
      FINISHED: 'bg-gray-100 text-gray-700',
      CANCELLED: 'bg-red-100 text-red-700'
    };
    return colors[status] || 'bg-gray-100 text-gray-700';
  };

  const getMatchStatusText = (status: string) => {
    const texts: any = {
      SCHEDULED: 'Programado',
      IN_PROGRESS: 'En Curso',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado'
    };
    return texts[status] || status;
  };

  // ✅ Cálculo de estadísticas del torneo
  const tournamentStats = {
    totalTeams: inscriptions.length,
    totalMatches: matches.length,
    finishedMatches: matches.filter((m: any) => m.status === 'FINISHED').length,
    scheduledMatches: matches.filter((m: any) => m.status === 'SCHEDULED').length,
    inProgressMatches: matches.filter((m: any) => m.status === 'IN_PROGRESS').length,
    totalPlayers: inscriptions.reduce((total: number, insc: any) => total + (insc.playerCount || 0), 0),
    completionPercentage: matches.length > 0
      ? Math.round((matches.filter((m: any) => m.status === 'FINISHED').length / matches.length) * 100)
      : 0
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
          <Link href="/torneos" className="mt-4 inline-block bg-blue-900 text-white px-6 py-2 rounded hover:bg-blue-800">
            Volver a torneos
          </Link>
        </div>
      </main>
    );
  }

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
              <p className="text-sm text-gray-500 mt-1">
                Modalidad: {tournament.modality === 'DIURNO' ? 'Diurna' : 'Nocturna'}
              </p>
            </div>
            <span className={`px-4 py-2 rounded-full text-sm font-semibold ${getStatusColor(tournament.status)}`}>
              {getStatusText(tournament.status)}
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mt-6">
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
              <div className="text-sm text-blue-600 font-medium">Equipos Inscritos</div>
              <div className="text-lg font-semibold text-gray-800">
                {tournamentStats.totalTeams} / {tournament.maxTeams || '∞'}
              </div>
            </div>
            <div className="bg-blue-50 p-4 rounded">
              <div className="text-sm text-blue-600 font-medium">Partidos</div>
              <div className="text-lg font-semibold text-gray-800">
                {tournamentStats.totalMatches} programados
              </div>
            </div>
          </div>

          {/* Botones de acción */}
          <div className="mt-6 flex gap-3">
            {/* Botón de inscripción */}
            {tournament.status === 'OPEN_FOR_INSCRIPTION' && (
              <button
                onClick={handleInscription}
                className="flex-1 bg-blue-900 text-white py-3 rounded-lg font-semibold hover:bg-blue-800 transition"
              >
                📝 Inscribirse al torneo
              </button>
            )}

            {/* Botón de tabla de posiciones */}
            {(tournament.status === 'IN_PROGRESS' || tournament.status === 'FINISHED') && (
              <Link
                href={`/torneos/${id}/posiciones`}
                className="flex-1 bg-green-600 text-white py-3 rounded-lg font-semibold hover:bg-green-700 transition text-center"
              >
                🏆 Ver Tabla de Posiciones
              </Link>
            )}

            {/* Botón volver */}
            <Link
              href="/torneos"
              className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition"
            >
              ← Volver
            </Link>
          </div>
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
              ℹ️ Información
            </button>
            <button
              onClick={() => setActiveTab('inscriptions')}
              className={`flex-1 px-6 py-4 font-medium transition ${
                activeTab === 'inscriptions'
                  ? 'bg-blue-900 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              👥 Equipos ({tournamentStats.totalTeams})
            </button>
            <button
              onClick={() => setActiveTab('matches')}
              className={`flex-1 px-6 py-4 font-medium transition ${
                activeTab === 'matches'
                  ? 'bg-blue-900 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              ⚽ Partidos ({tournamentStats.totalMatches})
            </button>
          </div>

          <div className="p-6">
            {/* Tab: Información */}
            {activeTab === 'info' && (
              <div className="space-y-6">
                <div>
                  <h3 className="font-semibold text-gray-800 mb-2 flex items-center gap-2">
                    📋 Información General
                  </h3>
                  <div className="bg-gray-50 p-4 rounded-lg space-y-2 text-sm">
                    <p><strong>Deporte:</strong> {tournament.sport?.name}</p>
                    <p><strong>Categoría:</strong> {tournament.category?.name}</p>
                    <p><strong>Máximo de equipos:</strong> {tournament.maxTeams || 'Sin límite'}</p>
                    <p><strong>Modalidad:</strong> {tournament.modality === 'DIURNO' ? 'Diurna' : 'Nocturna'}</p>
                    <p><strong>Creado por:</strong> {tournament.createdBy?.fullName || 'N/A'}</p>
                  </div>
                </div>

                {/* ✅ ESTADÍSTICAS MEJORADAS DEL TORNEO */}
                <div>
                  <h3 className="font-semibold text-gray-800 mb-3 flex items-center gap-2">
                    📊 Estadísticas del Torneo
                  </h3>

                  {/* Estadísticas principales */}
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                    <div className="bg-blue-50 p-4 rounded-lg text-center border-l-4 border-blue-500">
                      <div className="text-3xl font-bold text-blue-900">{tournamentStats.totalTeams}</div>
                      <div className="text-xs text-gray-600 font-medium mt-1">Equipos Inscritos</div>
                    </div>
                    <div className="bg-green-50 p-4 rounded-lg text-center border-l-4 border-green-500">
                      <div className="text-3xl font-bold text-green-900">{tournamentStats.totalMatches}</div>
                      <div className="text-xs text-gray-600 font-medium mt-1">Total Partidos</div>
                    </div>
                    <div className="bg-purple-50 p-4 rounded-lg text-center border-l-4 border-purple-500">
                      <div className="text-3xl font-bold text-purple-900">{tournamentStats.finishedMatches}</div>
                      <div className="text-xs text-gray-600 font-medium mt-1">Finalizados</div>
                    </div>
                    <div className="bg-orange-50 p-4 rounded-lg text-center border-l-4 border-orange-500">
                      <div className="text-3xl font-bold text-orange-900">{tournamentStats.scheduledMatches}</div>
                      <div className="text-xs text-gray-600 font-medium mt-1">Por Jugar</div>
                    </div>
                  </div>

                  {/* Estadísticas secundarias */}
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                    <div className="bg-gray-50 p-4 rounded-lg text-center">
                      <div className="text-2xl font-bold text-gray-900">{tournamentStats.inProgressMatches}</div>
                      <div className="text-xs text-gray-600 font-medium mt-1">En Curso</div>
                    </div>
                    <div className="bg-gray-50 p-4 rounded-lg text-center">
                      <div className="text-2xl font-bold text-gray-900">{tournamentStats.totalPlayers}</div>
                      <div className="text-xs text-gray-600 font-medium mt-1">Total Jugadores</div>
                    </div>
                    <div className="bg-gray-50 p-4 rounded-lg text-center">
                      <div className="text-2xl font-bold text-gray-900">{tournamentStats.completionPercentage}%</div>
                      <div className="text-xs text-gray-600 font-medium mt-1">Completado</div>
                    </div>
                  </div>

                  {/* Barra de progreso */}
                  {tournamentStats.totalMatches > 0 && (
                    <div className="mt-4">
                      <div className="flex justify-between text-sm mb-2">
                        <span className="font-medium text-gray-700">Progreso del Torneo</span>
                        <span className="font-semibold text-blue-900">{tournamentStats.completionPercentage}%</span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-3">
                        <div
                          className="bg-blue-600 h-3 rounded-full transition-all duration-500"
                          style={{ width: `${tournamentStats.completionPercentage}%` }}
                        ></div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Tab: Equipos */}
            {activeTab === 'inscriptions' && (
              <div>
                {inscriptions.length === 0 ? (
                  <div className="text-center py-12">
                    <p className="text-gray-500 text-lg mb-2">No hay equipos inscritos aún</p>
                    {tournament.status === 'OPEN_FOR_INSCRIPTION' && (
                      <button
                        onClick={handleInscription}
                        className="mt-4 bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800"
                      >
                        Ser el primero en inscribirse
                      </button>
                    )}
                  </div>
                ) : (
                  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {inscriptions.map((inscription: any) => (
                      <div key={inscription.id} className="border rounded-lg p-4 hover:shadow-md transition bg-gray-50">
                        <div className="flex items-start justify-between mb-2">
                          <h4 className="font-semibold text-gray-800 text-lg">{inscription.teamName}</h4>
                          <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded">
                            Aprobado
                          </span>
                        </div>
                        {inscription.club?.name && (
                          <p className="text-sm text-gray-600 mb-2">
                            🏛️ {inscription.club.name}
                          </p>
                        )}
                        <p className="text-xs text-gray-500">
                          👤 {inscription.delegate?.fullName || 'Delegado N/A'}
                        </p>
                        {inscription.playerCount > 0 && (
                          <p className="text-xs text-blue-600 mt-2">
                            👥 {inscription.playerCount} jugadores
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Tab: Partidos - ✅ AHORA MUESTRA SOLO LOS DEL TORNEO */}
            {activeTab === 'matches' && (
              <div>
                {matches.length === 0 ? (
                  <div className="text-center py-12">
                    <div className="text-gray-400 mb-4">
                      <svg className="mx-auto h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <p className="text-gray-500 text-lg font-semibold">No hay partidos programados aún</p>
                    <p className="text-gray-400 text-sm mt-2">
                      Los partidos se programarán cuando el torneo esté en curso
                    </p>
                  </div>
                ) : (
                  <>
                    <div className="mb-4 flex items-center justify-between">
                      <p className="text-sm text-gray-600">
                        Mostrando <span className="font-semibold text-gray-900">{matches.length}</span> partidos de este torneo
                      </p>
                      <div className="flex gap-2">
                        <span className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded">
                          {tournamentStats.scheduledMatches} Programados
                        </span>
                        <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded">
                          {tournamentStats.inProgressMatches} En Curso
                        </span>
                        <span className="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded">
                          {tournamentStats.finishedMatches} Finalizados
                        </span>
                      </div>
                    </div>

                    <div className="space-y-4">
                      {matches.map((match: any) => (
                        <div key={match.id} className="border rounded-lg p-4 hover:shadow-md transition bg-white">
                          <div className="flex items-center justify-between mb-3">
                            <span className={`text-xs px-3 py-1 rounded-full font-semibold ${getMatchStatusBadge(match.status)}`}>
                              {getMatchStatusText(match.status)}
                            </span>
                            <span className="text-sm text-gray-600 font-medium">
                              {match.matchDate || match.startsAt
                                ? new Date(match.matchDate || match.startsAt).toLocaleDateString('es-ES', {
                                    day: '2-digit',
                                    month: 'short',
                                    hour: '2-digit',
                                    minute: '2-digit'
                                  })
                                : 'Fecha por definir'}
                            </span>
                          </div>

                          <div className="flex items-center justify-between">
                            <div className="flex-1 text-right pr-4">
                              <p className="font-semibold text-gray-900 text-lg">
                                {match.homeTeam?.name || match.teamA?.name || 'Equipo Local'}
                              </p>
                            </div>
                            <div className="px-4 text-center">
                              <p className="text-2xl font-bold text-gray-800">VS</p>
                            </div>
                            <div className="flex-1 text-left pl-4">
                              <p className="font-semibold text-gray-900 text-lg">
                                {match.awayTeam?.name || match.teamB?.name || 'Equipo Visitante'}
                              </p>
                            </div>
                          </div>

                          {(match.venue || match.scenario) && (
                            <p className="text-center text-xs text-gray-500 mt-3">
                              📍 {match.venue?.name || 'Sede por confirmar'}
                              {match.scenario && ` - ${match.scenario.name}`}
                            </p>
                          )}

                          {/* Información adicional si el partido está en curso o finalizado */}
                          {match.status === 'FINISHED' && match.result && (
                            <div className="mt-3 pt-3 border-t text-center">
                              <span className="text-sm font-semibold text-gray-700">
                                Resultado: {match.result.homeScore} - {match.result.awayScore}
                              </span>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </main>
  );
}