// frontend-uptc/src/app/admin/reportes/page.tsx - VERSIÓN CORREGIDA
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
      <h1 className="text-3xl font-bold text-gray-800 mb-2">Reportes y Estadísticas</h1>
      <p className="text-gray-600 mb-6">Genera reportes en Excel o PDF sobre torneos, inscripciones y más</p>

      {/* Estadísticas rápidas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-uptc-yellow">
          <div className="text-gray-600 text-sm mb-2 font-semibold">Total Torneos</div>
          <div className="text-3xl font-bold text-uptc-black">{tournaments.length}</div>
          <div className="text-xs text-green-600 mt-2 font-medium">Activos en el sistema</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
          <div className="text-gray-600 text-sm mb-2 font-semibold">Reportes Disponibles</div>
          <div className="text-3xl font-bold text-uptc-black">{reports.length}</div>
          <div className="text-xs text-blue-600 mt-2 font-medium">Tipos de reporte</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
          <div className="text-gray-600 text-sm mb-2 font-semibold">Formato Excel</div>
          <div className="text-3xl font-bold text-green-600">✓</div>
          <div className="text-xs text-gray-700 mt-2 font-medium">Disponible</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-gray-400">
          <div className="text-gray-600 text-sm mb-2 font-semibold">Formato PDF</div>
          <div className="text-3xl font-bold text-gray-400">⏳</div>
          <div className="text-xs text-gray-700 mt-2 font-medium">Próximamente</div>
        </div>
      </div>

      {/* Generador de reportes */}
      <div className="bg-white rounded-lg shadow-lg p-6 mb-8 border-t-4 border-uptc-yellow">
        <h2 className="text-2xl font-semibold text-gray-800 mb-6">Generar Reporte</h2>

        {/* Selector de tipo de reporte */}
        <div className="mb-6">
          <label className="block text-gray-800 font-semibold mb-3 text-lg">
            Tipo de Reporte *
          </label>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {reports.map((report) => (
              <div
                key={report.id}
                onClick={() => setSelectedReport(report.id)}
                className={`border-2 rounded-lg p-5 cursor-pointer transition-all ${
                  selectedReport === report.id
                    ? 'border-uptc-yellow bg-yellow-50 shadow-md'
                    : 'border-gray-300 hover:border-uptc-yellow hover:shadow'
                }`}
              >
                <div className="flex items-start gap-3">
                  <span className="text-4xl">{report.icon}</span>
                  <div className="flex-1">
                    <h3 className="font-bold text-gray-900 mb-1 text-lg">{report.name}</h3>
                    <p className="text-sm text-gray-700 font-medium">{report.description}</p>
                    {report.requiresCategory && (
                      <span className="inline-block mt-2 text-xs bg-yellow-100 text-yellow-800 px-2 py-1 rounded font-semibold">
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
            <label className="block text-gray-800 font-semibold mb-2">
              Seleccionar Torneo *
            </label>
            <select
              value={selectedTournament || ''}
              onChange={(e) => {
                setSelectedTournament(Number(e.target.value));
                setSelectedCategory(null);
              }}
              className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900 font-medium"
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
            <label className="block text-gray-800 font-semibold mb-2">
              Seleccionar Categoría *
            </label>
            <select
              value={selectedCategory || ''}
              onChange={(e) => setSelectedCategory(Number(e.target.value))}
              className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900 font-medium"
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
              className="flex-1 bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition font-bold disabled:opacity-50 disabled:cursor-not-allowed shadow-lg"
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
              className="flex-1 bg-red-600 text-white px-6 py-3 rounded-lg hover:bg-red-700 transition font-bold shadow-lg"
            >
              📄 Generar PDF (Próximamente)
            </button>
          </div>
        )}
      </div>

      {/* Instrucciones */}
      <div className="bg-blue-50 border-l-4 border-blue-500 p-6 rounded-lg shadow">
        <h3 className="font-bold text-blue-900 mb-3 text-lg flex items-center gap-2">
          <span>💡</span> Instrucciones
        </h3>
        <ul className="text-sm text-blue-900 space-y-2 font-medium">
          <li className="flex items-start gap-2">
            <span className="font-bold">1.</span>
            <span>Selecciona el tipo de reporte que deseas generar</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="font-bold">2.</span>
            <span>Elige el torneo correspondiente</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="font-bold">3.</span>
            <span>Si el reporte lo requiere, selecciona la categoría</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="font-bold">4.</span>
            <span>Haz clic en "Generar Excel" para descargar el archivo</span>
          </li>
          <li className="flex items-start gap-2">
            <span className="font-bold">5.</span>
            <span>El archivo se descargará automáticamente en tu navegador</span>
          </li>
        </ul>
      </div>
    </div>
  );
}