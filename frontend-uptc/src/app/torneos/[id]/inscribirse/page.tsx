'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { tournamentsService } from '@/services/tournamentsService';
import categoriesService from '@/services/categoriesService';

export default function InscripcionTorneoPage() {
  const { id } = useParams();
  const [tournament, setTournament] = useState<any>(null);
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTournament();
  }, [id]);

  const fetchTournament = async () => {
    try {
      const data = await tournamentsService.getById(id as string);
      setTournament(data);

      const sportId = data.sport?.id;
      if (sportId) {
        const cats = await categoriesService.getActiveBySport(sportId);
        setCategories(cats);
      }
    } catch (error) {
      console.error('Error al cargar torneo:', error);
      alert('No se pudo cargar el torneo');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <p className="text-center py-10">Cargando torneo...</p>;

  return (
    <div className="max-w-3xl mx-auto p-6">
      <h1 className="text-2xl font-semibold mb-4 text-blue-900">{tournament?.name}</h1>

      <p className="text-gray-600 mb-6">{tournament?.description}</p>

      <div className="mb-4">
        <label className="block font-medium mb-1">Selecciona una categoría:</label>
        <select className="border rounded p-2 w-full">
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name} — {cat.membersPerTeam} jugadores por equipo
            </option>
          ))}
        </select>
      </div>

      <button className="bg-blue-900 text-white px-6 py-3 rounded hover:bg-blue-800 transition">
        Inscribirse
      </button>
    </div>
  );
}
