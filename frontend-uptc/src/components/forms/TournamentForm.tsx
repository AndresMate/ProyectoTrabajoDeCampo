'use client';

import { useState, useEffect } from 'react';
import { tournamentsService } from '@/services/tournamentsService';
import sportsService from '@/services/sportsService';
import categoriesService from '@/services/categoriesService';
import { authService } from '@/services/authService';

export default function TournamentForm({ tournamentId, onSuccess, onCancel }: any) {
  const [loading, setLoading] = useState(false);
  const [sports, setSports] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [formData, setFormData] = useState({
    name: '',
    maxTeams: 0,
    startDate: '',
    endDate: '',
    modality: 'DIURNO',
    status: 'PLANNING',
    sportId: 0,
    categoryId: 0,
    createdById: 0,
  });
  const [errors, setErrors] = useState<any>({});

  // =========================
  // Cargar datos iniciales
  // =========================
  useEffect(() => {
    fetchSports();

    const user = authService.getCurrentUser();
    if (user?.userId) {
      setFormData(prev => ({ ...prev, createdById: user.userId }));
    }

    // Si viene un ID, cargamos los datos del torneo existente
    if (tournamentId) {
      loadTournament(tournamentId);
    }
  }, [tournamentId]);

  useEffect(() => {
    if (formData.sportId) {
      fetchCategories(formData.sportId);
    }
  }, [formData.sportId]);

  const fetchSports = async () => {
    try {
      const data = await sportsService.getActive();
      setSports(data);
    } catch (err) {
      console.error('Error cargando deportes:', err);
    }
  };

  const fetchCategories = async (sportId: number) => {
    try {
      const data = await categoriesService.getActiveBySport(sportId);
      setCategories(data);
    } catch (err) {
      console.error('Error cargando categorías:', err);
    }
  };

  // ✅ Cargar datos del torneo existente
  const loadTournament = async (id: number) => {
    setLoading(true);
    try {
      const data = await tournamentsService.getById(id);
      setFormData({
        name: data.name || '',
        maxTeams: data.maxTeams || 0,
        startDate: data.startDate || '',
        endDate: data.endDate || '',
        modality: data.modality || 'DIURNO',
        status: data.status || 'PLANNING',
        sportId: data.sport?.id || 0,
        categoryId: data.category?.id || 0,
        createdById: data.createdBy?.id || 0,
      });
    } catch (err) {
      console.error('Error cargando torneo:', err);
    } finally {
      setLoading(false);
    }
  };

  // =========================
  // Validaciones
  // =========================
  const validateForm = () => {
    const newErrors: any = {};
    if (!formData.name.trim()) newErrors.name = 'El nombre es requerido';
    if (!formData.startDate) newErrors.startDate = 'La fecha de inicio es requerida';
    if (!formData.endDate) newErrors.endDate = 'La fecha de fin es requerida';
    if (!formData.sportId) newErrors.sportId = 'Selecciona un deporte';
    if (!formData.categoryId) newErrors.categoryId = 'Selecciona una categoría';
    if (formData.maxTeams <= 0) newErrors.maxTeams = 'Debe haber al menos un equipo permitido';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // =========================
  // Manejo de inputs
  // =========================
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: ['sportId', 'categoryId', 'maxTeams'].includes(name)
        ? Number(value)
        : value,
    }));
  };

  // =========================
  // Guardar torneo (crear o editar)
  // =========================
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;
    setLoading(true);

    try {
      const user = authService.getCurrentUser();
      if (!user?.userId) {
        alert('Error: usuario no autenticado');
        return;
      }

      const payload = {
        ...formData,
        createdById: user.userId,
      };

      if (tournamentId) {
        await tournamentsService.update(tournamentId, payload);
        alert('✅ Torneo actualizado correctamente');
      } else {
        await tournamentsService.create(payload);
        alert('✅ Torneo creado correctamente');
      }

      onSuccess();
    } catch (error: any) {
      console.error('❌ Error al guardar torneo:', error);
      alert('Error al guardar el torneo. Revisa la consola.');
    } finally {
      setLoading(false);
    }
  };

  // =========================
  // Render del formulario
  // =========================
  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-6 p-4 border rounded-lg shadow-md bg-white"
    >
      {loading && <p className="text-center text-gray-500">Cargando...</p>}

      {/* Nombre */}
      <div>
        <label className="block font-semibold mb-1">Nombre del Torneo *</label>
        <input
          name="name"
          value={formData.name}
          onChange={handleChange}
          required
          className="w-full border p-2 rounded"
        />
        {errors.name && <p className="text-red-500 text-sm">{errors.name}</p>}
      </div>

      {/* Máximo de equipos */}
      <div>
        <label className="block font-semibold mb-1">Número máximo de equipos *</label>
        <input
          type="number"
          name="maxTeams"
          value={formData.maxTeams}
          onChange={handleChange}
          required
          className="w-full border p-2 rounded"
          min={1}
        />
        {errors.maxTeams && <p className="text-red-500 text-sm">{errors.maxTeams}</p>}
      </div>

      {/* Modalidad */}
      <div>
        <label className="block font-semibold mb-1">Modalidad *</label>
        <select
          name="modality"
          value={formData.modality}
          onChange={handleChange}
          className="w-full border p-2 rounded"
          required
        >
          <option value="DIURNO">Diurna</option>
          <option value="NOCTURNO">Nocturna</option>
        </select>
      </div>

      {/* Estado */}
      <div>
        <label className="block font-semibold mb-1">Estado del Torneo *</label>
        <select
          name="status"
          value={formData.status}
          onChange={handleChange}
          className="w-full border p-2 rounded"
          required
        >
          <option value="PLANNING">Planificación</option>
          <option value="OPEN_FOR_INSCRIPTION">Inscripciones abiertas</option>
          <option value="IN_PROGRESS">En curso</option>
          <option value="FINISHED">Finalizado</option>
          <option value="CANCELLED">Cancelado</option>
        </select>
      </div>

      {/* Deporte */}
      <div>
        <label className="block font-semibold mb-1">Deporte *</label>
        <select
          name="sportId"
          value={formData.sportId}
          onChange={handleChange}
          required
          className="w-full border p-2 rounded"
        >
          <option value="0">Selecciona un deporte</option>
          {sports.map(s => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>
        {errors.sportId && <p className="text-red-500 text-sm">{errors.sportId}</p>}
      </div>

      {/* Categoría */}
      <div>
        <label className="block font-semibold mb-1">Categoría *</label>
        <select
          name="categoryId"
          value={formData.categoryId}
          onChange={handleChange}
          required
          className="w-full border p-2 rounded"
        >
          <option value="0">Selecciona una categoría</option>
          {categories.map(c => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        {errors.categoryId && <p className="text-red-500 text-sm">{errors.categoryId}</p>}
      </div>

      {/* Fechas */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block font-semibold mb-1">Fecha de inicio *</label>
          <input
            type="date"
            name="startDate"
            value={formData.startDate}
            onChange={handleChange}
            required
            className="w-full border p-2 rounded"
          />
          {errors.startDate && <p className="text-red-500 text-sm">{errors.startDate}</p>}
        </div>

        <div>
          <label className="block font-semibold mb-1">Fecha de fin *</label>
          <input
            type="date"
            name="endDate"
            value={formData.endDate}
            onChange={handleChange}
            required
            className="w-full border p-2 rounded"
          />
          {errors.endDate && <p className="text-red-500 text-sm">{errors.endDate}</p>}
        </div>
      </div>

      {/* Botones */}
      <div className="flex justify-end gap-3">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          {loading ? 'Guardando...' : tournamentId ? 'Actualizar' : 'Crear'}
        </button>
      </div>
    </form>
  );
}
