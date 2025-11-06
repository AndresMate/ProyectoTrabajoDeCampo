'use client';

import { useState, useEffect } from 'react';
import categoriesService, { CategoryCreateDTO } from '@/services/categoriesService';
import { toastWarning, toastPromise } from '@/utils/toast';

interface CategoryFormProps {
  categoryId?: number;
  sports: any[];
  onSuccess: () => void;
  onCancel: () => void;
}

export default function CategoryForm({ categoryId, sports, onSuccess, onCancel }: CategoryFormProps) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    sportId: 0,
    membersPerTeam: 0
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (categoryId) {
      fetchCategory();
    }
  }, [categoryId]);

  const fetchCategory = async () => {
    try {
      const category = await categoriesService.getById(categoryId!);
      setFormData({
        name: category.name || '',
        description: category.description || '',
        sportId: category.sportId || 0,
        membersPerTeam: category.membersPerTeam || 0
      });
    } catch (error) {
      console.error('Error al cargar categoría:', error);
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'El nombre es requerido';
    } else if (formData.name.trim().length < 2) {
      newErrors.name = 'El nombre debe tener al menos 2 caracteres';
    }

    if (!formData.sportId || formData.sportId === 0) {
      newErrors.sportId = 'Debes seleccionar un deporte';
    }

    if (!formData.membersPerTeam || formData.membersPerTeam <= 0) {
      newErrors.membersPerTeam = 'El número de miembros por equipo debe ser mayor a 0';
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
      const dataToSend: CategoryCreateDTO = {
        name: formData.name.trim(),
        description: formData.description.trim() || undefined,
        sportId: formData.sportId,
        membersPerTeam: formData.membersPerTeam
      };

      if (categoryId) {
        await toastPromise(
          categoriesService.update(categoryId, dataToSend),
          {
            loading: 'Actualizando categoría...',
            success: '✅ Categoría actualizada correctamente',
            error: (error: any) => error.response?.data?.message || 'Error al actualizar la categoría'
          }
        );
      } else {
        await toastPromise(
          categoriesService.create(dataToSend),
          {
            loading: 'Creando categoría...',
            success: '✅ Categoría creada correctamente',
            error: (error: any) => error.response?.data?.message || 'Error al crear la categoría'
          }
        );
      }

      onSuccess();
    } catch (error: any) {
      console.error('Error al guardar categoría:', error);
      // El error ya se muestra en el toastPromise
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'sportId' || name === 'membersPerTeam'
        ? (value === '' ? 0 : Number(value))
        : value
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Deporte */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">
          Deporte *
        </label>
        <select
          name="sportId"
          value={formData.sportId}
          onChange={handleChange}
          className={`w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow ${
            errors.sportId ? 'border-red-500' : 'border-gray-300'
          }`}
          required
        >
          <option value="0">-- Selecciona un deporte --</option>
          {sports.map(sport => (
            <option key={sport.id} value={sport.id}>
              {sport.name}
            </option>
          ))}
        </select>
        {errors.sportId && (
          <p className="mt-1 text-sm text-red-600">{errors.sportId}</p>
        )}
      </div>

      {/* Nombre */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">
          Nombre de la Categoría *
        </label>
        <input
          type="text"
          name="name"
          value={formData.name}
          onChange={handleChange}
          className={`w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow ${
            errors.name ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="Ej: Sub-15, Primera División, Femenino..."
          required
        />
        {errors.name && (
          <p className="mt-1 text-sm text-red-600">{errors.name}</p>
        )}
      </div>

      {/* Miembros por equipo */}
      <div>
        <label className="block text-gray-800 font-semibold mb-2">
          Miembros por Equipo *
        </label>
        <input
          type="number"
          name="membersPerTeam"
          value={formData.membersPerTeam || ''}
          onChange={handleChange}
          min="1"
          className={`w-full px-4 py-3 border-2 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow ${
            errors.membersPerTeam ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="Ej: 11, 5, 6..."
          required
        />
        {errors.membersPerTeam && (
          <p className="mt-1 text-sm text-red-600">{errors.membersPerTeam}</p>
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
          placeholder="Descripción de la categoría..."
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
          {loading ? 'Guardando...' : categoryId ? 'Actualizar Categoría' : 'Crear Categoría'}
        </button>
      </div>
    </form>
  );
}

