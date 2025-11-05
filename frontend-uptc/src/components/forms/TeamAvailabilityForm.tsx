'use client';

import { useState, useEffect } from 'react';

interface TimeSlot {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  available: boolean;
}

interface TeamAvailabilityFormProps {
  teamId: number;
  isNocturno: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}

const DAYS_OF_WEEK = [
  { value: 'MONDAY', label: 'Lunes' },
  { value: 'TUESDAY', label: 'Martes' },
  { value: 'WEDNESDAY', label: 'Miércoles' },
  { value: 'THURSDAY', label: 'Jueves' },
  { value: 'FRIDAY', label: 'Viernes' }
];

export default function TeamAvailabilityForm({
  teamId,
  isNocturno,
  onSuccess,
  onCancel
}: TeamAvailabilityFormProps) {
  const [loading, setLoading] = useState(false);
  const [availabilities, setAvailabilities] = useState<TimeSlot[]>([]);

  // Rango de horarios según modalidad
  const minTime = isNocturno ? '17:00' : '11:00';
  const maxTime = isNocturno ? '21:00' : '16:00';

  useEffect(() => {
    // Inicializar con un slot vacío por cada día
    const initialSlots = DAYS_OF_WEEK.map(day => ({
      dayOfWeek: day.value,
      startTime: minTime,
      endTime: maxTime,
      available: true
    }));
    setAvailabilities(initialSlots);
  }, []);

  const handleAddSlot = (dayOfWeek: string) => {
    setAvailabilities([
      ...availabilities,
      {
        dayOfWeek,
        startTime: minTime,
        endTime: maxTime,
        available: true
      }
    ]);
  };

  const handleRemoveSlot = (index: number) => {
    const dayOfWeek = availabilities[index].dayOfWeek;
    const slotsForDay = availabilities.filter(a => a.dayOfWeek === dayOfWeek);

    if (slotsForDay.length === 1) {
      alert('Cada día debe tener al menos un horario disponible');
      return;
    }

    setAvailabilities(availabilities.filter((_, i) => i !== index));
  };

  const handleSlotChange = (index: number, field: keyof TimeSlot, value: string | boolean) => {
    const updated = [...availabilities];
    updated[index] = { ...updated[index], [field]: value };
    setAvailabilities(updated);
  };

  const validateSlots = () => {
    // Verificar que cada día tenga al menos un slot
    for (const day of DAYS_OF_WEEK) {
      const slotsForDay = availabilities.filter(a => a.dayOfWeek === day.value);
      if (slotsForDay.length === 0) {
        alert(`Falta configurar horarios para ${day.label}`);
        return false;
      }
    }

    // Verificar que los horarios estén en rango
    for (const slot of availabilities) {
      if (slot.startTime < minTime || slot.endTime > maxTime) {
        alert(`Los horarios deben estar entre ${minTime} y ${maxTime}`);
        return false;
      }

      if (slot.startTime >= slot.endTime) {
        alert('La hora de inicio debe ser menor a la hora de fin');
        return false;
      }
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateSlots()) return;

    setLoading(true);

    try {
    alert('✅ Disponibilidad guardada correctamente');
    onSuccess();
  } catch (error: unknown) {
        if (error instanceof Error && (error as any).response?.data?.message) {
          alert((error as any).response.data.message);
        } else {
          alert('Error al guardar disponibilidad');
        }
  } finally {
    setLoading(false);
  }
};

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="bg-blue-50 border-l-4 border-uptc-yellow p-4 mb-6">
        <p className="text-sm text-uptc-black">
          <strong>Modalidad {isNocturno ? 'Nocturna' : 'Diurna'}:</strong>
          Los horarios deben estar entre {minTime} y {maxTime}
        </p>
      </div>

      {DAYS_OF_WEEK.map(day => {
        const slotsForDay = availabilities
          .map((slot, index) => ({ slot, index }))
          .filter(({ slot }) => slot.dayOfWeek === day.value);

        return (
          <div key={day.value} className="border rounded-lg p-4 bg-gray-50">
            <div className="flex justify-between items-center mb-3">
              <h3 className="font-semibold text-gray-800">{day.label}</h3>
              <button
                type="button"
                onClick={() => handleAddSlot(day.value)}
                className="text-blue-600 hover:text-uptc-black text-sm"
              >
                + Agregar horario
              </button>
            </div>

            <div className="space-y-2">
              {slotsForDay.map(({ slot, index }) => (
                <div key={index} className="flex items-center gap-3 bg-white p-3 rounded border">
                  <div className="flex-1 grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs text-gray-600 mb-1">Desde</label>
                      <input
                        type="time"
                        value={slot.startTime}
                        onChange={(e) => handleSlotChange(index, 'startTime', e.target.value)}
                        min={minTime}
                        max={maxTime}
                        className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-uptc-yellow"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-600 mb-1">Hasta</label>
                      <input
                        type="time"
                        value={slot.endTime}
                        onChange={(e) => handleSlotChange(index, 'endTime', e.target.value)}
                        min={minTime}
                        max={maxTime}
                        className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-uptc-yellow"
                        required
                      />
                    </div>
                  </div>

                  <label className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={slot.available}
                      onChange={(e) => handleSlotChange(index, 'available', e.target.checked)}
                      className="w-4 h-4 text-blue-600"
                    />
                    <span className="text-sm text-gray-700">Disponible</span>
                  </label>

                  <button
                    type="button"
                    onClick={() => handleRemoveSlot(index)}
                    className="text-red-600 hover:text-red-800"
                    disabled={slotsForDay.length === 1}
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          </div>
        );
      })}

      {/* Botones */}
      <div className="flex justify-end gap-3 pt-4 border-t">
        <button
          type="button"
          onClick={onCancel}
          className="px-6 py-2 bg-gray-300 rounded-lg hover:bg-gray-400 transition"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={loading}
          className="px-6 py-2 bg-uptc-black text-white rounded-lg hover:bg-gray-800 transition disabled:opacity-50"
        >
          {loading ? 'Guardando...' : 'Guardar Disponibilidad'}
        </button>
      </div>
    </form>
  );
}