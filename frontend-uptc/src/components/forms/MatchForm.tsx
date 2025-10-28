// typescript
// `src/components/forms/MatchForm.tsx`
'use client';

import { useState, useEffect } from 'react';
import matchesService from '@/services/matchesService';
import { tournamentsService } from '@/services/tournamentsService';
import categoriesService from '@/services/categoriesService';
import teamsService from '@/services/teamsService';
import venuesService from '@/services/venuesService';
import api from '@/services/api';

interface MatchFormProps {
  matchId?: number;
  onSuccess: () => void;
  onCancel: () => void;
}

interface TournamentBrief {
  id: number;
  name: string;
  sport?: { id: number; name?: string };
}

interface Category {
  id: number;
  name: string;
}

interface Team {
  id: number;
  name: string;
  clubName?: string;
}

interface Venue {
  id: number;
  name: string;
}

interface Scenario {
  id: number;
  name: string;
}

interface FormData {
  tournamentId: number;
  categoryId: number;
  homeTeamId: number;
  awayTeamId: number;
  matchDate: string;
  venueId: number;
  scenarioId: number;
}

interface ApiMatch {
  tournament?: TournamentBrief;
  category?: Category;
  homeTeam?: Team;
  awayTeam?: Team;
  startsAt?: string | null;
  venue?: Venue;
  scenario?: Scenario;
}

const getErrorMessage = (err: unknown) => {
  if (err instanceof Error) return err.message;
  if (typeof err === 'object' && err !== null && 'response' in err) {
    try {
      const resp = (err as Record<string, unknown>)['response'] as Record<string, unknown> | undefined;
      const data = resp?.['data'] as Record<string, unknown> | undefined;
      return (data?.['message'] as string) ?? String(err);
    } catch {
      return String(err);
    }
  }
  return String(err);
};

export default function MatchForm({ matchId, onSuccess, onCancel }: MatchFormProps) {
  const [loading, setLoading] = useState(false);
  const [tournaments, setTournaments] = useState<TournamentBrief[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [venues, setVenues] = useState<Venue[]>([]);
  const [scenarios, setScenarios] = useState<Scenario[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const [formData, setFormData] = useState<FormData>({
    tournamentId: 0,
    categoryId: 0,
    homeTeamId: 0,
    awayTeamId: 0,
    matchDate: '',
    venueId: 0,
    scenarioId: 0,
  });

  useEffect(() => {
    fetchTournaments();
    fetchVenues();

    if (matchId) {
      fetchMatch(matchId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [matchId]);

  const fetchMatch = async (id: number) => {
    try {
      const data = (await matchesService.getById(id)) as unknown as ApiMatch;
      setFormData({
        tournamentId: data.tournament?.id ?? 0,
        categoryId: data.category?.id ?? 0,
        homeTeamId: data.homeTeam?.id ?? 0,
        awayTeamId: data.awayTeam?.id ?? 0,
        matchDate: data.startsAt ? data.startsAt.slice(0, 16) : '',
        venueId: data.venue?.id ?? 0,
        scenarioId: data.scenario?.id ?? 0,
      });

      if (data.tournament?.id) await fetchCategories(data.tournament.id);
      if (data.tournament?.id && data.category?.id) await fetchTeams(data.tournament.id, data.category.id);
      if (data.venue?.id) await fetchScenarios(data.venue.id);
    } catch (error: unknown) {
      console.error('Error al cargar partido:', getErrorMessage(error));
    }
  };

  const fetchTournaments = async () => {
    try {
      const data = (await tournamentsService.getAll()) as TournamentBrief[];
      setTournaments(Array.isArray(data) ? data : []);
    } catch (error: unknown) {
      console.error('Error al cargar torneos:', getErrorMessage(error));
    }
  };

  const fetchVenues = async () => {
    try {
      const data = await venuesService.getAll();
      // venuesService puede devolver { content: [...] } o directamente array
      const list = Array.isArray(data) ? data : (data as any)?.content ?? [];
      setVenues(list as Venue[]);
    } catch (error: unknown) {
      console.error('Error al cargar sedes:', getErrorMessage(error));
    }
  };

  const fetchCategories = async (tournamentId: number) => {
    try {
      const tournament = await tournamentsService.getById(String(tournamentId));
      if (tournament?.sport?.id) {
        const data = await categoriesService.getActiveBySport(tournament.sport.id);
        setCategories(Array.isArray(data) ? data : []);
      }
    } catch (error: unknown) {
      console.error('Error al cargar categorías:', getErrorMessage(error));
    }
  };

  const fetchTeams = async (tournamentId: number, categoryId: number) => {
    try {
      const data = await teamsService.getByTournamentAndCategory(tournamentId, categoryId);
      setTeams(Array.isArray(data) ? data : []);
    } catch (error: unknown) {
      console.error('Error al cargar equipos:', getErrorMessage(error));
    }
  };

  const fetchScenarios = async (venueId: number) => {
    try {
      const data = await venuesService.getScenariosByVenue(venueId);
      setScenarios(Array.isArray(data) ? data : []);
    } catch (error: unknown) {
      console.error('Error al cargar escenarios:', getErrorMessage(error));
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.tournamentId) newErrors.tournamentId = 'Selecciona un torneo';
    if (!formData.categoryId) newErrors.categoryId = 'Selecciona una categoría';
    if (!formData.homeTeamId) newErrors.homeTeamId = 'Selecciona el equipo local';
    if (!formData.awayTeamId) newErrors.awayTeamId = 'Selecciona el equipo visitante';
    if (!formData.matchDate) newErrors.matchDate = 'Selecciona fecha y hora';
    if (formData.homeTeamId && formData.homeTeamId === formData.awayTeamId) {
      newErrors.awayTeamId = 'No puede ser el mismo equipo';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    const numericFields = ['tournamentId', 'categoryId', 'homeTeamId', 'awayTeamId', 'venueId', 'scenarioId'];

    setFormData(prev => ({
      ...prev,
      [name]: numericFields.includes(name) ? Number(value) : value,
    }));

    if (name === 'tournamentId' && value) {
      const tId = Number(value);
      fetchCategories(tId);
      setFormData(prev => ({ ...prev, categoryId: 0, homeTeamId: 0, awayTeamId: 0 }));
      setTeams([]);
    }

    if (name === 'categoryId' && value && formData.tournamentId) {
      const cId = Number(value);
      fetchTeams(formData.tournamentId, cId);
      setFormData(prev => ({ ...prev, homeTeamId: 0, awayTeamId: 0 }));
    }

    if (name === 'venueId' && value) {
      fetchScenarios(Number(value));
      setFormData(prev => ({ ...prev, scenarioId: 0 }));
    }

    if (errors[name]) {
      setErrors(prev => {
        const copy = { ...prev };
        delete copy[name];
        return copy;
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) {
      alert('⚠️ Por favor completa todos los campos requeridos.');
      return;
    }

    setLoading(true);

    // Construir payload según MatchCreateDTO
    const payload = {
      tournamentId: formData.tournamentId,
      categoryId: formData.categoryId,
      teamAId: formData.homeTeamId,
      teamBId: formData.awayTeamId,
      matchDate: formData.matchDate,
      venueId: formData.venueId || undefined,
      scenarioId: formData.scenarioId || undefined,
    };

    try {
      if (matchId) {
        // si matchesService tiene update usarlo, si no usar api.put directamente
        const svcAsAny = matchesService as unknown as Record<string, unknown>;
        if (typeof svcAsAny['update'] === 'function') {
          await (svcAsAny['update'] as (...args: unknown[]) => Promise<unknown>)(matchId, payload);
        } else {
          await api.put(`/matches/${matchId}`, payload);
        }
        alert('✅ Partido actualizado correctamente');
      } else {
        await matchesService.create(payload as any);
        alert('✅ Partido creado correctamente');
      }
      onSuccess();
    } catch (error: unknown) {
      console.error('Error al guardar partido:', getErrorMessage(error));
      alert(getErrorMessage(error) || 'Error al guardar el partido');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Torneo */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">Torneo *</label>
          <select
            name="tournamentId"
            value={formData.tournamentId}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
              errors.tournamentId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          >
            <option value={0}>Selecciona un torneo</option>
            {tournaments.map(t => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
          {errors.tournamentId && <p className="text-red-500 text-sm mt-1">{errors.tournamentId}</p>}
        </div>

        {/* Categoría */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">Categoría *</label>
          <select
            name="categoryId"
            value={formData.categoryId}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
              errors.categoryId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
            disabled={!formData.tournamentId}
          >
            <option value={0}>
              {formData.tournamentId ? 'Selecciona una categoría' : 'Primero selecciona un torneo'}
            </option>
            {categories.map(c => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          {errors.categoryId && <p className="text-red-500 text-sm mt-1">{errors.categoryId}</p>}
        </div>

        {/* Equipo Local */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">Equipo Local *</label>
          <select
            name="homeTeamId"
            value={formData.homeTeamId}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
              errors.homeTeamId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
            disabled={teams.length === 0}
          >
            <option value={0}>Selecciona equipo local</option>
            {teams.map(t => (
              <option key={t.id} value={t.id}>
                {t.name} {t.clubName ? `(${t.clubName})` : ''}
              </option>
            ))}
          </select>
          {errors.homeTeamId && <p className="text-red-500 text-sm mt-1">{errors.homeTeamId}</p>}
        </div>

        {/* Equipo Visitante */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">Equipo Visitante *</label>
          <select
            name="awayTeamId"
            value={formData.awayTeamId}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
              errors.awayTeamId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
            disabled={teams.length === 0}
          >
            <option value={0}>Selecciona equipo visitante</option>
            {teams
              .filter(t => t.id !== formData.homeTeamId)
              .map(t => (
                <option key={t.id} value={t.id}>
                  {t.name} {t.clubName ? `(${t.clubName})` : ''}
                </option>
              ))}
          </select>
          {errors.awayTeamId && <p className="text-red-500 text-sm mt-1">{errors.awayTeamId}</p>}
        </div>

        {/* Fecha */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">Fecha y Hora *</label>
          <input
            type="datetime-local"
            name="matchDate"
            value={formData.matchDate}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
              errors.matchDate ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          />
          {errors.matchDate && <p className="text-red-500 text-sm mt-1">{errors.matchDate}</p>}
        </div>
      </div>

      {/* Botones */}
      <div className="flex justify-end gap-3 pt-4 border-t">
        <button
          type="button"
          onClick={onCancel}
          className="px-6 py-2 bg-gray-300 rounded-lg hover:bg-gray-400 transition"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={loading}
          className="px-6 py-2 bg-uptc-black text-white rounded-lg hover:bg-gray-800 transition disabled:opacity-50"
        >
          {loading ? 'Guardando...' : matchId ? 'Actualizar Partido' : 'Crear Partido'}
        </button>
      </div>
    </form>
  );
}
