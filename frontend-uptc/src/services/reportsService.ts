import api from "./api";

const reportsService = {
  // Generar reporte de tabla de posiciones en Excel
  // --- MÉTODO MODIFICADO ---
  /**
   * Genera un reporte consolidado de inscripciones para múltiples torneos.
   * @param tournamentIds Un array de IDs de torneos
   * @returns Un Blob con el archivo Excel
   */
  generateStandingsExcel: async (tournamentIds: number[]): Promise<Blob> => {
    try {
      // 1. Usamos URLSearchParams para construir la URL
      // Esto nos permite enviar parámetros de array como:
      // /excel?tournamentIds=1&tournamentIds=2&tournamentIds=3
      const params = new URLSearchParams();
      tournamentIds.forEach(id => {
        params.append('tournamentIds', id.toString());
      });

      // 2. Hacemos la llamada GET a la URL base y pasamos los params
      const response = await api.get(
        '/reports/standings/excel', // La ruta base (sin query string)
        {
          params: params, // 'api' (axios) adjuntará los params
          responseType: 'blob'
        }
      );
      return response.data;
    } catch (error) {
      console.error("Error al generar reporte de standings:", error);
      throw error;
    }
  },

  // Generar reporte de inscripciones en Excel
  generateInscriptionsExcel: async (tournamentId: number): Promise<Blob> => {
    try {
      const response = await api.get(
        `/reports/inscriptions/excel?tournamentId=${tournamentId}`,
        {
          responseType: 'blob'
        }
      );
      return response.data;
    } catch (error) {
      console.error("Error al generar reporte de inscripciones:", error);
      throw error;
    }
  },

  // Descargar reporte
  downloadReport: (blob: Blob, filename: string) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }
};

export default reportsService;