// frontend-uptc/src/components/forms/MatchForm.tsx
'use client';

import { useEffect, useState } from 'react';
import matchesService, { MatchCreateDTO, Match } from '@/services/matchesService';
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
  // Estados del formulario
  const [formData, setFormData] = useState<MatchCreateDTO>({
    tournamentId: 0,
    categoryId: 0,
    scenarioId: null,
    startsAt: '',
    homeTeamId: 0,
    awayTeamId: 0,
    refereeId: null,
    status: 'SCHEDULED'
  });

  // Estados para los dropdowns
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [scenarios, setScenarios] = useState<Scenario[]>([]);
  const [referees, setReferees] = useState<Referee[]>([]);

  // Estados de carga
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);

  // Cargar datos iniciales
  useEffect(() => {
    loadInitialData();
  }, []);

  // Cargar partido existente si estamos en modo edición
  useEffect(() => {
    if (matchId) {
      loadMatch();
    }
  }, [matchId]);

  // Cargar categorías cuando cambia el torneo
  useEffect(() => {
    if (formData.tournamentId > 0) {
      loadCategories();
    }
  }, [formData.tournamentId]);

  // Cargar equipos cuando cambian torneo y categoría
  useEffect(() => {
    if (formData.tournamentId > 0 && formData.categoryId > 0) {
      loadTeams();
    }
  }, [formData.tournamentId, formData.categoryId]);

  const loadInitialData = async () => {
    setLoadingData(true);
    try {
      // Cargar torneos
      const tournamentsData = await tournamentsService.getAll();
      setTournaments(tournamentsData);

      // Cargar escenarios
      const scenariosData = await venuesService.getAllScenarios();
      setScenarios(scenariosData.content || scenariosData);

      // Cargar árbitros
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

      // ✅ CRÍTICO: Convertir de Match (respuesta) a MatchCreateDTO (formulario)
      setFormData({
        tournamentId: match.tournament.id,
        categoryId: match.category.id,
        scenarioId: match.scenario?.id || null,
        startsAt: match.startsAt ? formatDateForInput(match.startsAt) : '',
        homeTeamId: match.homeTeam.id,  // ✅ Extraer solo el ID
        awayTeamId: match.awayTeam.id,  // ✅ Extraer solo el ID
        refereeId: match.referee?.id || null,
        status: match.status
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

  // Formatear fecha para el input datetime-local
  const formatDateForInput = (dateString: string): string => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;

    setFormData(prev => ({
      ...prev,
      [name]: value === '' || value === 'null'
        ? null
        : ['tournamentId', 'categoryId', 'scenarioId', 'homeTeamId', 'awayTeamId', 'refereeId'].includes(name)
          ? Number(value)
          : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validaciones
    if (!formData.tournamentId || formData.tournamentId === 0) {
      alert('Selecciona un torneo');
      return;
    }

    if (!formData.categoryId || formData.categoryId === 0) {
      alert('Selecciona una categoría');
      return;
    }

    if (!formData.homeTeamId || formData.homeTeamId === 0) {
      alert('Selecciona el equipo local');
      return;
    }

    if (!formData.awayTeamId || formData.awayTeamId === 0) {
      alert('Selecciona el equipo visitante');
      return;
    }

    if (formData.homeTeamId === formData.awayTeamId) {
      alert('Los equipos local y visitante deben ser diferentes');
      return;
    }

    if (!formData.startsAt) {
      alert('Selecciona la fecha y hora del partido');
      return;
    }

    setLoading(true);
    try {
      // ✅ Preparar datos para enviar (convertir datetime-local a ISO)
      const dataToSend: MatchCreateDTO = {
        ...formData,
        startsAt: formData.startsAt ? new Date(formData.startsAt).toISOString() : null,
        scenarioId: formData.scenarioId || null,
        refereeId: formData.refereeId || null
      };

      console.log('📤 Enviando partido:', dataToSend);

      if (matchId) {
        await matchesService.update(matchId, dataToSend);
        alert('✅ Partido actualizado exitosamente');
      } else {
        await matchesService.create(dataToSend);
        alert('✅ Partido creado exitosamente');
      }

      onSuccess();
    } catch (error: any) {
      console.error('❌ Error al guardar partido:', error);
      const message = error.response?.data?.message || error.message || 'Error al guardar el partido';
      alert(`❌ ${message}`);
    } finally {
      setLoading(false);
    }
  };

  if (loadingData) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Torneo */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">
          Torneo *
        </label>
        <select
          name="tournamentId"
          value={formData.tournamentId}
          onChange={handleChange}
          required
          className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900"
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
        <label className="block text-gray-800 font-semibold mb-2">
          Categoría *
        </label>
        <select
          name="categoryId"
          value={formData.categoryId}
          onChange={handleChange}
          required
          disabled={!formData.tournamentId}
          className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900 disabled:bg-gray-100 disabled:cursor-not-allowed"
        >
          <option value="">-- Selecciona una categoría --</option>
          {categories.map(c => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>

      {/* Equipos en dos columnas */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Equipo Local */}
        <div>
          <label className="block text-gray-800 font-semibold mb-2">
            Equipo Local *
          </label>
          <select
            name="homeTeamId"
            value={formData.homeTeamId}
            onChange={handleChange}
            required
            disabled={teams.length === 0}
            className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900 disabled:bg-gray-100 disabled:cursor-not-allowed"
          >
            <option value="">-- Selecciona equipo local --</option>
            {teams.map(t => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </div>

        {/* Equipo Visitante */}
        <div>
          <label className="block text-gray-800 font-semibold mb-2">
            Equipo Visitante *
          </label>
          <select
            name="awayTeamId"
            value={formData.awayTeamId}
            onChange={handleChange}
            required
            disabled={teams.length === 0}
            className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900 disabled:bg-gray-100 disabled:cursor-not-allowed"
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
        <label className="block text-gray-800 font-semibold mb-2">
          Fecha y Hora del Partido *
        </label>
        <input
          type="datetime-local"
          name="startsAt"
          value={formData.startsAt}
          onChange={handleChange}
          required
          className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900"
        />
      </div>

      {/* Escenario */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">
          Escenario (Opcional)
        </label>
        <select
          name="scenarioId"
          value={formData.scenarioId || ''}
          onChange={handleChange}
          className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900"
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
        <label className="block text-gray-800 font-semibold mb-2">
          Árbitro (Opcional)
        </label>
        <select
          name="refereeId"
          value={formData.refereeId || ''}
          onChange={handleChange}
          className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900"
        >
          <option value="">-- Sin árbitro asignado --</option>
          {referees.map(r => (
            <option key={r.id} value={r.id}>
              {r.fullName}
            </option>
          ))}
        </select>
      </div>

      {/* Estado (solo en edición) */}
      {matchId && (
        <div>
          <label className="block text-gray-800 font-semibold mb-2">
            Estado
          </label>
          <select
            name="status"
            value={formData.status}
            onChange={handleChange}
            className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900"
          >
            <option value="SCHEDULED">Programado</option>
            <option value="IN_PROGRESS">En Curso</option>
            <option value="FINISHED">Finalizado</option>
            <option value="CANCELLED">Cancelado</option>
          </select>
        </div>
      )}

      {/* Alerta si no hay equipos */}
      {formData.tournamentId > 0 && formData.categoryId > 0 && teams.length === 0 && (
        <div className="bg-yellow-50 border-l-4 border-yellow-500 p-4 rounded">
          <p className="text-yellow-800 text-sm font-medium">
            ⚠️ No hay equipos disponibles para este torneo y categoría.
            Asegúrate de que haya equipos inscritos y aprobados.
          </p>
        </div>
      )}

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
          className="flex-1 px-6 py-3 bg-uptc-black text-uptc-yellow rounded-lg hover:bg-gray-800 transition font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
          disabled={loading || teams.length === 0}
        >
          {loading ? (
            <>
              <span className="inline-block animate-spin mr-2">⏳</span>
              {matchId ? 'Actualizando...' : 'Creando...'}
            </>
          ) : (
            matchId ? 'Actualizar Partido' : 'Crear Partido'
          )}
        </button>
      </div>
    </form>
  );
}