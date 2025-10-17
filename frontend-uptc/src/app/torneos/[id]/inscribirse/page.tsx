// src/app/torneos/[id]/inscribirse/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';
import inscriptionsService from '@/services/inscriptionsService';

export default function InscriptionFormPage() {
  const { id } = useParams();
  const router = useRouter();
  const [tournament, setTournament] = useState<any>(null);
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    tournamentId: Number(id),
    categoryId: 0,
    teamName: '',
    clubId: null,
    delegateName: '',
    delegateEmail: '',
    delegatePhone: ''
  });

  const [errors, setErrors] = useState<any>({});

  useEffect(() => {
    if (id) {
      fetchTournament();
    }
  }, [id]);

  const fetchTournament = async () => {
    try {
      const data = await tournamentsService.getById(id as string);
      setTournament(data);
      
      // Aquí deberías cargar las categorías disponibles
      // Por ahora simulamos
      setCategories([
        { id: 1, name: 'Categoría A' },
        { id: 2, name: 'Categoría B' }
      ]);
    } catch (error) {
      console.error('Error al cargar torneo:', error);
      alert('No se pudo cargar la información del torneo');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors: any = {};

    if (!formData.categoryId) newErrors.categoryId = 'Selecciona una categoría';
    if (!formData.teamName.trim()) newErrors.teamName = 'El nombre del equipo es requerido';
    if (!formData.delegateName.trim()) newErrors.delegateName = 'El nombre del delegado es requerido';
    
    if (!formData.delegateEmail.trim()) {
      newErrors.delegateEmail = 'El email es requerido';
    } else if (!/\S+@\S+\.\S+/.test(formData.delegateEmail)) {
      newErrors.delegateEmail = 'Email inválido';
    }

    if (!formData.delegatePhone.trim()) {
      newErrors.delegatePhone = 'El teléfono es requerido';
    } else if (!/^\d{10}$/.test(formData.delegatePhone.replace(/\s/g, ''))) {
      newErrors.delegatePhone = 'Teléfono inválido (10 dígitos)';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      alert('Por favor corrige los errores en el formulario');
      return;
    }

    setSubmitting(true);

    try {
      // Verificar disponibilidad del nombre
      const checkName = await inscriptionsService.checkTeamName(
        formData.tournamentId,
        formData.teamName
      );

      if (!checkName.isAvailable) {
        alert('El nombre del equipo ya está registrado en este torneo');
        setSubmitting(false);
        return;
      }

      // Crear inscripción
      await inscriptionsService.create(formData);
      
      alert('¡Inscripción enviada exitosamente! Recibirás una confirmación por email una vez sea aprobada.');
      router.push(`/torneos/${id}`);
    } catch (error: any) {
      console.error('Error al enviar inscripción:', error);
      alert(error.response?.data?.message || 'Error al enviar la inscripción');
    } finally {
      setSubmitting(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'categoryId' ? Number(value) : value
    }));
    
    // Limpiar error del campo
    if (errors[name]) {
      setErrors((prev: any) => ({ ...prev, [name]: undefined }));
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-900"></div>
      </div>
    );
  }

  if (!tournament) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-600 mb-4">Torneo no encontrado</h1>
          <a href="/torneos" className="text-blue-900 hover:underline">Volver a torneos</a>
        </div>
      </div>
    );
  }

  if (tournament.status !== 'REGISTRATION') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center max-w-md">
          <h1 className="text-2xl font-bold text-gray-800 mb-4">Inscripciones cerradas</h1>
          <p className="text-gray-600 mb-6">
            Este torneo no está aceptando inscripciones en este momento.
          </p>
          <a 
            href={`/torneos/${id}`}
            className="bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800 transition inline-block"
          >
            Ver torneo
          </a>
        </div>
      </div>
    );
  }

  return (
    <main className="min-h-screen py-10 bg-gray-50">
      <div className="max-w-3xl mx-auto px-4">
        <div className="bg-white rounded-lg shadow-lg p-8">
          <h1 className="text-3xl font-bold text-blue-900 mb-2">Inscripción al Torneo</h1>
          <p className="text-gray-600 mb-6">{tournament.name}</p>

          <div className="bg-blue-50 border-l-4 border-blue-900 p-4 mb-6">
            <p className="text-sm text-gray-700">
              <strong>Importante:</strong> Una vez enviada tu inscripción, será revisada por los administradores. 
              Recibirás una confirmación por email cuando sea aprobada.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Categoría */}
            <div>
              <label className="block text-gray-700 font-medium mb-2">
                Categoría *
              </label>
              <select
                name="categoryId"
                value={formData.categoryId}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.categoryId ? 'border-red-500' : 'border-gray-300'
                }`}
                required
              >
                <option value="0">Selecciona una categoría</option>
                {categories.map(cat => (
                  <option key={cat.id} value={cat.id}>
                    {cat.name}
                  </option>
                ))}
              </select>
              {errors.categoryId && (
                <p className="text-red-500 text-sm mt-1">{errors.categoryId}</p>
              )}
            </div>

            {/* Nombre del equipo */}
            <div>
              <label className="block text-gray-700 font-medium mb-2">
                Nombre del Equipo *
              </label>
              <input
                type="text"
                name="teamName"
                value={formData.teamName}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.teamName ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Ej: Los Tigres"
                required
              />
              {errors.teamName && (
                <p className="text-red-500 text-sm mt-1">{errors.teamName}</p>
              )}
            </div>

            <div className="border-t pt-6">
              <h3 className="text-lg font-semibold text-gray-800 mb-4">Datos del Delegado</h3>

              {/* Nombre del delegado */}
              <div className="mb-4">
                <label className="block text-gray-700 font-medium mb-2">
                  Nombre Completo *
                </label>
                <input
                  type="text"
                  name="delegateName"
                  value={formData.delegateName}
                  onChange={handleChange}
                  className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.delegateName ? 'border-red-500' : 'border-gray-300'
                  }`}
                  placeholder="Nombre completo del delegado"
                  required
                />
                {errors.delegateName && (
                  <p className="text-red-500 text-sm mt-1">{errors.delegateName}</p>
                )}
              </div>

              {/* Email del delegado */}
              <div className="mb-4">
                <label className="block text-gray-700 font-medium mb-2">
                  Email *
                </label>
                <input
                  type="email"
                  name="delegateEmail"
                  value={formData.delegateEmail}
                  onChange={handleChange}
                  className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.delegateEmail ? 'border-red-500' : 'border-gray-300'
                  }`}
                  placeholder="delegado@email.com"
                  required
                />
                {errors.delegateEmail && (
                  <p className="text-red-500 text-sm mt-1">{errors.delegateEmail}</p>
                )}
              </div>

              {/* Teléfono del delegado */}
              <div className="mb-4">
                <label className="block text-gray-700 font-medium mb-2">
                  Teléfono *
                </label>
                <input
                  type="tel"
                  name="delegatePhone"
                  value={formData.delegatePhone}
                  onChange={handleChange}
                  className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.delegatePhone ? 'border-red-500' : 'border-gray-300'
                  }`}
                  placeholder="3001234567"
                  required
                />
                {errors.delegatePhone && (
                  <p className="text-red-500 text-sm mt-1">{errors.delegatePhone}</p>
                )}
                <p className="text-sm text-gray-500 mt-1">Ingresa 10 dígitos sin espacios</p>
              </div>
            </div>

            {/* Términos y condiciones */}
            <div className="bg-gray-50 p-4 rounded-lg">
              <label className="flex items-start gap-3">
                <input
                  type="checkbox"
                  required
                  className="mt-1 w-4 h-4 text-blue-900 border-gray-300 rounded focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">
                  Acepto los términos y condiciones del torneo. Confirmo que la información proporcionada es 
                  verídica y me comprometo a cumplir con las reglas establecidas.
                </span>
              </label>
            </div>

            {/* Botones */}
            <div className="flex gap-4">
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 bg-blue-900 text-white py-3 rounded-lg font-semibold hover:bg-blue-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {submitting ? 'Enviando...' : 'Enviar Inscripción'}
              </button>
              <button
                type="button"
                onClick={() => router.back()}
                className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition"
              >
                Cancelar
              </button>
            </div>
          </form>
        </div>

        <div className="mt-6 text-center">
          <a href={`/torneos/${id}`} className="text-blue-900 hover:underline">
            ← Volver al torneo
          </a>
        </div>
      </div>
    </main>
  );
}