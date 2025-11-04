// typescript
// src/app/torneos/[matchId]/posiciones/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import standingsService from '@/services/standingsService';
import { tournamentsService } from '@/services/tournamentsService';

interface Standing {
  position: number;
  teamName: string;
  matchesPlayed: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDifference: number;
  points: number;
}

interface Category {
  id: number;
  name: string;
}

interface TournamentBrief {
  id: number;
  name: string;
  categories?: Category[];
}

function getPropNumber(obj: unknown, key: string, fallback = 0): number {
  if (typeof obj === 'object' && obj !== null) {
    const v = (obj as Record<string, unknown>)[key];
    if (typeof v === 'number') return v;
    if (typeof v === 'string') {
      const n = Number(v);
      return Number.isFinite(n) ? n : fallback;
    }
  }
  return fallback;
}

function getPropString(obj: unknown, key: string, fallback = ''): string {
  if (typeof obj === 'object' && obj !== null) {
    const v = (obj as Record<string, unknown>)[key];
    if (typeof v === 'string') return v;
    if (typeof v === 'object' && v !== null) {
      const name = (v as Record<string, unknown>)['name'];
      if (typeof name === 'string') return name;
    }
  }
  return fallback;
}

export default function StandingsPage() {
  const { id } = useParams();
  const [standings, setStandings] = useState<Standing[]>([]);
  const [tournament, setTournament] = useState<TournamentBrief | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      fetchTournament();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (selectedCategory) {
      fetchStandings();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedCategory]);

  const fetchTournament = async () => {
    try {
      const data = await tournamentsService.getById(id as string);
      const tour = data as unknown as TournamentBrief;
      setTournament(tour);

      if (Array.isArray(tour?.categories) && tour.categories.length > 0) {
        setCategories(tour.categories);
        setSelectedCategory(tour.categories[0].id);
      } else {
        // fallback si la API no devuelve categorías
        const fallback = [{ id: 1, name: 'Categoría Única' }];
        setCategories(fallback);
        setSelectedCategory(fallback[0].id);
      }
    } catch (error) {
      console.error('Error al cargar torneo:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchStandings = async () => {
    if (!selectedCategory) return;

    try {
      const raw = await standingsService.getStandings(Number(id), selectedCategory);
      const list: Standing[] = Array.isArray(raw)
        ? raw.map((s, idx) => {
            const gf = getPropNumber(s, 'goalsFor', getPropNumber(s, 'gf', 0));
            const ga = getPropNumber(s, 'goalsAgainst', getPropNumber(s, 'ga', 0));
            return {
              position: getPropNumber(s, 'position', idx + 1),
              teamName:
                getPropString(s, 'teamName') ||
                getPropString(s, 'team', '') ||
                getPropString(s, 'club', '') ||
                'Equipo',
              matchesPlayed: getPropNumber(s, 'matchesPlayed', getPropNumber(s, 'played', 0)),
              wins: getPropNumber(s, 'wins', getPropNumber(s, 'w', 0)),
              draws: getPropNumber(s, 'draws', getPropNumber(s, 'd', 0)),
              losses: getPropNumber(s, 'losses', getPropNumber(s, 'l', 0)),
              goalsFor: gf,
              goalsAgainst: ga,
              goalDifference: getPropNumber(s, 'goalDifference', gf - ga),
              points: getPropNumber(s, 'points', getPropNumber(s, 'pts', 0))
            };
          })
        : [];

      setStandings(list);
    } catch (error) {
      console.error('Error al cargar posiciones:', error);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-900"></div>
      </div>
    );
  }

  return (
    <main className="min-h-screen py-10 bg-gray-50">
      <div className="max-w-6xl mx-auto px-4">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-blue-900 mb-2">Tabla de Posiciones</h1>
          <p className="text-gray-600">{tournament?.name}</p>
        </div>

        {categories.length > 1 && (
          <div className="bg-white rounded-lg shadow p-4 mb-6">
            <label className="block text-gray-700 font-medium mb-2">Seleccionar Categoría:</label>
            <select
              value={selectedCategory ?? ''}
              onChange={(e) => setSelectedCategory(Number(e.target.value))}
              className="w-full md:w-64 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            >
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* El resto del JSX permanece igual */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead className="bg-blue-900 text-white">
                <tr>
                  <th className="px-4 py-3 text-center text-sm font-medium">POS</th>
                  <th className="px-6 py-3 text-left text-sm font-medium">EQUIPO</th>
                  <th className="px-4 py-3 text-center text-sm font-medium">PJ</th>
                  <th className="px-4 py-3 text-center text-sm font-medium">G</th>
                  <th className="px-4 py-3 text-center text-sm font-medium">E</th>
                  <th className="px-4 py-3 text-center text-sm font-medium">P</th>
                  <th className="px-4 py-3 text-center text-sm font-medium">GF</th>
                  <th className="px-4 py-3 text-center text-sm font-medium">GC</th>
                  <th className="px-4 py-3 text-center text-sm font-medium">DIF</th>
                  <th className="px-4 py-3 text-center text-sm font-medium">PTS</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {standings.map((standing, index) => (
                  <tr
                    key={index}
                    className={`hover:bg-gray-50 ${
                      index < 4 ? 'bg-green-50' : index >= standings.length - 3 ? 'bg-red-50' : ''
                    }`}
                  >
                    <td className="px-4 py-4 text-center">
                      <div
                        className={`w-8 h-8 rounded-full flex items-center justify-center mx-auto font-bold ${
                          index === 0 ? 'bg-yellow-400 text-white' : index === 1 ? 'bg-gray-400 text-white' : index === 2 ? 'bg-orange-400 text-white' : 'bg-gray-200 text-gray-700'
                        }`}
                      >
                        {standing.position}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-semibold text-gray-900">{standing.teamName}</div>
                    </td>
                    <td className="px-4 py-4 text-center text-gray-700">{standing.matchesPlayed}</td>
                    <td className="px-4 py-4 text-center text-green-600 font-semibold">{standing.wins}</td>
                    <td className="px-4 py-4 text-center text-gray-600">{standing.draws}</td>
                    <td className="px-4 py-4 text-center text-red-600 font-semibold">{standing.losses}</td>
                    <td className="px-4 py-4 text-center text-gray-700">{standing.goalsFor}</td>
                    <td className="px-4 py-4 text-center text-gray-700">{standing.goalsAgainst}</td>
                    <td className="px-4 py-4 text-center">
                      <span
                        className={`font-semibold ${
                          standing.goalDifference > 0 ? 'text-green-600' : standing.goalDifference < 0 ? 'text-red-600' : 'text-gray-600'
                        }`}
                      >
                        {standing.goalDifference > 0 ? '+' : ''}
                        {standing.goalDifference}
                      </span>
                    </td>
                    <td className="px-4 py-4 text-center">
                      <div className="bg-blue-900 text-white font-bold rounded px-3 py-1 inline-block">{standing.points}</div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {standings.length === 0 && <div className="p-8 text-center text-gray-500">No hay datos de posiciones disponibles</div>}
        </div>

        {/* Leyenda y volver */}
        <div className="mt-6 bg-white rounded-lg shadow p-4">
          <h3 className="font-semibold text-gray-800 mb-3">Leyenda:</h3>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4 text-sm">
            <div><strong>POS:</strong> Posición</div>
            <div><strong>PJ:</strong> Partidos Jugados</div>
            <div><strong>G:</strong> Ganados</div>
            <div><strong>E:</strong> Empatados</div>
            <div><strong>P:</strong> Perdidos</div>
            <div><strong>GF:</strong> Goles a Favor</div>
            <div><strong>GC:</strong> Goles en Contra</div>
            <div><strong>DIF:</strong> Diferencia de Goles</div>
            <div><strong>PTS:</strong> Puntos</div>
          </div>
          <div className="mt-4 flex gap-4 text-sm">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-green-50 border border-green-200"></div>
              <span>Clasificación</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-red-50 border border-red-200"></div>
              <span>Descenso</span>
            </div>
          </div>
        </div>

        <div className="mt-6 text-center">
          <a href={`/torneos/${id}`} className="text-blue-900 hover:underline font-medium">← Volver al torneo</a>
        </div>
      </div>
    </main>
  );
}