// src/app/admin/torneos/[tournamentId]/partidos/[matchId]/live/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import matchesService, { Match, MatchResult } from '@/services/matchesService';
import matchEventsService, { MatchEvent, MatchEventCreateDTO } from '@/services/matchEventsService';
import teamsService from '@/services/teamsService';
import LiveMatchControl from '@/components/LiveMatchControl';
import MatchEventsTimeline from '@/components/MatchEventsTimeline';
import LiveMatchScore from '@/components/LiveMatchScore';
import { toastWarning, toastPromise } from '@/utils/toast';

export default function LiveMatchPage() {
  const params = useParams();
  const router = useRouter();
  const tournamentId = Number(params.tournamentId);
  const matchId = Number(params.matchId);

  const [match, setMatch] = useState<Match | null>(null);
  const [events, setEvents] = useState<MatchEvent[]>([]);
  const [homeRoster, setHomeRoster] = useState<any[]>([]);
  const [awayRoster, setAwayRoster] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentMinute, setCurrentMinute] = useState(0);
  const [matchStartTime, setMatchStartTime] = useState<Date | null>(null);
  const [liveScore, setLiveScore] = useState({ home: 0, away: 0 });

  useEffect(() => {
    if (matchId) loadMatchData();
  }, [matchId]);

  useEffect(() => {
    if (match?.status === 'IN_PROGRESS' && matchStartTime) {
      const interval = setInterval(() => {
        const now = new Date();
        const elapsed = Math.floor((now.getTime() - matchStartTime.getTime()) / 60000);
        setCurrentMinute(Math.min(elapsed, 90));
      }, 1000);
      return () => clearInterval(interval);
    }
  }, [match?.status, matchStartTime]);

  const loadMatchData = async () => {
    try {
      setLoading(true);
      const matchData = await matchesService.getById(matchId);
      setMatch(matchData);

      if (matchData.status === 'IN_PROGRESS' && matchData.startsAt) {
        setMatchStartTime(new Date(matchData.startsAt));
      }

      const eventsData = await matchEventsService.getByMatch(matchId);
      setEvents(eventsData);
      calculateScore(eventsData, matchData);

      if (matchData.homeTeam?.id && matchData.awayTeam?.id) {
        const [homeData, awayData] = await Promise.all([
          teamsService.getRoster(matchData.homeTeam.id),
          teamsService.getRoster(matchData.awayTeam.id)
        ]);
        setHomeRoster(homeData.roster || []);
        setAwayRoster(awayData.roster || []);
      }
    } catch (error) {
      console.error('Error al cargar datos:', error);
      // El error ya se muestra en el interceptor de axios
    } finally {
      setLoading(false);
    }
  };

  const calculateScore = (events: MatchEvent[], matchData: Match) => {
    const validGoals = ['GOAL', 'PENALTY', 'OWN_GOAL'];
    const filtered = events.filter(e => validGoals.includes(e.type));
    const homeId = matchData.homeTeam?.id;
    const awayId = matchData.awayTeam?.id;

    const homeGoals = filtered.filter(e => (e.type === 'GOAL' || e.type === 'PENALTY') && e.team?.id === homeId).length;
    const awayGoals = filtered.filter(e => (e.type === 'GOAL' || e.type === 'PENALTY') && e.team?.id === awayId).length;
    const homeOwnGoals = filtered.filter(e => e.type === 'OWN_GOAL' && e.team?.id === homeId).length;
    const awayOwnGoals = filtered.filter(e => e.type === 'OWN_GOAL' && e.team?.id === awayId).length;

    setLiveScore({
      home: homeGoals + awayOwnGoals,
      away: awayGoals + homeOwnGoals
    });
  };

  const handleStartMatch = async () => {
    try {
      await toastPromise(
        matchesService.startMatch(matchId),
        {
          loading: 'Iniciando partido...',
          success: '✅ Partido iniciado',
          error: '❌ Error al iniciar partido'
        }
      );
      await loadMatchData();
    } catch (error) {
      console.error('Error al iniciar partido:', error);
      // El error ya se muestra en el toastPromise
    }
  };

  const handleFinishMatch = async () => {
  try {
    const result: MatchResult = {
      matchId,
      homeScore: liveScore.home,
      awayScore: liveScore.away,
      winnerId:
        liveScore.home > liveScore.away
          ? match!.homeTeam.id
          : liveScore.away > liveScore.home
            ? match!.awayTeam.id
            : undefined,
      notes: `Partido finalizado. Eventos registrados: ${events.length}`
    };

    // ✅ 1. Registrar el resultado primero
    await toastPromise(
      matchesService.registerResult(result),
      {
        loading: 'Registrando resultado...',
        success: '✅ Resultado registrado correctamente',
        error: '❌ Error al registrar resultado'
      }
    );

    // ✅ 2. Luego finalizar el partido
    await toastPromise(
      matchesService.finishMatch(matchId),
      {
        loading: 'Finalizando partido...',
        success: '🏁 Partido finalizado correctamente',
        error: '❌ Error al finalizar partido'
      }
    );

    router.push(`/admin/torneos/${tournamentId}/partidos`);
  } catch (error) {
    console.error('Error al finalizar partido:', error);
    toast.error('❌ Ocurrió un error al finalizar el partido');
  }
};

  const handleAddEvent = async (event: MatchEvent) => {
    if (!event.team?.id || !event.player?.id || !event.type) {
      toastWarning('Debes seleccionar un jugador, equipo y tipo de evento');
      return;
    }

    try {
      const payload: MatchEventCreateDTO = {
        matchId: matchId,
        teamId: event.team.id,
        playerId: event.player.id,
        type: event.type,
        minute: event.minute ?? currentMinute,
        description: event.description || '',
      };

      const newEvent = await toastPromise(
        matchEventsService.create(payload),
        {
          loading: 'Registrando evento...',
          success: '✅ Evento registrado',
          error: (error: any) => error?.response?.data?.message || '❌ Error al registrar evento'
        }
      );
      setEvents(prev => [...prev, newEvent]);
      calculateScore([...events, newEvent], match!);
    } catch (error: any) {
      console.error('Error al agregar evento:', error);
      // El error ya se muestra en el toastPromise
    }
  };

  const handleDeleteEvent = async (eventId: number) => {
    try {
      await toastPromise(
        matchEventsService.delete(eventId),
        {
          loading: 'Eliminando evento...',
          success: '✅ Evento eliminado',
          error: '❌ Error al eliminar evento'
        }
      );
      const updated = events.filter(e => e.id !== eventId);
      setEvents(updated);
      calculateScore(updated, match!);
    } catch (error) {
      console.error('Error al eliminar evento:', error);
      // El error ya se muestra en el toastPromise
    }
  };

  if (loading || !match) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Breadcrumb */}
      <div className="mb-4">
        <button
          onClick={() => router.push(`/admin/torneos/${tournamentId}/partidos`)}
          className="text-uptc-yellow hover:text-yellow-600 font-semibold inline-flex items-center gap-2"
        >
          ← Volver a partidos
        </button>
      </div>

      {/* Encabezado del partido */}
      <div className="bg-uptc-black text-white rounded-lg shadow-xl p-6 mb-6">
        <div className="flex justify-between items-center mb-4">
          <div>
            <h1 className="text-2xl font-bold text-uptc-yellow">
              Gestión en Vivo del Partido
            </h1>
            <p className="text-gray-300">
              {match.tournament.name} - {match.category.name}
            </p>
          </div>
        </div>

        {/* Marcador */}
        <LiveMatchScore
          match={match}
          homeScore={liveScore.home}
          awayScore={liveScore.away}
          currentMinute={currentMinute}
          status={match.status}
        />

        {/* Controles */}
        <div className="flex gap-4 mt-6">
          {match.status === 'SCHEDULED' && (
            <button
              onClick={handleStartMatch}
              className="bg-green-600 hover:bg-green-700 px-6 py-3 rounded-lg font-bold transition"
            >
              ▶️ INICIAR PARTIDO
            </button>
          )}

          {match.status === 'IN_PROGRESS' && (
            <>
              <button
                onClick={handleFinishMatch}
                className="bg-red-600 hover:bg-red-700 px-6 py-3 rounded-lg font-bold transition"
              >
                🏁 FINALIZAR PARTIDO
              </button>
              <div className="bg-yellow-500 text-black px-4 py-3 rounded-lg font-bold">
                ⏱️ Minuto: {currentMinute}'
              </div>
            </>
          )}

          {match.status === 'FINISHED' && (
            <div className="bg-gray-600 px-6 py-3 rounded-lg">
              ✅ Partido Finalizado
            </div>
          )}
        </div>
      </div>

      {/* Panel principal */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          {match.status === 'IN_PROGRESS' && (
            <LiveMatchControl
              match={match}
              homeRoster={homeRoster}
              awayRoster={awayRoster}
              currentMinute={currentMinute}
              onAddEvent={handleAddEvent}
            />
          )}

          <MatchEventsTimeline
            events={events}
            match={match}
            onDeleteEvent={handleDeleteEvent}
          />
        </div>
      </div>
    </div>
  );
}