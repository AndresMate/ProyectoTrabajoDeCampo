// frontend-uptc/src/components/LiveMatchScore.tsx
'use client';

interface LiveMatchScoreProps {
  match: any;
  homeScore: number;
  awayScore: number;
  currentMinute: number;
  status: string;
}

export default function LiveMatchScore({
  match,
  homeScore,
  awayScore,
  currentMinute,
  status
}: LiveMatchScoreProps) {
  return (
    <div className="bg-gradient-to-r from-gray-800 to-gray-900 rounded-lg p-6">
      <div className="grid grid-cols-3 items-center">
        {/* Equipo Local */}
        <div className="text-center">
          <h3 className="text-xl font-bold text-white mb-2">
            {match.homeTeam.name}
          </h3>
          <p className="text-gray-400 text-sm">
            {match.homeTeam.club?.name || 'Local'}
          </p>
        </div>

        {/* Marcador */}
        <div className="text-center">
          <div className="text-5xl font-bold text-uptc-yellow">
            {homeScore} - {awayScore}
          </div>
          {status === 'IN_PROGRESS' && (
            <div className="mt-2 text-green-400 font-medium animate-pulse">
              🔴 EN VIVO - {currentMinute}'
            </div>
          )}
          {status === 'FINISHED' && (
            <div className="mt-2 text-gray-400 font-medium">
              FINALIZADO
            </div>
          )}
        </div>

        {/* Equipo Visitante */}
        <div className="text-center">
          <h3 className="text-xl font-bold text-white mb-2">
            {match.awayTeam.name}
          </h3>
          <p className="text-gray-400 text-sm">
            {match.awayTeam.club?.name || 'Visitante'}
          </p>
        </div>
      </div>
    </div>
  );
}