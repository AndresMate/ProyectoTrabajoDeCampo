'use client';

import { useState } from 'react';

export default function AdminReportesPage() {
  const [selectedReport, setSelectedReport] = useState('');

  const reports = [
    { id: 'torneos', name: 'Reporte de Torneos', description: 'Estadísticas generales de torneos' },
    { id: 'inscripciones', name: 'Reporte de Inscripciones', description: 'Análisis de inscripciones por torneo' },
    { id: 'partidos', name: 'Reporte de Partidos', description: 'Estadísticas de partidos disputados' },
    { id: 'usuarios', name: 'Reporte de Usuarios', description: 'Actividad de usuarios en el sistema' },
  ];

  const handleGenerateReport = () => {
    if (!selectedReport) {
      alert('Por favor selecciona un tipo de reporte');
      return;
    }
    alert(`Generando reporte: ${selectedReport}`);
  };

  return (
    <div className="max-w-6xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">Reportes y Estadísticas</h1>

      {/* Estadísticas rápidas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-gray-500 text-sm mb-2">Total Torneos</div>
          <div className="text-3xl font-bold text-blue-900">24</div>
          <div className="text-xs text-green-600 mt-2">↑ +12% vs mes anterior</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-gray-500 text-sm mb-2">Inscripciones</div>
          <div className="text-3xl font-bold text-blue-900">1,234</div>
          <div className="text-xs text-green-600 mt-2">↑ +8% vs mes anterior</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-gray-500 text-sm mb-2">Partidos Jugados</div>
          <div className="text-3xl font-bold text-blue-900">456</div>
          <div className="text-xs text-red-600 mt-2">↓ -3% vs mes anterior</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-gray-500 text-sm mb-2">Usuarios Activos</div>
          <div className="text-3xl font-bold text-blue-900">892</div>
          <div className="text-xs text-green-600 mt-2">↑ +15% vs mes anterior</div>
        </div>
      </div>

      {/* Generador de reportes */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold text-gray-800 mb-6">Generar Reporte</h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
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
              <h3 className="font-semibold text-gray-800 mb-2">{report.name}</h3>
              <p className="text-sm text-gray-600">{report.description}</p>
            </div>
          ))}
        </div>

        <div className="flex gap-4">
          <button
            onClick={handleGenerateReport}
            className="bg-blue-900 text-white px-6 py-3 rounded-lg hover:bg-blue-800 transition font-medium"
          >
            Generar Reporte PDF
          </button>
          <button
            onClick={handleGenerateReport}
            className="bg-green-700 text-white px-6 py-3 rounded-lg hover:bg-green-600 transition font-medium"
          >
            Exportar a Excel
          </button>
        </div>
      </div>

      {/* Gráficos placeholder */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="font-semibold text-gray-800 mb-4">Torneos por Mes</h3>
          <div className="h-64 bg-gray-100 rounded flex items-center justify-center">
            <p className="text-gray-500">Gráfico (por implementar)</p>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="font-semibold text-gray-800 mb-4">Inscripciones por Deporte</h3>
          <div className="h-64 bg-gray-100 rounded flex items-center justify-center">
            <p className="text-gray-500">Gráfico (por implementar)</p>
          </div>
        </div>
      </div>
    </div>
  );
}