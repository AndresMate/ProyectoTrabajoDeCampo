'use client';

import { useEffect, useState } from 'react';
import matchesService, { MatchCreateDTO } from '@/services/matchesService';
import { tournamentsService } from '@/services/tournamentsService';
import categoriesService from '@/services/categoriesService';
import teamsService from '@/services/teamsService';
import venuesService from '@/services/venuesService';
import usersService from '@/services/usersService';

interface MatchFormProps {
  matchId?: number;
  onSuccess: () => void;
  onCancel: () => void;
}

interface Tournament {
  id: number;
  name: string;
}

interface Category {
  id: number;
  name: string;
}

interface Team {
  id: number;
  name: string;
}

interface Scenario {
  id: number;
  name: string;
}

interface Referee {
  id: number;
  fullName: string;
}

export default function MatchForm({ matchId, onSuccess, onCancel }: MatchFormProps) {
  const [formData, setFormData] = useState<MatchCreateDTO>({
    tournamentId: 0,
    categoryId: 0,
    scenarioId: null,
    startsAt: '',
    homeTeamId: 0,
    awayTeamId: 0,
    refereeId: null,
    status: 'SCHEDULED',
  });

  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [scenarios, setScenarios] = useState<Scenario[]>([]);
  const [referees, setReferees] = useState<Referee[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    if (matchId) loadMatch();
  }, [matchId]);

  useEffect(() => {
    if (formData.tournamentId > 0) loadCategories();
  }, [formData.tournamentId]);

  useEffect(() => {
    if (formData.tournamentId > 0 && formData.categoryId > 0) loadTeams();
  }, [formData.tournamentId, formData.categoryId]);

  const loadInitialData = async () => {
    setLoadingData(true);
    try {
      const tournamentsData = await tournamentsService.getAll();
      setTournaments(tournamentsData);

      const scenariosData = await venuesService.getAllScenarios();
      setScenarios(scenariosData.content || scenariosData);

      const refereesData = await usersService.getUsersByRole('REFEREE');
      setReferees(refereesData);
    } catch (error) {
      console.error('Error al cargar datos iniciales:', error);
      alert('Error al cargar datos del formulario');
    } finally {
      setLoadingData(false);
    }
  };

  const loadMatch = async () => {
    try {
      const match = await matchesService.getById(matchId!);
      setFormData({
        tournamentId: match.tournament.id,
        categoryId: match.category.id,
        scenarioId: match.scenario?.id || null,
        startsAt: match.startsAt ? formatDateForInput(match.startsAt) : '',
        homeTeamId: match.homeTeam.id,
        awayTeamId: match.awayTeam.id,
        refereeId: match.referee?.id || null,
        status: match.status,
      });
    } catch (error) {
      console.error('Error al cargar partido:', error);
      alert('Error al cargar datos del partido');
    }
  };

  const loadCategories = async () => {
    try {
      const categoriesData = await categoriesService.getAll();
      setCategories(categoriesData.content || categoriesData);
    } catch (error) {
      console.error('Error al cargar categorías:', error);
    }
  };

  const loadTeams = async () => {
    try {
      const teamsData = await teamsService.getByTournamentAndCategory(
        formData.tournamentId,
        formData.categoryId
      );
      setTeams(teamsData);
    } catch (error) {
      console.error('Error al cargar equipos:', error);
      setTeams([]);
    }
  };

  const formatDateForInput = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]:
        value === '' || value === 'null'
          ? null
          : ['tournamentId', 'categoryId', 'scenarioId', 'homeTeamId', 'awayTeamId', 'refereeId'].includes(name)
          ? Number(value)
          : value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.startsAt) {
      alert('Selecciona la fecha y hora del partido');
      return;
    }

    setLoading(true);
    try {
      const dataToSend: MatchCreateDTO = {
        ...formData,
        startsAt: new Date(formData.startsAt).toISOString(),
      };

      if (matchId) {
        await matchesService.update(matchId, dataToSend);
        alert('✅ Partido actualizado correctamente');
      } else {
        await matchesService.create(dataToSend);
        alert('✅ Partido creado exitosamente');
      }

      onSuccess();
    } catch (error: any) {
      console.error('Error al guardar partido:', error);
      alert(error.response?.data?.message || '❌ Error al guardar partido');
    } finally {
      setLoading(false);
    }
  };

  const isScheduled = formData.status === 'SCHEDULED';

  if (loadingData) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* ⚠️ Aviso cuando el partido no está programado */}
      {!isScheduled && (
        <div className="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-800 p-4 rounded-lg">
          <p className="font-semibold">⚠️ Este partido no está en estado "Programado".</p>
          <p className="text-sm">
            Solo puedes modificar la <strong>fecha y hora</strong>, el <strong>escenario</strong> o el <strong>árbitro</strong>.
          </p>
        </div>
      )}

      {/* Torneo */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">Torneo *</label>
        <select
          name="tournamentId"
          value={formData.tournamentId}
          onChange={handleChange}
          required
          disabled={!isScheduled}
          className="w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow disabled:bg-gray-100 disabled:text-gray-500"
        >
          <option value="">-- Selecciona un torneo --</option>
          {tournaments.map(t => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </select>
      </div>

      {/* Categoría */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">Categoría *</label>
        <select
          name="categoryId"
          value={formData.categoryId}
          onChange={handleChange}
          required
          disabled={!isScheduled || !formData.tournamentId}
          className="w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow disabled:bg-gray-100 disabled:text-gray-500"
        >
          <option value="">-- Selecciona una categoría --</option>
          {categories.map(c => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>

      {/* Equipos */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-gray-800 font-semibold mb-2">Equipo Local *</label>
          <select
            name="homeTeamId"
            value={formData.homeTeamId}
            onChange={handleChange}
            required
            disabled={!isScheduled || teams.length === 0}
            className="w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow disabled:bg-gray-100 disabled:text-gray-500"
          >
            <option value="">-- Selecciona equipo local --</option>
            {teams.map(t => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-gray-800 font-semibold mb-2">Equipo Visitante *</label>
          <select
            name="awayTeamId"
            value={formData.awayTeamId}
            onChange={handleChange}
            required
            disabled={!isScheduled || teams.length === 0}
            className="w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow disabled:bg-gray-100 disabled:text-gray-500"
          >
            <option value="">-- Selecciona equipo visitante --</option>
            {teams.map(t => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Fecha y Hora */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">Fecha y Hora del Partido *</label>
        <input
          type="datetime-local"
          name="startsAt"
          value={formData.startsAt}
          onChange={handleChange}
          required
          className="w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow"
        />
      </div>

      {/* Escenario */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">Escenario (Opcional)</label>
        <select
          name="scenarioId"
          value={formData.scenarioId || ''}
          onChange={handleChange}
          className="w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow"
        >
          <option value="">-- Sin escenario asignado --</option>
          {scenarios.map(s => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>
      </div>

      {/* Árbitro */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">Árbitro (Opcional)</label>
        <select
          name="refereeId"
          value={formData.refereeId || ''}
          onChange={handleChange}
          className="w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow"
        >
          <option value="">-- Sin árbitro asignado --</option>
          {referees.map(r => (
            <option key={r.id} value={r.id}>
              {r.fullName}
            </option>
          ))}
        </select>
      </div>

      {/* Botones */}
      <div className="flex gap-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          className="flex-1 px-6 py-3 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition font-semibold"
          disabled={loading}
        >
          Cancelar
        </button>
        <button
          type="submit"
          className="flex-1 px-6 py-3 bg-uptc-black text-uptc-yellow rounded-lg hover:bg-gray-800 transition font-semibold disabled:opacity-50"
          disabled={loading}
        >
          {loading ? 'Guardando...' : matchId ? 'Actualizar Partido' : 'Crear Partido'}
        </button>
      </div>
    </form>
  );
}