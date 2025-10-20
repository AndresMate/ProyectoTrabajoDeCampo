'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';
import inscriptionsService from '@/services/inscriptionsService';
import clubsService from '@/services/clubsService';

export default function InscripcionTorneoPage() {
  const { id } = useParams();
  const router = useRouter();
  const [tournament, setTournament] = useState<any>(null);
  const [clubs, setClubs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    teamName: '',
    clubId: 0,
    delegateName: '',
    delegateEmail: '',
    delegatePhone: ''
  });

  const [errors, setErrors] = useState<any>({});

  useEffect(() => {
    fetchData();
  }, [id]);

  const fetchData = async () => {
    try {
      // Cargar torneo y clubes en paralelo
      const [tournamentData, clubsData] = await Promise.all([
        tournamentsService.getById(id as string),
        clubsService.getActive()
      ]);

      setTournament(tournamentData);
      setClubs(clubsData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
      alert('No se pudo cargar la información del torneo');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors: any = {};

    if (!formData.teamName.trim()) {
      newErrors.teamName = 'El nombre del equipo es requerido';
    }

    if (!formData.delegateName.trim()) {
      newErrors.delegateName = 'El nombre del delegado es requerido';
    }

    if (!formData.delegateEmail.trim()) {
      newErrors.delegateEmail = 'El email es requerido';
    } else if (!/\S+@\S+\.\S+/.test(formData.delegateEmail)) {
      newErrors.delegateEmail = 'Email inválido';
    }

    if (!formData.delegatePhone.trim()) {
      newErrors.delegatePhone = 'El teléfono es requerido';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'clubId' ? Number(value) : value
    }));

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

    // Verificar disponibilidad del nombre
    const availability = await inscriptionsService.checkTeamName(
      Number(id),
      formData.teamName
    );

    if (!availability.isAvailable) {
      alert('El nombre del equipo ya está en uso. Por favor elige otro.');
      return;
    }

    setSubmitting(true);

    try {
      const inscriptionData = {
        tournamentId: Number(id),
        categoryId: tournament.category.id, // ✅ Se toma automáticamente del torneo
        clubId: formData.clubId || undefined,
        teamName: formData.teamName,
        delegateName: formData.delegateName,
        delegateEmail: formData.delegateEmail,
        delegatePhone: formData.delegatePhone
      };

      await inscriptionsService.create(inscriptionData);

      alert('✅ Inscripción enviada exitosamente. Está pendiente de aprobación.');
      router.push(`/torneos/${id}`);
    } catch (error: any) {
      console.error('Error al crear inscripción:', error);
      alert(error.response?.data?.message || 'Error al crear la inscripción');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  if (!tournament) {
    return (
      <div className="max-w-3xl mx-auto p-6">
        <div className="bg-red-50 border-l-4 border-red-500 p-4">
          <p className="text-red-700">No se pudo cargar el torneo</p>
        </div>
      </div>
    );
  }

  if (tournament.status !== 'OPEN_FOR_INSCRIPTION') {
    return (
      <div className="max-w-3xl mx-auto p-6">
        <div className="bg-yellow-50 border-l-4 border-yellow-500 p-4">
          <p className="text-yellow-700">
            Este torneo no está aceptando inscripciones en este momento.
          </p>
          <button
            onClick={() => router.push(`/torneos/${id}`)}
            className="mt-4 text-blue-600 hover:underline"
          >
            ← Volver al torneo
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-uptc-black mb-2">
          Inscribir Equipo
        </h1>
        <p className="text-gray-600">
          Torneo: <strong>{tournament.name}</strong>
        </p>
        <p className="text-gray-600">
          Deporte: <strong>{tournament.sport?.name}</strong> • Categoría: <strong>{tournament.category?.name}</strong>
        </p>
        <p className="text-sm text-gray-500 mt-2">
          Jugadores por equipo: <strong>{tournament.category?.membersPerTeam}</strong>
        </p>
      </div>

      {/* Formulario */}
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-lg p-8 space-y-6">
        {/* Nombre del equipo */}
        <div>
          <label className="block text-gray-700 font-semibold mb-2">
            Nombre del Equipo *
          </label>
          <input
            type="text"
            name="teamName"
            value={formData.teamName}
            onChange={handleChange}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
              errors.teamName ? 'border-red-500' : 'border-gray-300'
            }`}
            placeholder="Ej: Los Águilas"
            required
          />
          {errors.teamName && (
            <p className="text-red-500 text-sm mt-1">{errors.teamName}</p>
          )}
        </div>

        {/* Club (opcional) */}
        <div>
          <label className="block text-gray-700 font-semibold mb-2">
            Club (Opcional)
          </label>
          <select
            name="clubId"
            value={formData.clubId}
            onChange={handleChange}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow"
          >
            <option value="0">Sin club</option>
            {clubs.map(club => (
              <option key={club.id} value={club.id}>
                {club.name}
              </option>
            ))}
          </select>
        </div>

        {/* Datos del delegado */}
        <div className="border-t pt-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">
            Datos del Delegado
          </h3>

          <div className="space-y-4">
            <div>
              <label className="block text-gray-700 font-semibold mb-2">
                Nombre Completo *
              </label>
              <input
                type="text"
                name="delegateName"
                value={formData.delegateName}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
                  errors.delegateName ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Juan Pérez"
                required
              />
              {errors.delegateName && (
                <p className="text-red-500 text-sm mt-1">{errors.delegateName}</p>
              )}
            </div>

            <div>
              <label className="block text-gray-700 font-semibold mb-2">
                Correo Electrónico *
              </label>
              <input
                type="email"
                name="delegateEmail"
                value={formData.delegateEmail}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
                  errors.delegateEmail ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="delegado@uptc.edu.co"
                required
              />
              {errors.delegateEmail && (
                <p className="text-red-500 text-sm mt-1">{errors.delegateEmail}</p>
              )}
            </div>

            <div>
              <label className="block text-gray-700 font-semibold mb-2">
                Teléfono *
              </label>
              <input
                type="tel"
                name="delegatePhone"
                value={formData.delegatePhone}
                onChange={handleChange}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${
                  errors.delegatePhone ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="3001234567"
                required
              />
              {errors.delegatePhone && (
                <p className="text-red-500 text-sm mt-1">{errors.delegatePhone}</p>
              )}
            </div>
          </div>
        </div>

        {/* Información importante */}
        <div className="bg-blue-50 border-l-4 border-uptc-yellow p-4">
          <h4 className="font-semibold text-uptc-black mb-2">📋 Información Importante:</h4>
          <ul className="text-sm text-uptc-black space-y-1">
            <li>• Tu inscripción quedará pendiente de aprobación</li>
            <li>• Deberás agregar {tournament.category?.membersPerTeam} jugadores</li>
            <li>• El delegado será el responsable del equipo</li>
            <li>• Revisa bien los datos antes de enviar</li>
          </ul>
        </div>

        {/* Botones */}
        <div className="flex gap-4 pt-4">
          <button
            type="submit"
            disabled={submitting}
            className="flex-1 bg-uptc-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition disabled:opacity-50"
          >
            {submitting ? 'Enviando...' : '✅ Inscribir Equipo'}
          </button>
          <button
            type="button"
            onClick={() => router.push(`/torneos/${id}`)}
            className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
}