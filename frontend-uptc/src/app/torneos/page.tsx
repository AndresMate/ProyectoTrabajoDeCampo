'use client';

import { useEffect, useState } from 'react';
import { tournamentsService } from '@/services/tournamentsService';

interface Tournament {
  id: number;
  nombre: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string;
  estado: string;
}

export default function TorneosPage() {
  const [torneos, setTorneos] = useState<Tournament[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTorneos = async () => {
      try {
        const data = await tournamentsService.getAll();
        setTorneos(data);
      } catch (error) {
        console.error('Error al cargar torneos:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchTorneos();
  }, []);

  if (loading) {
    return <p className="text-center mt-10 text-gray-600">Cargando torneos...</p>;
  }

  if (torneos.length === 0) {
    return <p className="text-center mt-10 text-gray-600">No hay torneos disponibles actualmente.</p>;
  }

  return (
    <div className="max-w-6xl mx-auto mt-10 grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
      {torneos.map((torneo) => (
        <div key={torneo.id} className="bg-white shadow-lg rounded-lg p-6 hover:shadow-xl transition">
          <h2 className="text-xl font-semibold text-slate-800 mb-2">{torneo.nombre}</h2>
          <p className="text-sm text-gray-600 mb-2">{torneo.descripcion}</p>
          <p className="text-sm text-gray-500 mb-1">
            <strong>Inicio:</strong> {new Date(torneo.fechaInicio).toLocaleDateString()}
          </p>
          <p className="text-sm text-gray-500 mb-4">
            <strong>Fin:</strong> {new Date(torneo.fechaFin).toLocaleDateString()}
          </p>
          <span
            className={`text-xs px-3 py-1 rounded-full ${
              torneo.estado === 'activo'
                ? 'bg-green-100 text-green-700'
                : 'bg-gray-200 text-gray-700'
            }`}
          >
            {torneo.estado}
          </span>
          <button className="mt-4 w-full bg-slate-900 text-white py-2 rounded hover:bg-slate-800 transition">
            Ver detalles
          </button>
        </div>
      ))}
    </div>
  );
}
