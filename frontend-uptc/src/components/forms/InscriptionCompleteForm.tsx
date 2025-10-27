"use client";

import React, { useState, useEffect } from "react";
import inscriptionsService from "@/services/inscriptionsService";
import tournamentsService from "@/services/tournamentsService";
import clubsService from "@/services/clubsService";

type Player = {
  fullName: string;
  documentNumber: string;
  studentCode: string;
  institutionalEmail: string;
  idCardPhotoUrl: string;
};

type Availability = {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
};

type Club = { id: number; name: string };

type Props = {
  tournamentId: number | string;
  onSuccess: () => void;
  onCancel: () => void;
};

const defaultPlayer = (): Player => ({
  fullName: "",
  documentNumber: "",
  studentCode: "",
  institutionalEmail: "",
  idCardPhotoUrl: "",
});

const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"];

const SCHEDULES = {
  DIURNO: [
    ["11:00", "12:00"],
    ["12:00", "13:00"],
    ["13:00", "14:00"],
    ["14:00", "15:00"],
    ["15:00", "16:00"],
  ],
  NOCTURNO: [
    ["17:00", "18:00"],
    ["18:00", "19:00"],
    ["19:00", "20:00"],
    ["20:00", "21:00"],
  ],
};

export default function InscriptionCompleteForm({
  tournamentId,
  onSuccess,
  onCancel,
}: Props) {
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [tournament, setTournament] = useState<any>(null);
  const [clubs, setClubs] = useState<Club[]>([]);
  const [players, setPlayers] = useState<Player[]>([]);
  const [delegateIndex, setDelegateIndex] = useState(0);
  const [availability, setAvailability] = useState<Availability[]>([]);

  const [formData, setFormData] = useState({
    teamName: "",
    clubId: 0,
    delegatePhone: "",
  });

  // === Cargar torneo y clubs ===
  useEffect(() => {
    const fetchData = async () => {
      try {
        const t = await tournamentsService.getById(tournamentId);
        setTournament(t);
        const c = await clubsService.getAll();
        setClubs(Array.isArray(c) ? c : []);
      } catch (e) {
        console.error("Error cargando datos:", e);
      }
    };
    fetchData();
  }, [tournamentId]);

  // === Inicializar jugadores ===
  useEffect(() => {
    if (tournament?.category?.membersPerTeam) {
      setPlayers(
        Array.from({ length: tournament.category.membersPerTeam }, () =>
          defaultPlayer()
        )
      );
    }
  }, [tournament]);

  // === Manejo de jugadores ===
  const handlePlayerChange = (i: number, field: keyof Player, value: string) => {
    setPlayers((prev) => {
      const updated = [...prev];
      updated[i] = { ...updated[i], [field]: value };
      return updated;
    });
  };

  // === Subir foto ===
  const handleFileUpload = async (index: number, file: File | null) => {
    if (!file) return;
    if (!["image/jpeg", "image/jpg", "image/png"].includes(file.type)) {
      alert("Solo se permiten JPG o PNG");
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      alert("El archivo no puede superar los 5MB");
      return;
    }
    handlePlayerChange(index, "idCardPhotoUrl", "UPLOADING...");
    try {
      const data = new FormData();
      data.append("file", file);
      const res = await fetch("http://localhost:8080/api/files/upload/id-card", {
        method: "POST",
        body: data,
      });
      const json = await res.json();
      handlePlayerChange(index, "idCardPhotoUrl", json.url || "");
    } catch (e) {
      handlePlayerChange(index, "idCardPhotoUrl", "");
      alert("Error al subir la foto");
    }
  };

  // === Validar Paso 1 (datos del equipo) ===
  const validateStep1 = async () => {
    if (!formData.teamName.trim()) return alert("El nombre del equipo es requerido");
    if (!formData.delegatePhone.trim()) return alert("Teléfono del delegado requerido");

    const isTeamNameAvailable = await inscriptionsService.checkTeamName(
      Number(tournamentId),
      formData.teamName
    );
    if (!isTeamNameAvailable)
      return alert("❌ Este nombre de equipo ya está registrado en este torneo");

    if (formData.clubId) {
      const isClubAvailable = await inscriptionsService.checkClub(
        Number(tournamentId),
        formData.clubId
      );
      if (!isClubAvailable)
        return alert("❌ Este club ya tiene un equipo inscrito en este torneo");
    }

    setStep(2);
  };

  // === Validar Paso 2 (jugadores) ===
  const validateStep2 = async () => {
    for (let i = 0; i < players.length; i++) {
      const p = players[i];
      if (!p.fullName || !p.documentNumber || !p.studentCode || !p.institutionalEmail)
        return alert(`Faltan datos del jugador ${i + 1}`);
      if (p.idCardPhotoUrl === "UPLOADING...") return alert(`Foto en proceso del jugador ${i + 1}`);
      if (!p.idCardPhotoUrl) return alert(`Foto obligatoria para el jugador ${i + 1}`);

      const available = await inscriptionsService.checkPlayer(
        Number(tournamentId),
        p.documentNumber
      );
      if (!available)
        return alert(`El jugador ${p.fullName} ya está inscrito en otro equipo`);
    }
    setStep(3);
  };

  // === Validar Paso 3 (disponibilidad) ===
  const validateStep3 = (): boolean => {
    const selectedDays = new Set(availability.map((a) => a.dayOfWeek));
    for (const d of DAYS) {
      if (!selectedDays.has(d)) {
        alert(`Debe seleccionar al menos un horario para ${d}`);
        return false;
      }
    }
    return true;
  };

  // === Enviar inscripción completa ===
  const handleSubmit = async () => {
    if (!validateStep3()) return;
    setLoading(true);
    try {
      const payload = {
        tournamentId: Number(tournamentId),
        categoryId: tournament.category.id,
        clubId: formData.clubId || undefined,
        teamName: formData.teamName,
        delegatePhone: formData.delegatePhone,
        delegateIndex,
        players,
        availability,
      };
      await inscriptionsService.create(payload);
      alert("✅ Inscripción creada correctamente");
      onSuccess();
    } catch (e) {
      console.error(e);
      alert("❌ Error al crear inscripción");
    } finally {
      setLoading(false);
    }
  };

  // === Renderizado de pasos ===
  if (!tournament)
    return <div className="p-6 text-center">Cargando datos del torneo...</div>;

  return (
    <div className="p-6 bg-white rounded-lg shadow-lg space-y-6">
      {step === 1 && (
        <>
          <h2 className="text-xl font-bold mb-4">📋 Datos del Equipo</h2>
          <div className="space-y-4">
            <input
              type="text"
              placeholder="Nombre del equipo *"
              className="w-full p-2 border rounded"
              value={formData.teamName}
              onChange={(e) =>
                setFormData({ ...formData, teamName: e.target.value })
              }
            />
            <select
              value={formData.clubId}
              onChange={(e) =>
                setFormData({ ...formData, clubId: Number(e.target.value) })
              }
              className="w-full p-2 border rounded"
            >
              <option value={0}>Sin club</option>
              {Array.isArray(clubs) &&
                clubs.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
            </select>
            <input
              type="tel"
              placeholder="Teléfono del delegado *"
              className="w-full p-2 border rounded"
              value={formData.delegatePhone}
              onChange={(e) =>
                setFormData({ ...formData, delegatePhone: e.target.value })
              }
            />
          </div>
          <button
            onClick={validateStep1}
            className="mt-4 bg-black text-yellow-400 px-6 py-3 rounded-lg font-bold"
          >
            Siguiente ➡️
          </button>
        </>
      )}

      {step === 2 && (
        <>
          <h2 className="text-xl font-bold mb-4">👥 Jugadores</h2>
          {players.map((p, i) => (
            <div key={i} className="border p-3 rounded mb-3 bg-gray-50">
              <div className="flex justify-between">
                <h4 className="font-semibold">Jugador {i + 1}</h4>
                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    checked={delegateIndex === i}
                    onChange={() => setDelegateIndex(i)}
                  />
                  <span>Delegado</span>
                </label>
              </div>
              <input
                className="w-full border p-2 my-1"
                placeholder="Nombre completo"
                value={p.fullName}
                onChange={(e) =>
                  handlePlayerChange(i, "fullName", e.target.value)
                }
              />
              <input
                className="w-full border p-2 my-1"
                placeholder="Documento"
                value={p.documentNumber}
                onChange={(e) =>
                  handlePlayerChange(i, "documentNumber", e.target.value)
                }
              />
              <input
                className="w-full border p-2 my-1"
                placeholder="Código estudiantil"
                value={p.studentCode}
                onChange={(e) =>
                  handlePlayerChange(i, "studentCode", e.target.value)
                }
              />
              <input
                className="w-full border p-2 my-1"
                placeholder="Correo institucional"
                value={p.institutionalEmail}
                onChange={(e) =>
                  handlePlayerChange(i, "institutionalEmail", e.target.value)
                }
              />
              <input
                type="file"
                accept="image/*"
                onChange={(e) =>
                  handleFileUpload(i, e.target.files?.[0] || null)
                }
              />
            </div>
          ))}
          <div className="flex justify-between">
            <button
              onClick={() => setStep(1)}
              className="bg-gray-300 px-4 py-2 rounded"
            >
              ⬅️ Volver
            </button>
            <button
              onClick={validateStep2}
              className="bg-black text-yellow-400 px-6 py-3 rounded font-bold"
            >
              Siguiente ➡️
            </button>
          </div>
        </>
      )}

      {step === 3 && (
        <>
          <h2 className="text-xl font-bold mb-4">🕐 Disponibilidad Horaria</h2>
          <div className="overflow-x-auto">
            <table className="w-full border">
              <thead>
                <tr className="bg-gray-100">
                  <th className="p-2 border">Hora</th>
                  {DAYS.map((d) => (
                    <th key={d} className="p-2 border">
                      {d}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {(SCHEDULES[
                  tournament.modality === "NOCTURNO" ? "NOCTURNO" : "DIURNO"
                ] as [string, string][]).map(([start, end], i) => (
                  <tr key={i}>
                    <td className="p-2 border text-center">
                      {start} - {end}
                    </td>
                    {DAYS.map((day) => {
                      const selected = availability.some(
                        (a) =>
                          a.dayOfWeek === day &&
                          a.startTime === start &&
                          a.endTime === end
                      );
                      return (
                        <td
                          key={day}
                          className="p-2 border text-center cursor-pointer"
                          onClick={() => {
                            setAvailability((prev) => {
                              if (selected)
                                return prev.filter(
                                  (a) =>
                                    !(
                                      a.dayOfWeek === day &&
                                      a.startTime === start &&
                                      a.endTime === end
                                    )
                                );
                              return [...prev, { dayOfWeek: day, startTime: start, endTime: end }];
                            });
                          }}
                        >
                          {selected ? "✅" : ""}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="flex justify-between mt-4">
            <button
              onClick={() => setStep(2)}
              className="bg-gray-300 px-4 py-2 rounded"
            >
              ⬅️ Volver
            </button>
            <button
              disabled={loading}
              onClick={handleSubmit}
              className="bg-green-600 text-white px-6 py-3 rounded font-bold"
            >
              {loading ? "Enviando..." : "✅ Finalizar inscripción"}
            </button>
          </div>
        </>
      )}
    </div>
  );
}
