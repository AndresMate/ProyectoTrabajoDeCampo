'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';
import inscriptionsService from '@/services/inscriptionsService';
import clubsService from '@/services/clubsService';

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
  const [uploadingFiles, setUploadingFiles] = useState<{ [key: number]: boolean }>({});

  const [formData, setFormData] = useState({
    teamName: '',
    clubId: 0,
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

      // ✅ Inicializar jugadores según el tamaño del equipo
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

    if (!formData.delegatePhone.trim()) {
      newErrors.delegatePhone = 'El teléfono del delegado es requerido';
    } else if (!/^\d{10}$/.test(formData.delegatePhone)) {
      newErrors.delegatePhone = 'Teléfono debe tener 10 dígitos';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateStep2 = () => {
    // Verificar si hay archivos en proceso de subida
    const hasUploading = Object.values(uploadingFiles).some(status => status);
    if (hasUploading) {
      alert('⏳ Hay fotos que aún se están subiendo. Por favor espera.');
      return false;
    }

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

  const handleFileUpload = async (index: number, file: File | null) => {
    if (!file) {
      console.log('No hay archivo seleccionado');
      return;
    }

    console.log('📤 Archivo seleccionado:', file.name, file.type, file.size);

    const validTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    if (!validTypes.includes(file.type)) {
      alert('Solo se permiten archivos JPG, JPEG o PNG');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert('El archivo no debe superar 5MB');
      return;
    }

    setUploadingFiles(prev => ({ ...prev, [index]: true }));

    try {
      const formData = new FormData();
      formData.append('file', file);

      console.log('⬆️  Enviando archivo al servidor...');

      const response = await fetch('http://localhost:8080/api/files/upload/id-card', {
        method: 'POST',
        body: formData
      });

      console.log('📥 Respuesta del servidor:', response.status, response.statusText);

      if (!response.ok) {
        const errorData = await response.json();
        console.error('❌ Error del servidor:', errorData);
        throw new Error(errorData.error || 'Error al subir archivo');
      }

      const json = await response.json();
      console.log('✅ JSON completo de respuesta:', json);

      const fileUrl = json.url as string | undefined;

      if (fileUrl) {
        handlePlayerChange(index, 'idCardPhotoUrl', fileUrl);
        alert(`✅ Foto del jugador ${index + 1} cargada exitosamente`);
        console.log(`🔗 URL guardada para jugador ${index + 1}:`, fileUrl);
      } else {
        throw new Error('Respuesta de upload sin URL');
      }
    } catch (error) {
      console.error('❌ Error completo al subir:', error);
      alert(`❌ Error al cargar la foto del jugador ${index + 1}: ${error instanceof Error ? error.message : 'Error desconocido'}`);
    } finally {
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
        delegatePhone: formData.delegatePhone,
        delegateIndex, // ✅ Índice del jugador que será delegado
        players
      };

      console.log('📤 Enviando inscripción:', inscriptionData);

      await inscriptionsService.create(inscriptionData);

      alert('✅ ¡Inscripción enviada exitosamente!\n\nTu solicitud está pendiente de aprobación.');
      router.push(`/torneos/${id}`);
    } catch (error: any) {
      console.error('Error al enviar inscripción:', error);
      alert(error.response?.data?.message || '❌ Error al procesar la inscripción. Por favor intenta de nuevo.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow mx-auto mb-4"></div>
          <p className="text-gray-600">Cargando...</p>
        </div>
      </div>
    );
  }

  if (!tournament) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600">No se pudo cargar la información del torneo</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h1 className="text-3xl font-bold text-uptc-black mb-2">
            📝 Inscripción a Torneo
          </h1>
          <div className="text-gray-600">
            <p className="font-semibold">Torneo: <span className="text-uptc-black">{tournament.name}</span></p>
            <p>Deporte: <span className="font-medium">{tournament.sport?.name}</span> • Categoría: <span className="font-medium">{tournament.category?.name}</span></p>
            <p className="text-sm mt-1">Jugadores requeridos: <span className="font-bold text-uptc-yellow">{tournament.category?.membersPerTeam}</span></p>
          </div>
        </div>

        {/* Indicador de pasos */}
        <div className="bg-white rounded-lg shadow-md p-4 mb-6">
          <div className="flex items-center justify-center space-x-4">
            <div className={`flex items-center ${step === 1 ? 'text-uptc-yellow' : 'text-gray-400'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step === 1 ? 'bg-uptc-yellow text-white' : 'bg-gray-200'}`}>
                1
              </div>
              <span className="ml-2 font-semibold">Datos del Equipo</span>
            </div>
            <div className="w-16 h-1 bg-gray-300"></div>
            <div className={`flex items-center ${step === 2 ? 'text-uptc-yellow' : 'text-gray-400'}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step === 2 ? 'bg-uptc-yellow text-white' : 'bg-gray-200'}`}>
                2
              </div>
              <span className="ml-2 font-semibold">Jugadores</span>
            </div>
          </div>
        </div>

        {/* Formulario */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* PASO 1: Datos del Equipo */}
            {step === 1 && (
              <>
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
                    placeholder="Ej: Águilas FC"
                  />
                  {errors.teamName && (
                    <p className="text-red-500 text-sm mt-1">{errors.teamName}</p>
                  )}
                </div>

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
                    <option value={0}>Sin club</option>
                    {clubs.map(club => (
                      <option key={club.id} value={club.id}>
                        {club.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-gray-700 font-semibold mb-2">
                    Teléfono del Delegado *
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
                    maxLength={10}
                  />
                  {errors.delegatePhone && (
                    <p className="text-red-500 text-sm mt-1">{errors.delegatePhone}</p>
                  )}
                  <p className="text-xs text-gray-500 mt-1">
                    El teléfono del jugador que será el delegado del equipo
                  </p>
                </div>

                {/* Información importante */}
                <div className="bg-blue-50 border-l-4 border-uptc-yellow p-4">
                  <h4 className="font-semibold text-uptc-black mb-2">📋 Información Importante:</h4>
                  <ul className="text-sm text-uptc-black space-y-1">
                    <li>• Tu inscripción quedará pendiente de aprobación</li>
                    <li>• Deberás registrar exactamente {tournament.category?.membersPerTeam} jugadores</li>
                    <li>• Uno de los jugadores será designado como delegado</li>
                    <li>• Cada jugador debe cargar su carnet estudiantil (JPG, PNG máx 5MB)</li>
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
                  <h3 className="text-xl font-bold text-uptc-black border-b pb-2">
                    👥 Jugadores ({tournament.category.membersPerTeam} requeridos)
                  </h3>

                  {players.map((player, index) => (
                    <div key={index} className="border rounded-lg p-6 bg-gray-50 space-y-4">
                      <div className="flex justify-between items-center mb-3">
                        <h4 className="font-bold text-lg text-uptc-black">Jugador {index + 1}</h4>
                        <label className="flex items-center gap-2 cursor-pointer bg-white px-4 py-2 rounded-lg border-2 border-uptc-yellow hover:bg-uptc-yellow hover:text-white transition">
                          <input
                            type="radio"
                            name="delegate"
                            checked={delegateIndex === index}
                            onChange={() => setDelegateIndex(index)}
                            className="w-4 h-4"
                          />
                          <span className="text-sm font-semibold">
                            {delegateIndex === index ? '✓ Delegado' : 'Marcar como Delegado'}
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

                      {/* Input de archivo */}
                      <div>
                        <label className="block text-sm font-semibold mb-2">
                          📷 Foto del Carnet Estudiantil *
                        </label>
                        <input
                          type="file"
                          accept="image/jpeg,image/jpg,image/png"
                          onChange={(e) => handleFileUpload(index, e.target.files?.[0] || null)}
                          disabled={uploadingFiles[index]}
                          className="w-full px-3 py-2 border rounded-lg bg-white disabled:opacity-50 disabled:cursor-not-allowed"
                        />

                        {/* Indicador de carga */}
                        {uploadingFiles[index] && (
                          <div className="mt-2 flex items-center gap-2">
                            <div className="animate-spin h-4 w-4 border-2 border-blue-500 border-t-transparent rounded-full"></div>
                            <p className="text-blue-600 text-sm">⏳ Subiendo foto...</p>
                          </div>
                        )}

                        {/* Confirmación de carga exitosa */}
                        {player.idCardPhotoUrl && !uploadingFiles[index] && (
                          <div className="mt-2 p-2 bg-green-50 border border-green-200 rounded">
                            <p className="text-green-700 font-semibold text-sm">✅ Foto cargada correctamente</p>
                            <a
                              href={player.idCardPhotoUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="text-xs text-blue-600 hover:underline break-all"
                            >
                              Ver foto subida
                            </a>
                          </div>
                        )}

                        {!player.idCardPhotoUrl && !uploadingFiles[index] && (
                          <p className="text-xs text-gray-500 mt-1">
                            Formatos: JPG, JPEG, PNG • Máximo: 5MB
                          </p>
                        )}
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
                    <li>• Has marcado a <strong className="text-uptc-black">{players[delegateIndex]?.fullName || `Jugador ${delegateIndex + 1}`}</strong> como delegado</li>
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
                    disabled={submitting || Object.values(uploadingFiles).some(status => status)}
                    className="flex-1 bg-uptc-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {submitting ? 'Enviando...' : '✅ Inscribir Equipo'}
                  </button>
                </div>
              </>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}