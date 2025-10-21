'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';
import inscriptionsService from '@/services/inscriptionsService';
import clubsService from '@/services/clubsService';
import fileUploadService from '@/services/fileUploadService'; // ✅ Importar el servicio

type Player = {
  fullName: string;
  documentNumber: string;
  studentCode: string;
  institutionalEmail: string;
  idCardPhotoUrl: string;
};

export default function InscripcionTorneoPage() {
  const { id } = useParams();
  const router = useRouter();
  const [tournament, setTournament] = useState<any>(null);
  const [clubs, setClubs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [step, setStep] = useState(1);
  const [players, setPlayers] = useState<Player[]>([]);
  const [delegateIndex, setDelegateIndex] = useState<number>(0);
  const [uploadingFiles, setUploadingFiles] = useState<{ [key: number]: boolean }>({}); // ✅ Estado para tracking de uploads

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
      const [tournamentData, clubsData] = await Promise.all([
        tournamentsService.getById(id as string),
        clubsService.getActive()
      ]);

      setTournament(tournamentData);
      setClubs(clubsData);

      const membersCount = tournamentData.category?.membersPerTeam || 1;
      setPlayers(Array.from({ length: membersCount }, () => ({
        fullName: '',
        documentNumber: '',
        studentCode: '',
        institutionalEmail: '',
        idCardPhotoUrl: ''
      })));
    } catch (error) {
      console.error('Error al cargar datos:', error);
      alert('No se pudo cargar la información del torneo');
    } finally {
      setLoading(false);
    }
  };

  const validateStep1 = () => {
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
    } else if (!/^\d{10}$/.test(formData.delegatePhone)) {
      newErrors.delegatePhone = 'Teléfono debe tener 10 dígitos';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateStep2 = () => {
    for (let i = 0; i < players.length; i++) {
      const player = players[i];
      if (!player.fullName.trim()) {
        alert(`Falta el nombre completo del jugador ${i + 1}`);
        return false;
      }
      if (!player.documentNumber.trim()) {
        alert(`Falta el documento del jugador ${i + 1}`);
        return false;
      }
      if (!player.studentCode.trim()) {
        alert(`Falta el código estudiantil del jugador ${i + 1}`);
        return false;
      }
      if (!player.institutionalEmail.trim() || !/\S+@\S+\.\S+/.test(player.institutionalEmail)) {
        alert(`Email inválido para el jugador ${i + 1}`);
        return false;
      }
      if (!player.idCardPhotoUrl) {
        alert(`Falta la foto del carnet del jugador ${i + 1}`);
        return false;
      }
    }
    return true;
  };

  const handleNextStep = async () => {
    if (!validateStep1()) {
      alert('Por favor corrige los errores en el formulario');
      return;
    }

    const availability = await inscriptionsService.checkTeamName(
      Number(id),
      formData.teamName
    );

    if (!availability.isAvailable) {
      alert('El nombre del equipo ya está en uso. Por favor elige otro.');
      return;
    }

    setStep(2);
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

  const handlePlayerChange = (index: number, field: keyof Player, value: string) => {
    setPlayers(prev => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
  };

  // ✅ Función mejorada usando el servicio
  const handleFileUpload = async (index: number, file: File | null) => {
    if (!file) return;

    const validTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    if (!validTypes.includes(file.type)) {
      alert('Solo se permiten archivos JPG, JPEG o PNG');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert('El archivo no debe superar 5MB');
      return;
    }

    // ✅ Activar indicador de carga
    setUploadingFiles(prev => ({ ...prev, [index]: true }));

    try {
      // ✅ Usar el servicio de upload
      const fileUrl = await fileUploadService.uploadIdCard(file);

      // ✅ Actualizar el estado del jugador con la URL
      handlePlayerChange(index, 'idCardPhotoUrl', fileUrl);

      alert('✅ Foto cargada exitosamente');
    } catch (error: any) {
      console.error('Error al subir archivo:', error);
      alert(error.response?.data?.message || '❌ Error al cargar la foto. Por favor intenta de nuevo.');
    } finally {
      // ✅ Desactivar indicador de carga
      setUploadingFiles(prev => ({ ...prev, [index]: false }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateStep2()) {
      return;
    }

    setSubmitting(true);

    try {
      const inscriptionData = {
        tournamentId: Number(id),
        categoryId: tournament.category.id,
        clubId: formData.clubId || undefined,
        teamName: formData.teamName,
        delegateName: formData.delegateName,
        delegateEmail: formData.delegateEmail,
        delegatePhone: formData.delegatePhone,
        delegateIndex,
        players
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
    <div className="max-w-4xl mx-auto p-6">
      {/* Header con indicador de paso */}
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

        {/* Indicador de pasos */}
        <div className="flex items-center gap-4 mt-6">
          <div className={`flex items-center gap-2 ${step === 1 ? 'text-uptc-yellow' : 'text-gray-400'}`}>
            <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold ${
              step === 1 ? 'bg-uptc-yellow text-uptc-black' : 'bg-gray-300'
            }`}>
              1
            </div>
            <span className="font-semibold">Datos del Equipo</span>
          </div>
          <div className="flex-1 h-1 bg-gray-300"></div>
          <div className={`flex items-center gap-2 ${step === 2 ? 'text-uptc-yellow' : 'text-gray-400'}`}>
            <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold ${
              step === 2 ? 'bg-uptc-yellow text-uptc-black' : 'bg-gray-300'
            }`}>
              2
            </div>
            <span className="font-semibold">Jugadores</span>
          </div>
        </div>
      </div>

      {/* Formulario */}
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-lg p-8 space-y-6">
        {/* PASO 1: Datos del equipo y delegado */}
        {step === 1 && (
          <>
            <div className="space-y-6">
              <h3 className="text-xl font-bold text-gray-800 border-b pb-2">
                📋 Información del Equipo
              </h3>

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
                />
                {errors.teamName && (
                  <p className="text-red-500 text-sm mt-1">{errors.teamName}</p>
                )}
              </div>

              {/* Club */}
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
            </div>

            {/* Datos del delegado */}
            <div className="border-t pt-6 space-y-4">
              <h3 className="text-xl font-bold text-gray-800 border-b pb-2">
                👤 Datos del Delegado
              </h3>

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
                />
                {errors.delegatePhone && (
                  <p className="text-red-500 text-sm mt-1">{errors.delegatePhone}</p>
                )}
              </div>
            </div>

            {/* Información importante */}
            <div className="bg-blue-50 border-l-4 border-uptc-yellow p-4">
              <h4 className="font-semibold text-uptc-black mb-2">📋 Información Importante:</h4>
              <ul className="text-sm text-uptc-black space-y-1">
                <li>• Tu inscripción quedará pendiente de aprobación</li>
                <li>• Deberás agregar {tournament.category?.membersPerTeam} jugadores</li>
                <li>• El delegado será el responsable del equipo</li>
                <li>• Cada jugador debe cargar su carnet estudiantil</li>
              </ul>
            </div>

            {/* Botones paso 1 */}
            <div className="flex gap-4 pt-4">
              <button
                type="button"
                onClick={handleNextStep}
                className="flex-1 bg-uptc-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition"
              >
                Continuar →
              </button>
              <button
                type="button"
                onClick={() => router.push(`/torneos/${id}`)}
                className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition"
              >
                Cancelar
              </button>
            </div>
          </>
        )}

        {/* PASO 2: Jugadores */}
        {step === 2 && (
          <>
            <div className="space-y-6">
              <h3 className="text-xl font-bold text-gray-800 border-b pb-2">
                👥 Jugadores ({tournament.category.membersPerTeam} requeridos)
              </h3>

              {players.map((player, index) => (
                <div key={index} className="border rounded-lg p-6 bg-gray-50 space-y-4">
                  <div className="flex justify-between items-center mb-3">
                    <h4 className="font-bold text-lg">Jugador {index + 1}</h4>
                    <label className="flex items-center gap-2 cursor-pointer">
                      <input
                        type="radio"
                        name="delegate"
                        checked={delegateIndex === index}
                        onChange={() => setDelegateIndex(index)}
                        className="w-4 h-4"
                      />
                      <span className="text-sm font-semibold text-uptc-black">
                        Marcar como Delegado
                      </span>
                    </label>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-semibold mb-1">Nombre Completo *</label>
                      <input
                        type="text"
                        value={player.fullName}
                        onChange={(e) => handlePlayerChange(index, 'fullName', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow"
                        placeholder="Juan Pérez López"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-semibold mb-1">Documento *</label>
                      <input
                        type="text"
                        value={player.documentNumber}
                        onChange={(e) => handlePlayerChange(index, 'documentNumber', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow"
                        placeholder="1234567890"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-semibold mb-1">Código Estudiantil *</label>
                      <input
                        type="text"
                        value={player.studentCode}
                        onChange={(e) => handlePlayerChange(index, 'studentCode', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow"
                        placeholder="202012345"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-semibold mb-1">Email Institucional *</label>
                      <input
                        type="email"
                        value={player.institutionalEmail}
                        onChange={(e) => handlePlayerChange(index, 'institutionalEmail', e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow"
                        placeholder="jugador@uptc.edu.co"
                        required
                      />
                    </div>
                  </div>

                  {/* ✅ Input de archivo mejorado con indicador de carga */}
                  <div>
                    <label className="block text-sm font-semibold mb-2">
                      📷 Foto del Carnet Estudiantil *
                    </label>
                    <input
                      type="file"
                      accept="image/jpeg,image/jpg,image/png"
                      onChange={(e) => handleFileUpload(index, e.target.files?.[0] || null)}
                      disabled={uploadingFiles[index]} // ✅ Deshabilitar durante la carga
                      className="w-full px-3 py-2 border rounded-lg bg-white disabled:opacity-50 disabled:cursor-not-allowed"
                    />

                    {/* ✅ Indicador de carga */}
                    {uploadingFiles[index] && (
                      <p className="text-blue-600 text-sm mt-2 flex items-center gap-2">
                        <span className="animate-spin">⏳</span> Subiendo archivo...
                      </p>
                    )}

                    {/* ✅ Confirmación de carga exitosa */}
                    {player.idCardPhotoUrl && !uploadingFiles[index] && (
                      <p className="text-green-600 text-sm mt-2 flex items-center gap-1">
                        ✅ Foto cargada exitosamente
                      </p>
                    )}

                    <p className="text-xs text-gray-500 mt-1">
                      Formatos: JPG, JPEG, PNG • Máximo: 5MB
                    </p>
                  </div>
                </div>
              ))}
            </div>

            {/* Información de validación */}
            <div className="bg-yellow-50 border-l-4 border-yellow-500 p-4">
              <h4 className="font-semibold text-yellow-800 mb-2">⚠️ Antes de enviar:</h4>
              <ul className="text-sm text-yellow-800 space-y-1">
                <li>• Verifica que todos los datos sean correctos</li>
                <li>• Asegúrate de haber cargado todas las fotos de carnets</li>
                <li>• Un jugador será el delegado del equipo</li>
                <li>• La inscripción quedará pendiente de aprobación</li>
              </ul>
            </div>

            {/* Botones paso 2 */}
            <div className="flex gap-4 pt-4">
              <button
                type="button"
                onClick={() => setStep(1)}
                className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition"
              >
                ← Volver
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 bg-uptc-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition disabled:opacity-50"
              >
                {submitting ? 'Enviando...' : '✅ Inscribir Equipo'}
              </button>
            </div>
          </>
        )}
      </form>
    </div>
  );
}