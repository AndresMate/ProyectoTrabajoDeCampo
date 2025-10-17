import api from "./api";

const reportsService = {
  // Generar reporte de tabla de posiciones en Excel
  generateStandingsExcel: async (tournamentId: number, categoryId: number): Promise<Blob> => {
    try {
      const response = await api.get(
        `/reports/standings/excel?tournamentId=${tournamentId}&categoryId=${categoryId}`,
        {
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