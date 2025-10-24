import React, { useState, useEffect, FC } from 'react';

type Player = {
  fullName: string;
  documentNumber: string;
  studentCode: string;
  institutionalEmail: string;
  idCardPhotoUrl: string;
};

type Category = {
  id: number;
  name?: string;
  membersPerTeam: number;
};

type Tournament = {
  id: number;
  name: string;
  category: Category;
} | null;

type Club = {
  id: number;
  name: string;
};

type Props = {
  tournamentId: number | string;
  onSuccess: () => void;
  onCancel: () => void;
};

const defaultPlayer = (): Player => ({
  fullName: '',
  documentNumber: '',
  studentCode: '',
  institutionalEmail: '',
  idCardPhotoUrl: ''
});

const InscriptionCompleteForm: FC<Props> = ({ tournamentId, onSuccess, onCancel }) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [tournament, setTournament] = useState<Tournament>(null);
  const [clubs, setClubs] = useState<Club[]>([]);
  const [players, setPlayers] = useState<Player[]>([]);
  const [delegateIndex, setDelegateIndex] = useState<number>(0);

  const [formData, setFormData] = useState({
    teamName: '',
    clubId: 0,
    delegatePhone: ''
  });

  // Cargar datos básicos (tournament y clubs)
  useEffect(() => {
    const fetchData = async () => {
      try {
        const tRes = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}`);
        if (tRes.ok) {
          const tData = await tRes.json();
          setTournament(tData);
        }
      } catch (err) {
        console.error('Error cargando torneo:', err);
      }

      try {
        const cRes = await fetch('http://localhost:8080/api/clubs');
        if (cRes.ok) {
          const cData = await cRes.json();
          setClubs(cData);
        }
      } catch (err) {
        console.error('Error cargando clubs:', err);
      }
    };

    fetchData();
  }, [tournamentId]);

  // Inicializar jugadores según categoría
  useEffect(() => {
    const members = tournament?.category?.membersPerTeam;
    if (typeof members === 'number' && members > 0) {
      const initialPlayers: Player[] = Array.from({ length: members }, () => defaultPlayer());
      setPlayers(initialPlayers);
    }
  }, [tournament]);

  const handlePlayerChange = (index: number, field: keyof Player, value: string) => {
    setPlayers(prev => {
      const updated = [...prev];
      updated[index] = { ...(updated[index] ?? defaultPlayer()), [field]: value };
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

    // Mostrar indicador de carga
    handlePlayerChange(index, 'idCardPhotoUrl', 'UPLOADING...');

    try {
      const data = new FormData();
      data.append('file', file);

      console.log('⬆️  Enviando archivo al servidor...');

      const response = await fetch('http://localhost:8080/api/files/upload/id-card', {
        method: 'POST',
        body: data
      });

      console.log('📥 Respuesta del servidor:', response.status, response.statusText);

      if (!response.ok) {
        const errorData = await response.json();
        console.error('❌ Error del servidor:', errorData);
        throw new Error(errorData.error || 'Error al subir archivo');
      }

      const json = await response.json();
      console.log('✅ JSON completo de respuesta:', json);

      // ✅ CORRECCIÓN: Usar json.url en lugar de json.fileUrl
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
      handlePlayerChange(index, 'idCardPhotoUrl', ''); // Limpiar el estado
      alert(`❌ Error al cargar la foto del jugador ${index + 1}: ${error instanceof Error ? error.message : 'Error desconocido'}`);
    }
  };

  const validateForm = (): boolean => {
    if (!formData.teamName.trim()) {
      alert('El nombre del equipo es requerido');
      return false;
    }

    if (!formData.delegatePhone.trim()) {
      alert('El teléfono del delegado es requerido');
      return false;
    }

    for (let i = 0; i < players.length; i++) {
      const player = players[i];

      if (!player.fullName || !player.documentNumber || !player.studentCode ||
          !player.institutionalEmail || !player.idCardPhotoUrl) {
        alert(`Falta completar datos del jugador ${i + 1}`);
        console.log('❌ Jugador incompleto:', player);
        return false;
      }

      if (player.idCardPhotoUrl === 'UPLOADING...') {
        alert(`La foto del jugador ${i + 1} aún se está subiendo. Por favor espera.`);
        return false;
      }

      if (!/\S+@\S+\.\S+/.test(player.institutionalEmail)) {
        alert(`Email inválido para jugador ${i + 1}`);
        return false;
      }
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateForm()) return;

    setLoading(true);

    try {
      if (!tournament) throw new Error('Torneo no cargado');

      const payload = {
        tournamentId: Number(tournamentId),
        categoryId: tournament.category.id,
        clubId: formData.clubId || undefined,
        teamName: formData.teamName,
        delegatePhone: formData.delegatePhone,
        delegateIndex,
        players
      };

      console.log('📤 Payload a enviar:', payload);

      const response = await fetch('http://localhost:8080/api/inscriptions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Error al crear inscripción');
      }

      alert('✅ Inscripción creada exitosamente');
      onSuccess();
    } catch (error) {
      console.error('❌ Error completo:', error);
      alert(`❌ Error al crear la inscripción: ${error instanceof Error ? error.message : 'Error desconocido'}`);
    } finally {
      setLoading(false);
    }
  };

  if (!tournament) return <div className="text-center p-8">Cargando...</div>;

  return (
    <form onSubmit={handleSubmit} className="space-y-6 p-6 bg-white rounded-lg shadow">
      <div className="border-b pb-4">
        <h3 className="text-lg font-bold mb-4">📋 Datos del Equipo</h3>

        <div className="grid gap-4">
          <div>
            <label className="block font-semibold mb-2">Nombre del Equipo *</label>
            <input
              type="text"
              value={formData.teamName}
              onChange={(e) => setFormData({...formData, teamName: e.target.value})}
              className="w-full px-4 py-2 border rounded-lg"
              required
            />
          </div>

          <div>
            <label className="block font-semibold mb-2">Club (Opcional)</label>
            <select
              value={formData.clubId}
              onChange={(e) => setFormData({...formData, clubId: Number(e.target.value)})}
              className="w-full px-4 py-2 border rounded-lg"
            >
              <option value={0}>Sin club</option>
              {clubs.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>

          <div>
            <label className="block font-semibold mb-2">Teléfono del Delegado *</label>
            <input
              type="tel"
              value={formData.delegatePhone}
              onChange={(e) => setFormData({...formData, delegatePhone: e.target.value})}
              className="w-full px-4 py-2 border rounded-lg"
              placeholder="3001234567"
              required
            />
          </div>
        </div>
      </div>

      <div>
        <h3 className="text-lg font-bold mb-4">
          👥 Jugadores ({tournament.category.membersPerTeam} requeridos)
        </h3>

        <div className="space-y-6">
          {players.map((player, index) => (
            <div key={index} className="border p-4 rounded-lg bg-gray-50">
              <div className="flex justify-between items-center mb-3">
                <h4 className="font-semibold">Jugador {index + 1}</h4>
                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    name="delegate"
                    checked={delegateIndex === index}
                    onChange={() => setDelegateIndex(index)}
                  />
                  <span className="text-sm">Delegado</span>
                </label>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <input
                  type="text"
                  placeholder="Nombre Completo *"
                  value={player.fullName}
                  onChange={(e) => handlePlayerChange(index, 'fullName', e.target.value)}
                  className="px-3 py-2 border rounded"
                  required
                />
                <input
                  type="text"
                  placeholder="Documento *"
                  value={player.documentNumber}
                  onChange={(e) => handlePlayerChange(index, 'documentNumber', e.target.value)}
                  className="px-3 py-2 border rounded"
                  required
                />
                <input
                  type="text"
                  placeholder="Código Estudiantil *"
                  value={player.studentCode}
                  onChange={(e) => handlePlayerChange(index, 'studentCode', e.target.value)}
                  className="px-3 py-2 border rounded"
                  required
                />
                <input
                  type="email"
                  placeholder="Email Institucional *"
                  value={player.institutionalEmail}
                  onChange={(e) => handlePlayerChange(index, 'institutionalEmail', e.target.value)}
                  className="px-3 py-2 border rounded"
                  required
                />
              </div>

              <div className="mt-3">
                <label className="block text-sm font-semibold mb-2">
                  📷 Foto del Carnet Estudiantil *
                </label>
                <input
                  type="file"
                  accept="image/jpeg,image/jpg,image/png"
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                    const file = e.target.files?.[0] ?? null;
                    if (file) {
                      handleFileUpload(index, file);
                    }
                  }}
                  className="w-full border p-2 rounded"
                  disabled={player.idCardPhotoUrl === 'UPLOADING...'}
                />

                {/* Estados visuales */}
                {player.idCardPhotoUrl === 'UPLOADING...' && (
                  <div className="mt-2 flex items-center gap-2">
                    <div className="animate-spin h-4 w-4 border-2 border-blue-500 border-t-transparent rounded-full"></div>
                    <p className="text-blue-600 text-sm">⏳ Subiendo foto...</p>
                  </div>
                )}

                {player.idCardPhotoUrl && player.idCardPhotoUrl !== 'UPLOADING...' && (
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

                {!player.idCardPhotoUrl && (
                  <p className="text-gray-500 text-xs mt-1">
                    Formatos: JPG, JPEG, PNG • Máximo: 5MB
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="flex gap-3 pt-4 border-t">
        <button
          type="submit"
          disabled={loading || players.some(p => p.idCardPhotoUrl === 'UPLOADING...')}
          className="flex-1 bg-black text-yellow-400 py-3 rounded-lg font-bold disabled:opacity-50 hover:bg-gray-800 transition"
        >
          {loading ? 'Enviando...' : '✅ Crear Inscripción'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          disabled={loading}
          className="px-6 py-3 bg-gray-300 rounded-lg hover:bg-gray-400 transition disabled:opacity-50"
        >
          Cancelar
        </button>
      </div>
    </form>
  );
};

export default InscriptionCompleteForm;