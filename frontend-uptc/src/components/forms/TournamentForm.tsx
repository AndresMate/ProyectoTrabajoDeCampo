'use client';

// typescript
import { useState, useEffect } from 'react';
import { tournamentsService } from '@/services/tournamentsService';
import sportsService from '@/services/sportsService';
import categoriesService from '@/services/categoriesService';
import { authService } from '@/services/authService';
import { toastError, toastWarning, toastPromise } from '@/utils/toast';

type Modality = 'DIURNO' | 'NOCTURNO';
type Status =
  | 'PLANNING'
  | 'OPEN_FOR_INSCRIPTION'
  | 'IN_PROGRESS'
  | 'FINISHED'
  | 'CANCELLED';

interface Sport {
  id: number;
  name: string;
}

interface Category {
  id: number;
  name: string;
}

interface FormData {
  name: string;
  maxTeams: number;
  startDate: string;
  endDate: string;
  modality: Modality;
  status: Status;
  sportId: number;
  categoryId: number;
  createdById: number;
}

interface TournamentDTO {
  name?: string;
  maxTeams?: number;
  startDate?: string;
  endDate?: string;
  modality?: Modality;
  status?: Status;
  sport?: { id: number };
  category?: { id: number };
  createdBy?: { id: number };
}

interface Props {
  tournamentId?: number;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function TournamentForm({ tournamentId, onSuccess, onCancel }: Props) {
  const [loading, setLoading] = useState<boolean>(false);
  const [sports, setSports] = useState<Sport[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [formData, setFormData] = useState<FormData>({
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
  const [errors, setErrors] = useState<Partial<Record<keyof FormData, string>>>({});

  // =========================
  // Cargar datos iniciales
  // =========================
  useEffect(() => {
    fetchSports();

    const user = authService.getCurrentUser();
    if (user?.userId) {
      setFormData(prev => ({ ...prev, createdById: user.userId }));
    }

    if (tournamentId) {
      loadTournament(tournamentId);
    }
  }, [tournamentId]);

  useEffect(() => {
    if (formData.sportId) {
      fetchCategories(formData.sportId);
    } else {
      setCategories([]);
      setFormData(prev => ({ ...prev, categoryId: 0 }));
    }
  }, [formData.sportId]);

  const fetchSports = async () => {
    try {
      const data = (await sportsService.getActive()) as Sport[];
      setSports(data);
    } catch (err) {
      console.error('Error cargando deportes:', err);
    }
  };

  const fetchCategories = async (sportId: number) => {
    try {
      const data = (await categoriesService.getActiveBySport(sportId)) as Category[];
      setCategories(data);
    } catch (err) {
      console.error('Error cargando categorías:', err);
    }
  };

  const loadTournament = async (id: number) => {
    setLoading(true);
    try {
      const data = (await tournamentsService.getById(id)) as TournamentDTO;
      setFormData({
        name: data.name ?? '',
        maxTeams: data.maxTeams ?? 0,
        startDate: data.startDate ?? '',
        endDate: data.endDate ?? '',
        modality: (data.modality ?? 'DIURNO') as Modality,
        status: (data.status ?? 'PLANNING') as Status,
        sportId: data.sport?.id ?? 0,
        categoryId: data.category?.id ?? 0,
        createdById: data.createdBy?.id ?? 0,
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
    const newErrors: Partial<Record<keyof FormData, string>> = {};
    if (!formData.name.trim()) newErrors.name = 'El nombre es requerido';
    if (!formData.startDate) newErrors.startDate = 'La fecha de inicio es requerida';
    if (!formData.endDate) newErrors.endDate = 'La fecha de fin es requerida';
    if (!formData.sportId) newErrors.sportId = 'Selecciona un deporte';
    if (!formData.categoryId) {
      if (formData.sportId && categories.length === 0) {
        newErrors.categoryId = 'Este deporte no tiene categorías. Por favor, crea al menos una categoría primero.';
      } else {
        newErrors.categoryId = 'Selecciona una categoría';
      }
    }
    if (formData.maxTeams <= 0) newErrors.maxTeams = 'Debe haber al menos un equipo permitido';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // =========================
  // Manejo de inputs
  // =========================
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const name = e.target.name as keyof FormData;
    const value = e.target.value;
    setFormData(prev => ({
      ...prev,
      [name]:
        name === 'sportId' || name === 'categoryId' || name === 'maxTeams'
          ? Number(value)
          : (value as any),
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
        toastError('Error: usuario no autenticado');
        setLoading(false);
        return;
      }

      const payload: Partial<FormData> = {
        ...formData,
        createdById: user.userId,
      };

      if (tournamentId) {
        await toastPromise(
          tournamentsService.update(tournamentId, payload),
          {
            loading: 'Actualizando torneo...',
            success: '✅ Torneo actualizado correctamente',
            error: 'Error al guardar el torneo'
          }
        );
      } else {
        await toastPromise(
          tournamentsService.create(payload),
          {
            loading: 'Creando torneo...',
            success: '✅ Torneo creado correctamente',
            error: 'Error al guardar el torneo'
          }
        );
      }

      onSuccess();
    } catch (error: unknown) {
      console.error('❌ Error al guardar torneo:', error);
      // El error ya se muestra en el toastPromise
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
          <option value={0}>Selecciona un deporte</option>
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
        {formData.sportId && categories.length === 0 ? (
          <div className="w-full border-2 border-yellow-400 bg-yellow-50 p-3 rounded">
            <p className="text-yellow-800 text-sm font-medium">
              ⚠️ Este deporte no tiene categorías disponibles. Por favor, crea al menos una categoría para este deporte antes de crear el torneo.
            </p>
          </div>
        ) : (
          <>
            <select
              name="categoryId"
              value={formData.categoryId}
              onChange={handleChange}
              required
              disabled={!formData.sportId || categories.length === 0}
              className={`w-full border p-2 rounded ${
                !formData.sportId || categories.length === 0 ? 'bg-gray-100 cursor-not-allowed' : ''
              }`}
            >
              <option value={0}>
                {!formData.sportId 
                  ? 'Primero selecciona un deporte' 
                  : categories.length === 0 
                    ? 'No hay categorías disponibles' 
                    : 'Selecciona una categoría'}
              </option>
              {categories.map(c => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
            {errors.categoryId && <p className="text-red-500 text-sm">{errors.categoryId}</p>}
          </>
        )}
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
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-uptc-black"
        >
          {loading ? 'Guardando...' : tournamentId ? 'Actualizar' : 'Crear'}
        </button>
      </div>
    </form>
  );
}