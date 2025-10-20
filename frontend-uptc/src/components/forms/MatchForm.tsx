'use client';

import { useState, useEffect } from 'react';
import matchesService from '@/services/matchesService';
import { tournamentsService } from '@/services/tournamentsService';
import categoriesService from '@/services/categoriesService';
import teamsService from '@/services/teamsService';
import venuesService from '@/services/venuesService';

interface MatchFormProps {
  matchId?: number;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function MatchForm({ matchId, onSuccess, onCancel }: MatchFormProps) {
  const [loading, setLoading] = useState(false);
  const [tournaments, setTournaments] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [teams, setTeams] = useState<any[]>([]);
  const [venues, setVenues] = useState<any[]>([]);
  const [scenarios, setScenarios] = useState<any[]>([]);

  const [formData, setFormData] = useState({
    tournamentId: 0,
    categoryId: 0,
    homeTeamId: 0,
    awayTeamId: 0,
    matchDate: '',
    venueId: 0,
    scenarioId: 0,
  });

  const [errors, setErrors] = useState<any>({});

  useEffect(() => {
    fetchTournaments();
    fetchVenues();
  }, []);

  const fetchTournaments = async () => {
    try {
      const data = await tournamentsService.getAll();
      setTournaments(data);
    } catch (error) {
      console.error('Error al cargar torneos:', error);
    }
  };

  const fetchVenues = async () => {
    try {
      const data = await venuesService.getAll();
      setVenues(data.content || data);
    } catch (error) {
      console.error('Error al cargar sedes:', error);
    }
  };

  const fetchCategories = async (tournamentId: number) => {
    try {
      const tournament = await tournamentsService.getById(tournamentId.toString());
      if (tournament.sport?.id) {
        const data = await categoriesService.getActiveBySport(tournament.sport.id);
        setCategories(data);
      }
    } catch (error) {
      console.error('Error al cargar categorías:', error);
    }
  };

  const fetchTeams = async (tournamentId: number, categoryId: number) => {
    try {
      const data = await teamsService.getByTournamentAndCategory(tournamentId, categoryId);
      setTeams(data);
    } catch (error) {
      console.error('Error al cargar equipos:', error);
    }
  };

  const fetchScenarios = async (venueId: number) => {
    try {
      const data = await venuesService.getScenariosByVenue(venueId);
      setScenarios(data);
    } catch (error) {
      console.error('Error al cargar escenarios:', error);
    }
  };

  const validateForm = () => {
    const newErrors: any = {};

    if (!formData.tournamentId) newErrors.tournamentId = 'Selecciona un torneo';
    if (!formData.categoryId) newErrors.categoryId = 'Selecciona una categoría';
    if (!formData.homeTeamId) newErrors.homeTeamId = 'Selecciona el equipo local';
    if (!formData.awayTeamId) newErrors.awayTeamId = 'Selecciona el equipo visitante';
    if (!formData.matchDate) newErrors.matchDate = 'Selecciona fecha y hora';

    if (formData.homeTeamId === formData.awayTeamId) {
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
      [name]: numericFields.includes(name) ? Number(value) : value
    }));

    // Lógica de cascada
    if (name === 'tournamentId' && value) {
      fetchCategories(Number(value));
      setFormData(prev => ({ ...prev, categoryId: 0, homeTeamId: 0, awayTeamId: 0 }));
      setTeams([]);
    }

    if (name === 'categoryId' && value && formData.tournamentId) {
      fetchTeams(formData.tournamentId, Number(value));
      setFormData(prev => ({ ...prev, homeTeamId: 0, awayTeamId: 0 }));
    }

    if (name === 'venueId' && value) {
      fetchScenarios(Number(value));
      setFormData(prev => ({ ...prev, scenarioId: 0 }));
    }

    if (errors[name]) {
      setErrors((prev: any) => ({ ...prev, [name]: undefined }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      alert('Por favor corrige los errores en el formulario');
      return;
    }

    setLoading(true);

    try {
      await matchesService.create({
        ...formData,
        startsAt: formData.matchDate
      });
      alert('✅ Partido creado correctamente');
      onSuccess();
    } catch (error: any) {
      console.error('Error al crear partido:', error);
      alert(error.response?.data?.message || 'Error al crear el partido');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Torneo */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">
            Torneo *
          </label>
          <select
            name="tournamentId"
            value={formData.tournamentId}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${
              errors.tournamentId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          >
            <option value="0">Selecciona un torneo</option>
            {tournaments.map(t => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
          {errors.tournamentId && (
            <p className="text-red-500 text-sm mt-1">{errors.tournamentId}</p>
          )}
        </div>

        {/* Categoría */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">
            Categoría *
          </label>
          <select
            name="categoryId"
            value={formData.categoryId}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${
              errors.categoryId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
            disabled={!formData.tournamentId}
          >
            <option value="0">
              {formData.tournamentId ? 'Selecciona una categoría' : 'Primero selecciona un torneo'}
            </option>
            {categories.map(c => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          {errors.categoryId && (
            <p className="text-red-500 text-sm mt-1">{errors.categoryId}</p>
          )}
        </div>

        {/* Equipo Local */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">
            Equipo Local *
          </label>
          <select
            name="homeTeamId"
            value={formData.homeTeamId}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${
              errors.homeTeamId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
            disabled={teams.length === 0}
          >
            <option value="0">
              {teams.length > 0 ? 'Selecciona equipo local' : 'Carga equipos primero'}
            </option>
            {teams.map(t => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
          {errors.homeTeamId && (
            <p className="text-red-500 text-sm mt-1">{errors.homeTeamId}</p>
          )}
        </div>

        {/* Equipo Visitante */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">
            Equipo Visitante *
          </label>
          <select
            name="awayTeamId"
            value={formData.awayTeamId}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${
              errors.awayTeamId ? 'border-red-500' : 'border-gray-300'
            }`}
            required
            disabled={teams.length === 0}
          >
            <option value="0">
              {teams.length > 0 ? 'Selecciona equipo visitante' : 'Carga equipos primero'}
            </option>
            {teams.filter(t => t.id !== formData.homeTeamId).map(t => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
          {errors.awayTeamId && (
            <p className="text-red-500 text-sm mt-1">{errors.awayTeamId}</p>
          )}
        </div>

        {/* Fecha y Hora */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">
            Fecha y Hora del Partido *
          </label>
          <input
            type="datetime-local"
            name="matchDate"
            value={formData.matchDate}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 ${
              errors.matchDate ? 'border-red-500' : 'border-gray-300'
            }`}
            required
          />
          {errors.matchDate && (
            <p className="text-red-500 text-sm mt-1">{errors.matchDate}</p>
          )}
        </div>

        {/* Sede */}
        <div>
          <label className="block font-semibold mb-2 text-gray-700">
            Sede (Opcional)
          </label>
          <select
            name="venueId"
            value={formData.venueId}
            onChange={handleChange}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            <option value="0">Sin sede específica</option>
            {venues.map(v => (
              <option key={v.id} value={v.id}>
                {v.name}
              </option>
            ))}
          </select>
        </div>

        {/* Escenario */}
        {formData.venueId > 0 && (
          <div>
            <label className="block font-semibold mb-2 text-gray-700">
              Escenario (Opcional)
            </label>
            <select
              name="scenarioId"
              value={formData.scenarioId}
              onChange={handleChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            >
              <option value="0">Sin escenario específico</option>
              {scenarios.map(s => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
        )}
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
          className="px-6 py-2 bg-blue-900 text-white rounded-lg hover:bg-blue-800 transition disabled:opacity-50"
        >
          {loading ? 'Creando...' : 'Crear Partido'}
        </button>
      </div>
    </form>
  );
}