'use client';

import { useState, useEffect } from 'react';
import sportsService, { SportCreateDTO } from '@/services/sportsService';
import { toastWarning, toastPromise } from '@/utils/toast';

interface SportFormProps {
  sportId?: number;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function SportForm({ sportId, onSuccess, onCancel }: SportFormProps) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (sportId) {
      fetchSport();
    }
  }, [sportId]);

  const fetchSport = async () => {
    try {
      const sport = await sportsService.getById(sportId!);
      setFormData({
        name: sport.name || '',
        description: sport.description || ''
      });
    } catch (error) {
      console.error('Error al cargar deporte:', error);
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'El nombre es requerido';
    } else if (formData.name.trim().length < 3) {
      newErrors.name = 'El nombre debe tener al menos 3 caracteres';
    }

    if (formData.description && formData.description.length > 500) {
      newErrors.description = 'La descripción no puede exceder 500 caracteres';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      toastWarning('Por favor corrige los errores en el formulario');
      return;
    }

    setLoading(true);

    try {
      const dataToSend: SportCreateDTO = {
        name: formData.name.trim(),
        description: formData.description.trim() || undefined,
        isActive: true // Los deportes nuevos siempre se crean activos
      };

      if (sportId) {
        await toastPromise(
          sportsService.update(sportId, dataToSend),
          {
            loading: 'Actualizando deporte...',
            success: '✅ Deporte actualizado correctamente',
            error: (error: any) => error.response?.data?.message || 'Error al actualizar el deporte'
          }
        );
      } else {
        await toastPromise(
          sportsService.create(dataToSend),
          {
            loading: 'Creando deporte...',
            success: '✅ Deporte creado correctamente',
            error: (error: any) => error.response?.data?.message || 'Error al crear el deporte'
          }
        );
      }

      onSuccess();
    } catch (error: any) {
      console.error('Error al guardar deporte:', error);
      // El error ya se muestra en el toastPromise
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Nombre */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">
          Nombre del Deporte *
        </label>
        <input
          type="text"
          name="name"
          value={formData.name}
          onChange={handleChange}
          className={`w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow ${
            errors.name ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="Ej: Fútbol, Baloncesto, Voleibol..."
          required
        />
        {errors.name && (
          <p className="mt-1 text-sm text-red-600">{errors.name}</p>
        )}
      </div>

      {/* Descripción */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">
          Descripción
        </label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleChange}
          rows={4}
          maxLength={500}
          className={`w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow ${
            errors.description ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="Descripción del deporte..."
        />
        {errors.description && (
          <p className="mt-1 text-sm text-red-600">{errors.description}</p>
        )}
        <p className="mt-1 text-xs text-gray-500">
          {formData.description.length}/500 caracteres
        </p>
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
          {loading ? 'Guardando...' : sportId ? 'Actualizar Deporte' : 'Crear Deporte'}
        </button>
      </div>
    </form>
  );
}

