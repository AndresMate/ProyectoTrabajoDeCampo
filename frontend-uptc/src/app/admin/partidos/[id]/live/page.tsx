'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import matchesService, { Match, MatchResult } from '@/services/matchesService';
import matchEventsService, { MatchEvent, MatchEventCreateDTO } from '@/services/matchEventsService';
import teamsService from '@/services/teamsService';
import LiveMatchControl from '@/components/LiveMatchControl';
import MatchEventsTimeline from '@/components/MatchEventsTimeline';
import LiveMatchScore from '@/components/LiveMatchScore';

export default function LiveMatchPage() {
  const params = useParams();
  const router = useRouter();
  const matchId = Number(params.id);

  const [match, setMatch] = useState<Match | null>(null);
  const [events, setEvents] = useState<MatchEvent[]>([]);
  const [homeRoster, setHomeRoster] = useState<any[]>([]);
  const [awayRoster, setAwayRoster] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentMinute, setCurrentMinute] = useState(0);
  const [matchStartTime, setMatchStartTime] = useState<Date | null>(null);
  const [liveScore, setLiveScore] = useState({ home: 0, away: 0 });

  // 🔹 Cargar datos solo al montar o tras un cambio relevante
  useEffect(() => {
    if (matchId) loadMatchData();
  }, [matchId]);

  // 🔹 Actualizar el minuto actual según el tiempo real del partido
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

  // 🔹 Cargar partido, eventos y equipos
  const loadMatchData = async () => {
    try {
      setLoading(true);
      const matchData = await matchesService.getById(matchId);
      setMatch(matchData);

      // ⏱️ Usa la hora real de inicio para el reloj
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
      alert('❌ Error al cargar datos del partido');
    } finally {
      setLoading(false);
    }
  };

  // 🔹 Calcular marcador actual
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

  // 🔹 Iniciar partido
  const handleStartMatch = async () => {
    if (!confirm('¿Iniciar el partido?')) return;
    try {
      await matchesService.startMatch(matchId);
      await loadMatchData();
      alert('✅ Partido iniciado');
    } catch (error) {
      console.error('Error al iniciar partido:', error);
      alert('❌ Error al iniciar partido');
    }
  };

  // 🔹 Finalizar partido y registrar resultado
  const handleFinishMatch = async () => {
    if (!confirm('¿Finalizar el partido y registrar el resultado?')) return;
    try {
      await matchesService.finishMatch(matchId);

      const result: MatchResult = {
        matchId: matchId,
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

      await matchesService.registerResult(result);
      alert('✅ Partido finalizado y resultado registrado');
      router.push('/admin/partidos');
    } catch (error) {
      console.error('Error al finalizar partido:', error);
      alert('❌ Error al finalizar partido');
    }
  };

  // 🔹 Agregar evento sin recargar todo
  const handleAddEvent = async (event: MatchEvent) => {
    if (!event.team?.id || !event.player?.id || !event.type) {
      alert('⚠️ Debes seleccionar un jugador, equipo y tipo de evento');
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

      const newEvent = await matchEventsService.create(payload);
      setEvents(prev => [...prev, newEvent]);
      calculateScore([...events, newEvent], match!);
      alert('✅ Evento registrado');
    } catch (error: any) {
      console.error('Error al agregar evento:', error);
      alert(error?.response?.data?.message || '❌ Error al registrar evento');
    }
  };

  // 🔹 Eliminar evento sin recargar todo
  const handleDeleteEvent = async (eventId: number) => {
    if (!confirm('¿Eliminar este evento?')) return;
    try {
      await matchEventsService.delete(eventId);
      const updated = events.filter(e => e.id !== eventId);
      setEvents(updated);
      calculateScore(updated, match!);
      alert('✅ Evento eliminado');
    } catch (error) {
      console.error('Error al eliminar evento:', error);
      alert('❌ Error al eliminar evento');
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
          <button
            onClick={() => router.push('/admin/partidos')}
            className="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg transition"
          >
            ← Volver
          </button>
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