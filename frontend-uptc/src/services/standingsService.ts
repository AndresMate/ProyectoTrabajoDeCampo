// frontend-uptc/src/services/standingsService.ts
import api from "./api";

export interface StandingResponse {
  id?: number;
  position: number;
  teamName: string;
  team?: {
    id: number;
    name: string;
  };
  matchesPlayed: number;
  played?: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  gf?: number;
  goalsAgainst: number;
  ga?: number;
  goalDifference: number;
  points: number;
  pts?: number;
}

const standingsService = {
  /**
   * Obtener standings de un torneo y categoría
   */
  getStandings: async (
    tournamentId: number,
    categoryId: number
  ): Promise<StandingResponse[]> => {
    try {
      console.log(`🔍 Consultando standings: tournament=${tournamentId}, category=${categoryId}`);
      const response = await api.get(
        `/standings/tournament/${tournamentId}/category/${categoryId}`
      );
      console.log('📊 Respuesta del backend:', response.data);
      return response.data as StandingResponse[];
    } catch (error) {
      console.error("❌ Error al obtener standings:", error);
      throw error;
    }
  },

  /**
   * Recalcular standings desde los resultados de los partidos
   */
  recalculate: async (
    tournamentId: number,
    categoryId: number
  ): Promise<string> => {
    try {
      console.log(`🔄 Recalculando standings: tournament=${tournamentId}, category=${categoryId}`);
      const response = await api.post(
        `/standings/recalculate`,
        null,
        {
          params: {
            tournamentId,
            categoryId
          }
        }
      );
      console.log('✅ Recalculación completada:', response.data);
      return response.data?.message || "Standings recalculados exitosamente";
    } catch (error: any) {
      console.error("❌ Error al recalcular standings:", error);
      throw error;
    }
  }
};

export default standingsService;