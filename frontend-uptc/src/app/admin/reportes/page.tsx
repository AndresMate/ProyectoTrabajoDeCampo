// frontend-uptc/src/app/admin/reportes/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { tournamentsService } from '@/services/tournamentsService';
import categoriesService from '@/services/categoriesService';
import reportsService from '@/services/reportsService';

interface Tournament {
  id: number;
  name: string;
  status: string;
}

interface Category {
  id: number;
  name: string;
}

export default function AdminReportesPage() {
  const [selectedReport, setSelectedReport] = useState('');
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedTournament, setSelectedTournament] = useState<number | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchTournaments();
  }, []);

  useEffect(() => {
    if (selectedTournament) {
      fetchCategories();
    }
  }, [selectedTournament]);

  const fetchTournaments = async () => {
    try {
      const data = await tournamentsService.getAll();
      setTournaments(data);
    } catch (error) {
      console.error('Error al cargar torneos:', error);
    }
  };

  const fetchCategories = async () => {
    try {
      const data = await categoriesService.getAll();
      setCategories(data.content || data);
    } catch (error) {
      console.error('Error al cargar categorías:', error);
    }
  };

  const reports = [
    {
      id: 'standings',
      name: 'Tabla de Posiciones',
      description: 'Reporte de standings por torneo y categoría',
      requiresCategory: true,
      icon: '🏆'
    },
    {
      id: 'inscriptions',
      name: 'Inscripciones',
      description: 'Lista de equipos inscritos en un torneo',
      requiresCategory: false,
      icon: '📋'
    },
    {
      id: 'matches',
      name: 'Calendario de Partidos',
      description: 'Fixture completo del torneo',
      requiresCategory: false,
      icon: '📅'
    },
    {
      id: 'statistics',
      name: 'Estadísticas Generales',
      description: 'Resumen de torneos, equipos y partidos',
      requiresCategory: false,
      icon: '📊'
    }
  ];

  const handleGenerateExcel = async () => {
    if (!selectedReport) {
      alert('Selecciona un tipo de reporte');
      return;
    }

    if (!selectedTournament) {
      alert('Selecciona un torneo');
      return;
    }

    const report = reports.find(r => r.id === selectedReport);
    if (report?.requiresCategory && !selectedCategory) {
      alert('Selecciona una categoría');
      return;
    }

    setLoading(true);
    try {
      let blob: Blob;
      let filename: string;

      switch (selectedReport) {
        case 'standings':
          blob = await reportsService.generateStandingsExcel(
            selectedTournament,
            selectedCategory!
          );
          filename = `standings_t${selectedTournament}_c${selectedCategory}.xlsx`;
          break;

        case 'inscriptions':
          blob = await reportsService.generateInscriptionsExcel(selectedTournament);
          filename = `inscriptions_t${selectedTournament}.xlsx`;
          break;

        default:
          alert('Tipo de reporte no implementado aún');
          return;
      }

      reportsService.downloadReport(blob, filename);
      alert('✅ Reporte generado exitosamente');
    } catch (error: any) {
      console.error('Error al generar reporte:', error);
      alert('❌ Error al generar el reporte');
    } finally {
      setLoading(false);
    }
  };

  const handleGeneratePDF = () => {
    alert('La generación de PDFs estará disponible próximamente');
  };

  return (
    <div className="max-w-6xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Reportes y Estadísticas</h1>

      {/* Estadísticas rápidas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-gray-500 text-sm mb-2">Total Torneos</div>
          <div className="text-3xl font-bold text-blue-900">{tournaments.length}</div>
          <div className="text-xs text-green-600 mt-2">Activos en el sistema</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-gray-500 text-sm mb-2">Reportes Disponibles</div>
          <div className="text-3xl font-bold text-blue-900">{reports.length}</div>
          <div className="text-xs text-blue-600 mt-2">Tipos de reporte</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-gray-500 text-sm mb-2">Formato Excel</div>
          <div className="text-3xl font-bold text-green-600">✓</div>
          <div className="text-xs text-gray-600 mt-2">Disponible</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-gray-500 text-sm mb-2">Formato PDF</div>
          <div className="text-3xl font-bold text-gray-400">⏳</div>
          <div className="text-xs text-gray-600 mt-2">Próximamente</div>
        </div>
      </div>

      {/* Generador de reportes */}
      <div className="bg-white rounded-lg shadow p-6 mb-8">
        <h2 className="text-xl font-semibold text-gray-800 mb-6">Generar Reporte</h2>

        {/* Selector de tipo de reporte */}
        <div className="mb-6">
          <label className="block text-gray-700 font-medium mb-3">
            Tipo de Reporte *
          </label>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {reports.map((report) => (
              <div
                key={report.id}
                onClick={() => setSelectedReport(report.id)}
                className={`border-2 rounded-lg p-4 cursor-pointer transition ${
                  selectedReport === report.id
                    ? 'border-blue-900 bg-blue-50'
                    : 'border-gray-200 hover:border-blue-400'
                }`}
              >
                <div className="flex items-start gap-3">
                  <span className="text-3xl">{report.icon}</span>
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-800 mb-1">{report.name}</h3>
                    <p className="text-sm text-gray-600">{report.description}</p>
                    {report.requiresCategory && (
                      <span className="inline-block mt-2 text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded">
                        Requiere categoría
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Selector de torneo */}
        {selectedReport && (
          <div className="mb-4">
            <label className="block text-gray-700 font-medium mb-2">
              Seleccionar Torneo *
            </label>
            <select
              value={selectedTournament || ''}
              onChange={(e) => {
                setSelectedTournament(Number(e.target.value));
                setSelectedCategory(null);
              }}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            >
              <option value="">-- Selecciona un torneo --</option>
              {tournaments.map(t => (
                <option key={t.id} value={t.id}>
                  {t.name} ({t.status})
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Selector de categoría */}
        {selectedReport && reports.find(r => r.id === selectedReport)?.requiresCategory && selectedTournament && (
          <div className="mb-4">
            <label className="block text-gray-700 font-medium mb-2">
              Seleccionar Categoría *
            </label>
            <select
              value={selectedCategory || ''}
              onChange={(e) => setSelectedCategory(Number(e.target.value))}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            >
              <option value="">-- Selecciona una categoría --</option>
              {categories.map(c => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Botones de generación */}
        {selectedReport && selectedTournament && (
          <div className="flex gap-4 mt-6">
            <button
              onClick={handleGenerateExcel}
              disabled={loading}
              className="flex-1 bg-green-700 text-white px-6 py-3 rounded-lg hover:bg-green-600 transition font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <span className="inline-block animate-spin mr-2">⏳</span>
                  Generando...
                </>
              ) : (
                <>📊 Generar Excel</>
              )}
            </button>
            <button
              onClick={handleGeneratePDF}
              className="flex-1 bg-red-700 text-white px-6 py-3 rounded-lg hover:bg-red-600 transition font-medium"
            >
              📄 Generar PDF (Próximamente)
            </button>
          </div>
        )}
      </div>

      {/* Instrucciones */}
      <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
        <h3 className="font-semibold text-blue-900 mb-2">💡 Instrucciones</h3>
        <ul className="text-sm text-blue-800 space-y-1">
          <li>1. Selecciona el tipo de reporte que deseas generar</li>
          <li>2. Elige el torneo correspondiente</li>
          <li>3. Si el reporte lo requiere, selecciona la categoría</li>
          <li>4. Haz clic en "Generar Excel" para descargar el archivo</li>
          <li>5. El archivo se descargará automáticamente en tu navegador</li>
        </ul>
      </div>
    </div>
  );
}