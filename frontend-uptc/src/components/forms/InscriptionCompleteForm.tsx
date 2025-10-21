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

    try {
      const data = new FormData();
      data.append('file', file);

      const response = await fetch('http://localhost:8080/api/files/upload/id-card', {
        method: 'POST',
        body: data
      });

      if (!response.ok) throw new Error('Error al subir archivo');

      const json = await response.json();
      const fileUrl = json.fileUrl as string | undefined;
      if (fileUrl) {
        handlePlayerChange(index, 'idCardPhotoUrl', fileUrl);
        alert('✅ Foto cargada exitosamente');
      } else {
        throw new Error('Respuesta de upload sin fileUrl');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('❌ Error al cargar la foto');
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

      const response = await fetch('http://localhost:8080/api/inscriptions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) throw new Error('Error al crear inscripción');

      alert('✅ Inscripción creada exitosamente');
      onSuccess();
    } catch (error) {
      console.error('Error:', error);
      alert('❌ Error al crear la inscripción');
    } finally {
      setLoading(false);
    }
  };

  if (!tournament) return <div className="text-center">Cargando...</div>;

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
                  📷 Foto del Carnet *
                </label>
                <input
                  type="file"
                  accept="image/jpeg,image/jpg,image/png"
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleFileUpload(index, e.target.files?.[0] ?? null)}
                  className="w-full"
                />
                {player.idCardPhotoUrl && (
                  <p className="text-green-600 text-sm mt-1">✅ Foto cargada</p>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="flex gap-3 pt-4 border-t">
        <button
          type="submit"
          disabled={loading}
          className="flex-1 bg-black text-yellow-400 py-3 rounded-lg font-bold disabled:opacity-50"
        >
          {loading ? 'Enviando...' : '✅ Crear Inscripción'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="px-6 py-3 bg-gray-300 rounded-lg"
        >
          Cancelar
        </button>
      </div>
    </form>
  );
};

export default InscriptionCompleteForm;
