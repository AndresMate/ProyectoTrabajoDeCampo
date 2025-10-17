import api from "./api";

export interface Standing {
  position: number;
  team: {
    id: number;
    name: string;
  };
  matchesPlayed: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDifference: number;
  points: number;
}

const standingsService = {
  // Obtener tabla de posiciones
  getStandings: async (tournamentId: number, categoryId: number): Promise<Standing[]> => {
    try {
      const response = await api.get(`/standings/${tournamentId}/${categoryId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener tabla de posiciones:", error);
      throw error;
    }
  },

  // Recalcular tabla de posiciones (Admin)
  recalculate: async (tournamentId: number, categoryId: number): Promise<string> => {
    try {
      const response = await api.post(`/standings/${tournamentId}/${categoryId}/recalculate`);
      return response.data;
    } catch (error) {
      console.error("Error al recalcular tabla:", error);
      throw error;
    }
  }
};

export default standingsService;