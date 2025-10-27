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

  // Disponibilidad: internal representation is a map day -> Set(startTime)
  // days used in payload will be English uppercase: MONDAY..FRIDAY
  const DAYS_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'] as const;
  const DAY_LABEL: Record<string, string> = {
    MONDAY: 'Lunes',
    TUESDAY: 'Martes',
    WEDNESDAY: 'Miércoles',
    THURSDAY: 'Jueves',
    FRIDAY: 'Viernes'
  };

  const DIURNO_SLOTS = [
    { start: '11:00', end: '12:00' },
    { start: '12:00', end: '13:00' },
    { start: '13:00', end: '14:00' },
    { start: '14:00', end: '15:00' },
    { start: '15:00', end: '16:00' }
  ];

  const NOCTURNO_SLOTS = [
    { start: '17:00', end: '18:00' },
    { start: '18:00', end: '19:00' },
    { start: '19:00', end: '20:00' },
    { start: '20:00', end: '21:00' }
  ];

  // internal selection state
  const [selection, setSelection] = useState<Record<string, Set<string>>>({
    MONDAY: new Set(),
    TUESDAY: new Set(),
    WEDNESDAY: new Set(),
    THURSDAY: new Set(),
    FRIDAY: new Set()
  });

  // computed availability for payload (list of objects)
  const computeAvailabilityPayload = (): { dayOfWeek: string; startTime: string; endTime: string }[] => {
    const slots = tournament?.modality === 'DIURNO' ? DIURNO_SLOTS : NOCTURNO_SLOTS;
    const result: { dayOfWeek: string; startTime: string; endTime: string }[] = [];

    for (const day of DAYS_ORDER) {
      const starts = Array.from(selection[day] || []);
      for (const start of starts) {
        const slot = slots.find(s => s.start === start);
        if (slot) {
          result.push({ dayOfWeek: day, startTime: slot.start, endTime: slot.end });
        }
      }
    }
    return result;
  };

  useEffect(() => {
    fetchData();
    // reset selection when tournament changes
    return () => {};
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const fetchData = async () => {
    try {
      const [tournamentData, clubsData] = await Promise.all([
        tournamentsService.getById(id as string),
        clubsService.getActive()
      ]);

      setTournament(tournamentData);
      setClubs(clubsData);

      // Inicializar jugadores según el tamaño del equipo
      const membersCount = tournamentData.category?.membersPerTeam || 1;
      setPlayers(Array.from({ length: membersCount }, () => ({
        fullName: '',
        documentNumber: '',
        studentCode: '',
        institutionalEmail: '',
        idCardPhotoUrl: ''
      })));

      // inicializar selection vacío (por si viene otro torneo)
      setSelection({
        MONDAY: new Set(),
        TUESDAY: new Set(),
        WEDNESDAY: new Set(),
        THURSDAY: new Set(),
        FRIDAY: new Set()
      });

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
    } else if (!/^\d{7,15}$/.test(formData.delegatePhone)) {
      // acepta entre 7 y 15 dígitos (ajusta a tu regla si necesitas 10 exactos)
      newErrors.delegatePhone = 'Teléfono inválido';
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

  const validateStep3 = () => {
    // mínimo una franja por día: verificar cada día tiene al menos un elemento en selection
    for (const day of DAYS_ORDER) {
      if (!selection[day] || selection[day].size === 0) {
        alert(`Debes seleccionar al menos una franja horaria para ${DAY_LABEL[day]}`);
        return false;
      }
    }
    return true;
  };

  const handleNextStep = async () => {
    if (step === 1) {
      if (!validateStep1()) {
        alert('Por favor corrige los errores en el formulario');
        return;
      }

      // Chequear nombre de equipo disponible
      try {
        const availability = await inscriptionsService.checkTeamName(Number(id), formData.teamName);
        if (!availability.isAvailable) {
          alert('El nombre del equipo ya está en uso. Por favor elige otro.');
          return;
        }
      } catch (err) {
        console.error('Error al chequear nombre:', err);
        alert('No se pudo verificar el nombre del equipo. Intenta nuevamente.');
        return;
      }

      setStep(2);
      return;
    }

    if (step === 2) {
      if (!validateStep2()) return;
      setStep(3);
      return;
    }
  };

  const handleBack = () => {
    if (step > 1) setStep(step - 1);
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
      const form = new FormData();
      form.append('file', file);

      const response = await fetch('http://localhost:8080/api/files/upload/id-card', {
        method: 'POST',
        body: form
      });

      if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.message || 'Error al subir archivo');
      }

      const json = await response.json();
      const fileUrl = json.url as string | undefined;

      if (fileUrl) {
        handlePlayerChange(index, 'idCardPhotoUrl', fileUrl);
      } else {
        throw new Error('Respuesta sin URL');
      }
    } catch (error) {
      console.error('❌ Error completo al subir:', error);
      alert(`❌ Error al cargar la foto del jugador ${index + 1}: ${error instanceof Error ? error.message : 'Error desconocido'}`);
    } finally {
      setUploadingFiles(prev => ({ ...prev, [index]: false }));
    }
  };

  const toggleSlot = (day: string, start: string) => {
    setSelection(prev => {
      const updated = { ...prev };
      const setForDay = new Set(updated[day] || []);
      if (setForDay.has(start)) {
        setForDay.delete(start);
      } else {
        setForDay.add(start);
      }
      updated[day] = setForDay;
      return updated;
    });
  };

  const isSlotSelected = (day: string, start: string) => {
    return selection[day]?.has(start) ?? false;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // validate previous steps again
    if (step !== 3) {
      alert('Debes completar todos los pasos antes de enviar');
      return;
    }

    if (!validateStep3()) return;
    if (!validateStep2()) return; // ensure players still valid

    setSubmitting(true);

    try {
      const inscriptionData = {
        tournamentId: Number(id),
        categoryId: tournament.category.id,
        clubId: formData.clubId || undefined,
        teamName: formData.teamName,
        delegatePhone: formData.delegatePhone,
        delegateIndex,
        players,
        availability: computeAvailabilityPayload()
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

  const slotsToShow = tournament.modality === 'DIURNO' ? DIURNO_SLOTS : NOCTURNO_SLOTS;

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
            <p className="text-sm mt-1">Modalidad: <span className="font-bold">{tournament.modality}</span></p>
          </div>
        </div>

        {/* Indicador de pasos */}
        <div className="bg-white rounded-lg shadow-md p-4 mb-6">
          <div className="flex items-center justify-center space-x-4">
            <StepIndicator step={step} index={1} label="Datos del Equipo" />
            <div className="w-16 h-1 bg-gray-300"></div>
            <StepIndicator step={step} index={2} label="Jugadores" />
            <div className="w-16 h-1 bg-gray-300"></div>
            <StepIndicator step={step} index={3} label="Disponibilidad" />
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
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${errors.teamName ? 'border-red-500' : 'border-gray-300'}`}
                    placeholder="Ej: Águilas FC"
                  />
                  {errors.teamName && (<p className="text-red-500 text-sm mt-1">{errors.teamName}</p>)}
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
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow ${errors.delegatePhone ? 'border-red-500' : 'border-gray-300'}`}
                    placeholder="3001234567"
                    maxLength={15}
                  />
                  {errors.delegatePhone && (<p className="text-red-500 text-sm mt-1">{errors.delegatePhone}</p>)}
                  <p className="text-xs text-gray-500 mt-1">
                    El teléfono del jugador que será el delegado del equipo
                  </p>
                </div>

                <div className="bg-blue-50 border-l-4 border-uptc-yellow p-4">
                  <h4 className="font-semibold text-uptc-black mb-2">📋 Información Importante:</h4>
                  <ul className="text-sm text-uptc-black space-y-1">
                    <li>• Tu inscripción quedará pendiente de aprobación</li>
                    <li>• Deberás registrar exactamente {tournament.category?.membersPerTeam} jugadores</li>
                    <li>• Uno de los jugadores será designado como delegado</li>
                    <li>• Cada jugador debe cargar su carnet estudiantil (JPG, PNG máx 5MB)</li>
                  </ul>
                </div>

                <div className="flex gap-4 pt-4">
                  <button type="button" onClick={handleNextStep} className="flex-1 bg-uptc-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition">
                    Continuar →
                  </button>
                  <button type="button" onClick={() => router.push(`/torneos/${id}`)} className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition">
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
                          <input type="radio" name="delegate" checked={delegateIndex === index} onChange={() => setDelegateIndex(index)} className="w-4 h-4" />
                          <span className="text-sm font-semibold">{delegateIndex === index ? '✓ Delegado' : 'Marcar como Delegado'}</span>
                        </label>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-semibold mb-1">Nombre Completo *</label>
                          <input type="text" value={player.fullName} onChange={(e) => handlePlayerChange(index, 'fullName', e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow" placeholder="Juan Pérez López" required />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold mb-1">Documento *</label>
                          <input type="text" value={player.documentNumber} onChange={(e) => handlePlayerChange(index, 'documentNumber', e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow" placeholder="1234567890" required />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold mb-1">Código Estudiantil *</label>
                          <input type="text" value={player.studentCode} onChange={(e) => handlePlayerChange(index, 'studentCode', e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow" placeholder="202012345" required />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold mb-1">Email Institucional *</label>
                          <input type="email" value={player.institutionalEmail} onChange={(e) => handlePlayerChange(index, 'institutionalEmail', e.target.value)} className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-uptc-yellow" placeholder="jugador@uptc.edu.co" required />
                        </div>
                      </div>

                      <div>
                        <label className="block text-sm font-semibold mb-2">📷 Foto del Carnet Estudiantil *</label>
                        <input type="file" accept="image/jpeg,image/jpg,image/png" onChange={(e) => handleFileUpload(index, e.target.files?.[0] || null)} disabled={uploadingFiles[index]} className="w-full px-3 py-2 border rounded-lg bg-white disabled:opacity-50 disabled:cursor-not-allowed" />
                        {uploadingFiles[index] && (<div className="mt-2 flex items-center gap-2"><div className="animate-spin h-4 w-4 border-2 border-blue-500 border-t-transparent rounded-full"></div><p className="text-blue-600 text-sm">⏳ Subiendo foto...</p></div>)}
                        {player.idCardPhotoUrl && !uploadingFiles[index] && (<div className="mt-2 p-2 bg-green-50 border border-green-200 rounded"><p className="text-green-700 font-semibold text-sm">✅ Foto cargada correctamente</p><a href={player.idCardPhotoUrl} target="_blank" rel="noopener noreferrer" className="text-xs text-blue-600 hover:underline break-all">Ver foto subida</a></div>)}
                        {!player.idCardPhotoUrl && !uploadingFiles[index] && (<p className="text-xs text-gray-500 mt-1">Formatos: JPG, JPEG, PNG • Máximo: 5MB</p>)}
                      </div>
                    </div>
                  ))}
                </div>

                <div className="flex gap-4 pt-4">
                  <button type="button" onClick={handleBack} className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition">← Volver</button>
                  <button type="button" onClick={handleNextStep} className="flex-1 bg-uptc-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition">Continuar →</button>
                </div>
              </>
            )}

            {/* PASO 3: Disponibilidad */}
            {step === 3 && (
              <>
                <div>
                  <h3 className="text-xl font-bold text-uptc-black border-b pb-2">🕒 Disponibilidad Horaria</h3>
                  <p className="text-sm text-gray-600 mt-2 mb-4">
                    Selecciona al menos una franja por cada día (Lunes a Viernes). Puedes seleccionar varias franjas por día.
                  </p>

                  <div className="overflow-auto">
                    <table className="w-full border-collapse">
                      <thead>
                        <tr>
                          <th className="border px-3 py-2 bg-gray-100 text-left">Hora</th>
                          {DAYS_ORDER.map(day => (
                            <th key={day} className="border px-3 py-2 bg-gray-100 text-center">{DAY_LABEL[day]}</th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        {slotsToShow.map(slot => (
                          <tr key={slot.start}>
                            <td className="border px-3 py-2">{`${slot.start} - ${slot.end}`}</td>
                            {DAYS_ORDER.map(day => {
                              const selected = isSlotSelected(day, slot.start);
                              return (
                                <td
                                  key={day + slot.start}
                                  className={`border px-3 py-2 text-center cursor-pointer select-none ${selected ? 'bg-uptc-yellow' : 'bg-gray-50'}`}
                                  onClick={() => toggleSlot(day, slot.start)}
                                  role="button"
                                  aria-pressed={selected}
                                >
                                  <input type="checkbox" className="hidden" checked={selected} readOnly />
                                  <div className="min-h-[28px] flex items-center justify-center">
                                    {selected ? <span className="font-semibold">✓</span> : null}
                                  </div>
                                </td>
                              );
                            })}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <div className="flex gap-4 pt-4">
                    <button type="button" onClick={handleBack} className="px-6 py-3 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400 transition">← Volver</button>
                    <button type="submit" disabled={submitting || Object.values(uploadingFiles).some(v => v)} className="flex-1 bg-uptc-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition">
                      {submitting ? 'Enviando...' : '✅ Enviar Inscripción'}
                    </button>
                  </div>
                </div>
              </>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}

/* ---------- Helper component for step indicator ---------- */
function StepIndicator({ step, index, label }: { step: number; index: number; label: string }) {
  const active = step === index;
  return (
    <div className={`flex items-center ${active ? 'text-uptc-yellow' : 'text-gray-400'}`}>
      <div className={`w-8 h-8 rounded-full flex items-center justify-center ${active ? 'bg-uptc-yellow text-white' : 'bg-gray-200'}`}>
        {index}
      </div>
      <span className="ml-2 font-semibold">{label}</span>
    </div>
  );
}
