import api from './api';

// Esto debe coincidir con tu TeamSummaryDTO del backend
export interface TeamSummary {
  id: number;
  name: string;
  club?: { id: number; name: string } | null;
}

export interface Standing {
  position?: number;
  team?: TeamSummary | null;
  teamName?: string;  // ✅ Agregar teamName
  teamId?: number;
  tournamentId?: number;
  categoryId?: number;
  played?: number;
  wins?: number;
  draws?: number;
  losses?: number;
  goalsFor?: number;
  goalsAgainst?: number;
  goalDifference?: number;
  points?: number;
}

const standingsService = {
  getStandings: async (tournamentId: number, categoryId?: number): Promise<Standing[]> => {
    // ✅ CORREGIDO: Usar la URL correcta con parámetros en la ruta
    if (!categoryId) {
      throw new Error('categoryId is required');
    }
    const response = await api.get(`/standings/${tournamentId}/${categoryId}`);
    return response.data as Standing[];
  },

  recalculate: async (tournamentId: number, categoryId: number): Promise<string> => {
    const response = await api.post(`/standings/${tournamentId}/${categoryId}/recalculate`);
    return response.data as string;
  }
};

export default standingsService;