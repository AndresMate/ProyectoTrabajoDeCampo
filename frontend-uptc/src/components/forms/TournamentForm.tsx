'use client';

// typescript
import { useState, useEffect } from 'react';
import { tournamentsService } from '@/services/tournamentsService';
import sportsService from '@/services/sportsService';
import categoriesService from '@/services/categoriesService';
import { authService } from '@/services/authService';
import { toastError, toastWarning, toastPromise, getErrorMessage } from '@/utils/toast';

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
  inscriptionStartDate: string;
  inscriptionEndDate: string;
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
  inscriptionStartDate?: string;
  inscriptionEndDate?: string;
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
    inscriptionStartDate: '',
    inscriptionEndDate: '',
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
        inscriptionStartDate: data.inscriptionStartDate ?? '',
        inscriptionEndDate: data.inscriptionEndDate ?? '',
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
    if (!formData.startDate) {
      newErrors.startDate = 'La fecha de inicio es requerida';
    } else {
      // Validar que la fecha de inicio no sea pasada
      const startDate = new Date(formData.startDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      if (startDate < today) {
        newErrors.startDate = 'La fecha de inicio no puede ser anterior a la fecha actual. Por favor, seleccione una fecha de hoy en adelante';
      }
    }
    if (!formData.endDate) {
      newErrors.endDate = 'La fecha de fin es requerida';
    } else if (formData.startDate) {
      // Validar que la fecha de fin sea posterior o igual a la fecha de inicio
      const startDate = new Date(formData.startDate);
      const endDate = new Date(formData.endDate);
      if (endDate < startDate) {
        newErrors.endDate = 'La fecha de fin debe ser posterior o igual a la fecha de inicio. Por favor, seleccione una fecha de fin que sea igual o posterior a la fecha de inicio';
      }
    }
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
  // Calcular fechas sugeridas
  // =========================
  const calculateSuggestedInscriptionDates = (startDate: string) => {
    if (!startDate) return { inscriptionStartDate: '', inscriptionEndDate: '' };
    
    const start = new Date(startDate);
    const inscriptionStart = new Date(start);
    inscriptionStart.setDate(inscriptionStart.getDate() - 30);
    
    const inscriptionEnd = new Date(start);
    inscriptionEnd.setDate(inscriptionEnd.getDate() - 1);
    
    return {
      inscriptionStartDate: inscriptionStart.toISOString().split('T')[0],
      inscriptionEndDate: inscriptionEnd.toISOString().split('T')[0],
    };
  };

  // =========================
  // Validar y corregir fechas automáticamente
  // =========================
  const validateAndCorrectDates = (newFormData: FormData): FormData => {
    let corrected = { ...newFormData };

    // Si hay startDate, validar y corregir fechas de inscripción
    if (corrected.startDate) {
      const startDateObj = new Date(corrected.startDate);
      
      // Si inscriptionEndDate es posterior o igual a startDate, corregir
      if (corrected.inscriptionEndDate) {
        const inscriptionEndDateObj = new Date(corrected.inscriptionEndDate);
        if (inscriptionEndDateObj >= startDateObj) {
          // Ajustar a 1 día antes del inicio
          const correctedEnd = new Date(startDateObj);
          correctedEnd.setDate(correctedEnd.getDate() - 1);
          corrected.inscriptionEndDate = correctedEnd.toISOString().split('T')[0];
        }
      }

      // Si inscriptionStartDate es posterior a inscriptionEndDate, corregir
      if (corrected.inscriptionStartDate && corrected.inscriptionEndDate) {
        const inscriptionStartDateObj = new Date(corrected.inscriptionStartDate);
        const inscriptionEndDateObj = new Date(corrected.inscriptionEndDate);
        if (inscriptionStartDateObj >= inscriptionEndDateObj) {
          // Ajustar inscriptionStartDate a 30 días antes de inscriptionEndDate
          const correctedStart = new Date(corrected.inscriptionEndDate);
          correctedStart.setDate(correctedStart.getDate() - 30);
          corrected.inscriptionStartDate = correctedStart.toISOString().split('T')[0];
        }
      }
    }

    return corrected;
  };

  // =========================
  // Manejo de inputs
  // =========================
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const name = e.target.name as keyof FormData;
    const value = e.target.value;
    
    let newFormData: FormData = {
      ...formData,
      [name]:
        name === 'sportId' || name === 'categoryId' || name === 'maxTeams'
          ? Number(value)
          : (value as any),
    };

    // Si se cambia startDate, sugerir fechas de inscripción automáticamente
    if (name === 'startDate' && value) {
      const suggested = calculateSuggestedInscriptionDates(value);
      // Solo sugerir si los campos están vacíos
      if (!newFormData.inscriptionStartDate) {
        newFormData.inscriptionStartDate = suggested.inscriptionStartDate;
      }
      if (!newFormData.inscriptionEndDate) {
        newFormData.inscriptionEndDate = suggested.inscriptionEndDate;
      }
      
      // Validar en tiempo real que la fecha no sea pasada
      const startDate = new Date(value);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      if (startDate < today) {
        setErrors(prev => ({
          ...prev,
          startDate: 'La fecha de inicio no puede ser anterior a la fecha actual. Por favor, seleccione una fecha de hoy en adelante'
        }));
      } else {
        // Limpiar el error si la fecha es válida
        setErrors(prev => {
          const newErrors = { ...prev };
          delete newErrors.startDate;
          return newErrors;
        });
      }
      
      // Si hay endDate, validar que endDate sea posterior o igual a startDate
      if (newFormData.endDate) {
        const endDate = new Date(newFormData.endDate);
        if (endDate < startDate) {
          setErrors(prev => ({
            ...prev,
            endDate: 'La fecha de fin debe ser posterior o igual a la fecha de inicio. Por favor, seleccione una fecha de fin que sea igual o posterior a la fecha de inicio'
          }));
        } else {
          setErrors(prev => {
            const newErrors = { ...prev };
            delete newErrors.endDate;
            return newErrors;
          });
        }
      }
    }
    
    // Si se cambia endDate, validar en tiempo real que sea posterior o igual a startDate
    if (name === 'endDate' && value && newFormData.startDate) {
      const startDate = new Date(newFormData.startDate);
      const endDate = new Date(value);
      
      if (endDate < startDate) {
        setErrors(prev => ({
          ...prev,
          endDate: 'La fecha de fin debe ser posterior o igual a la fecha de inicio. Por favor, seleccione una fecha de fin que sea igual o posterior a la fecha de inicio'
        }));
      } else {
        // Limpiar el error si la fecha es válida
        setErrors(prev => {
          const newErrors = { ...prev };
          delete newErrors.endDate;
          return newErrors;
        });
      }
    }

    // Validar y corregir fechas automáticamente
    newFormData = validateAndCorrectDates(newFormData);

    setFormData(newFormData);
    
    // Limpiar errores del campo modificado (excepto startDate y endDate que ya se manejan arriba)
    if (errors[name] && name !== 'startDate' && name !== 'endDate') {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
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
      
      // Extraer el mensaje de error del backend
      const errorMessage = getErrorMessage(error);
      
      // Si el error es sobre startDate, mostrarlo en el campo específico
      if (errorMessage.includes('fecha de inicio') || 
          errorMessage.includes('START_DATE') ||
          errorMessage.includes('anterior a la fecha actual') ||
          errorMessage.includes('fecha actual')) {
        setErrors(prev => ({
          ...prev,
          startDate: errorMessage || 'La fecha de inicio no puede ser anterior a la fecha actual. Por favor, seleccione una fecha de hoy en adelante'
        }));
        // No mostrar toast adicional, el error ya está en el campo
        setLoading(false);
        return;
      }
      
      // Si el error es sobre endDate, mostrarlo en el campo específico
      if (errorMessage.includes('fecha de fin') || 
          errorMessage.includes('END_DATE') ||
          errorMessage.includes('posterior a la fecha de inicio') ||
          errorMessage.includes('posterior o igual')) {
        setErrors(prev => ({
          ...prev,
          endDate: errorMessage || 'La fecha de fin debe ser posterior o igual a la fecha de inicio. Por favor, seleccione una fecha de fin que sea igual o posterior a la fecha de inicio'
        }));
        // No mostrar toast adicional, el error ya está en el campo
        setLoading(false);
        return;
      }
      
      // Para otros errores, el toastPromise ya los muestra
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

      {/* Fechas del torneo */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block font-semibold mb-1">Fecha de inicio del torneo *</label>
          {errors.startDate ? (
            <div className="w-full border-2 border-red-400 bg-red-50 p-3 rounded">
              <p className="text-red-800 text-sm font-medium">
                ❌ {errors.startDate}
              </p>
            </div>
          ) : (
            <>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                required
                min={new Date().toISOString().split('T')[0]}
                className="w-full border p-2 rounded border-gray-300"
              />
              <p className="text-gray-500 text-xs mt-1">
                La fecha debe ser hoy o una fecha futura
              </p>
            </>
          )}
        </div>

        <div>
          <label className="block font-semibold mb-1">Fecha de fin del torneo *</label>
          {errors.endDate ? (
            <div className="w-full border-2 border-red-400 bg-red-50 p-3 rounded">
              <p className="text-red-800 text-sm font-medium">
                ❌ {errors.endDate}
              </p>
            </div>
          ) : (
            <>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                required
                min={formData.startDate || new Date().toISOString().split('T')[0]}
                className="w-full border p-2 rounded border-gray-300"
              />
              {formData.startDate && (
                <p className="text-gray-500 text-xs mt-1">
                  La fecha debe ser igual o posterior a la fecha de inicio ({new Date(formData.startDate).toLocaleDateString('es-ES')})
                </p>
              )}
            </>
          )}
        </div>
      </div>

      {/* Fechas de inscripción */}
      <div className="border-t pt-4">
        <h3 className="font-semibold mb-3 text-gray-700">Fechas de Inscripción</h3>
        <p className="text-sm text-gray-600 mb-4">
          Estas fechas definen el período en el que los equipos pueden inscribirse al torneo.
          Si no las defines, se calcularán automáticamente (30 días antes del inicio del torneo).
        </p>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block font-semibold mb-1">Inicio de inscripciones</label>
            <input
              type="date"
              name="inscriptionStartDate"
              value={formData.inscriptionStartDate}
              onChange={handleChange}
              className="w-full border p-2 rounded"
            />
            {formData.startDate && !formData.inscriptionStartDate && (
              <p className="text-xs text-gray-500 mt-1">
                Se sugerirá automáticamente al guardar
              </p>
            )}
          </div>

          <div>
            <label className="block font-semibold mb-1">Cierre de inscripciones</label>
            <input
              type="date"
              name="inscriptionEndDate"
              value={formData.inscriptionEndDate}
              onChange={handleChange}
              className="w-full border p-2 rounded"
            />
            {formData.startDate && !formData.inscriptionEndDate && (
              <p className="text-xs text-gray-500 mt-1">
                Se sugerirá automáticamente al guardar
              </p>
            )}
            {formData.inscriptionEndDate && formData.startDate && 
             new Date(formData.inscriptionEndDate) >= new Date(formData.startDate) && (
              <p className="text-yellow-600 text-xs mt-1">
                ⚠️ Se ajustará automáticamente a 1 día antes del inicio
              </p>
            )}
          </div>
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