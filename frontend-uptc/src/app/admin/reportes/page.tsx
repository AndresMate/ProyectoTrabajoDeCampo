// frontend-uptc/src/app/admin/reportes/page.tsx - VERSIÓN CORREGIDA
'use client';

import { useState, useEffect } from 'react';
import { tournamentsService } from '@/services/tournamentsService';
import categoriesService from '@/services/categoriesService';
import reportsService from '@/services/reportsService';
import { toastWarning, toastSuccess, toastError } from '@/utils/toast';

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
  
  // --- CAMBIO 1: ESTADO ---
  // Mantenemos el torneo único para otros reportes
  const [selectedTournament, setSelectedTournament] = useState<number | null>(null);
  // Añadimos estado para MÚLTIPLES torneos
  const [selectedTournaments, setSelectedTournaments] = useState<number[]>([]);
  // --- FIN CAMBIO 1 ---

  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchTournaments();
  }, []);

  useEffect(() => {
    // Este useEffect sigue siendo útil para el reporte de 'standings' (si lo mantienes)
    // o para cualquier otro reporte futuro que use 1 torneo + 1 categoría.
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

  // --- CAMBIO 2: DEFINICIÓN DE REPORTES ---
  // Modificamos el reporte 'standings'
  const reports = [
    {
      id: 'multiTournament', // ID cambiado
      name: 'Reporte Consolidado (Multi-Torneo)', // Nombre cambiado
      description: 'Lista de jugadores inscritos en los torneos seleccionados', // Desc. cambiada
      requiresCategory: false, // Ya no requiere categoría
      icon: '🗂️' // Ícono cambiado
    },
    {
      id: 'inscriptions',
      name: 'Inscripciones (Por Torneo)', // Nombre ajustado para claridad
      description: 'Lista de equipos inscritos en un torneo',
      requiresCategory: false,
      icon: '📋'
    }
  ];
  // --- FIN CAMBIO 2 ---

  // --- CAMBIO 3: NUEVOS MANEJADORES ---
  
  // Manejador para los checkboxes de múltiple selección
  const handleTournamentCheckboxChange = (tournamentId: number) => {
    setSelectedTournaments(prev => {
      if (prev.includes(tournamentId)) {
        // Si ya está, lo quita
        return prev.filter(id => id !== tournamentId);
      } else {
        // Si no está, lo añade
        return [...prev, tournamentId];
      }
    });
  };

  // Manejador para limpiar selecciones al cambiar de reporte
  const handleReportChange = (reportId: string) => {
    setSelectedReport(reportId);
    // Reseteamos todas las selecciones para evitar conflictos
    setSelectedTournament(null);
    setSelectedTournaments([]);
    setSelectedCategory(null);
  };
  // --- FIN CAMBIO 3 ---


  // --- CAMBIO 4: LÓGICA DE GENERACIÓN ---
  const handleGenerateExcel = async () => {
    if (!selectedReport) {
      toastWarning('Selecciona un tipo de reporte');
      return;
    }

    setLoading(true);
    try {
      let blob: Blob;
      let filename: string;

      switch (selectedReport) {
        
        // ESTE ES EL NUEVO CASO (reemplaza 'standings')
        case 'multiTournament':
          if (selectedTournaments.length === 0) {
            toastWarning('Selecciona al menos un torneo para este reporte');
            setLoading(false); // Detenemos el loading
            return;
          }
          // Llamamos a la función del service (que ahora acepta un array)
          blob = await reportsService.generateStandingsExcel(selectedTournaments);
          filename = `reporte_consolidado_torneos.xlsx`;
          break;

        // ESTE CASO SE QUEDA IGUAL
        case 'inscriptions':
          if (!selectedTournament) {
            toastWarning('Selecciona un torneo');
            setLoading(false); // Detenemos el loading
            return;
          }
          blob = await reportsService.generateInscriptionsExcel(selectedTournament);
          filename = `inscriptions_t${selectedTournament}.xlsx`;
          break;

        default:
          toastWarning('Tipo de reporte no implementado aún');
          setLoading(false); // Detenemos el loading
          return;
      }

      // El resto de la lógica es la misma
      reportsService.downloadReport(blob, filename);
      toastSuccess('✅ Reporte generado exitosamente');
    } catch (error: any) {
      console.error('Error al generar reporte:', error);
      toastError('❌ Error al generar el reporte');
    } finally {
      setLoading(false);
    }
  };
  // --- FIN CAMBIO 4 ---

  const handleGeneratePDF = () => {
    toastWarning('La generación de PDFs estará disponible próximamente');
  };

  return (
    <div className="max-w-6xl mx-auto">
      {/* ... (Tu cabecera y estadísticas rápidas no cambian) ... */}
       <h1 className="text-3xl font-bold text-gray-800 mb-2">Reportes y Estadísticas</h1>
       <p className="text-gray-600 mb-6">Genera reportes en Excel o PDF sobre torneos, inscripciones y más</p>
       {/* ... (Estadísticas rápidas) ... */}
       <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        {/* ... (Tus 4 tarjetas de estadísticas) ... */}
       </div>

      {/* Generador de reportes */}
      <div className="bg-white rounded-lg shadow-lg p-6 mb-8 border-t-4 border-uptc-yellow">
        <h2 className="text-2xl font-semibold text-gray-800 mb-6">Generar Reporte</h2>

        {/* --- CAMBIO 5: UI - Selector de tipo de reporte --- */}
        <div className="mb-6">
          <label className="block text-gray-800 font-semibold mb-3 text-lg">
            Tipo de Reporte *
          </label>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {reports.map((report) => (
              <div
                key={report.id}
                // Usamos el nuevo manejador para limpiar el estado
                onClick={() => handleReportChange(report.id)} 
                className={`border-2 rounded-lg p-5 cursor-pointer transition-all ${
                  selectedReport === report.id
                    ? 'border-uptc-yellow bg-yellow-50 shadow-md'
                    : 'border-gray-300 hover:border-uptc-yellow hover:shadow'
                }`}
              >
                {/* ... (El contenido interno de la tarjeta no cambia) ... */}
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
        {/* --- FIN CAMBIO 5 --- */}


        {/* --- CAMBIO 6: UI - Selectores condicionales --- */}

        {/* 6.1: Mostrar CHECKBOXES si es el reporte 'multiTournament' */}
        {selectedReport === 'multiTournament' && (
          <div className="mb-4">
            <label className="block text-gray-800 font-semibold mb-2">
              Seleccionar Torneos * (Puedes elegir varios)
            </label>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-2 border-2 border-gray-300 rounded-lg p-4 max-h-60 overflow-y-auto bg-white">
              {tournaments.length === 0 && <p className="text-gray-500">Cargando torneos...</p>}
              {tournaments.map(t => (
                <label key={t.id} className="flex items-center gap-3 p-2 rounded-md hover:bg-gray-100 cursor-pointer">
                  <input
                    type="checkbox"
                    className="h-5 w-5 text-uptc-yellow border-gray-400 rounded focus:ring-uptc-yellow"
                    checked={selectedTournaments.includes(t.id)}
                    onChange={() => handleTournamentCheckboxChange(t.id)}
                  />
                  <span className="font-medium text-gray-800">{t.name} ({t.status})</span>
                </label>
              ))}
            </div>
            <p className="text-sm text-gray-600 mt-2">
              Seleccionados: {selectedTournaments.length}
            </p>
          </div>
        )}

        {/* 6.2: Mostrar <select> SIMPLE para otros reportes (que no sean 'multiTournament') */}
        {selectedReport && selectedReport !== 'multiTournament' && (
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
        {/* --- FIN CAMBIO 6 --- */}


        {/* Selector de categoría (NO CAMBIA) */}
        {/* Esta lógica sigue funcionando, porque 'multiTournament' tiene requiresCategory=false y se ocultará solo */}
        {selectedReport && reports.find(r => r.id === selectedReport)?.requiresCategory && selectedTournament && (
          <div className="mb-4">
            {/* ... (tu código de selector de categoría) ... */}
          </div>
        )}

        {/* --- CAMBIO 7: UI - Visibilidad de Botones --- */}
        {/* Hacemos que los botones aparezcan solo con el tipo de reporte seleccionado */}
        {selectedReport && (
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
        {/* --- FIN CAMBIO 7 --- */}
      </div>

      {/* ... (El resto de tu página (Instrucciones) no cambia) ... */}
    </div>
  );
}