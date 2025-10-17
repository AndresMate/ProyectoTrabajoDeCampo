import api from "./api";

export interface Match {
  id: number;
  tournament: {
    id: number;
    name: string;
  };
  category: {
    id: number;
    name: string;
  };
  teamA: {
    id: number;
    name: string;
  };
  teamB: {
    id: number;
    name: string;
  };
  matchDate: string;
  venue?: {
    id: number;
    name: string;
  };
  scenario?: {
    id: number;
    name: string;
  };
  status: string;
  round?: number;
  matchNumber?: number;
}

export interface MatchCreateDTO {
  tournamentId: number;
  categoryId: number;
  teamAId: number;
  teamBId: number;
  matchDate: string;
  venueId?: number;
  scenarioId?: number;
  round?: number;
  matchNumber?: number;
}

export interface MatchResult {
  matchId: number;
  scoreTeamA: number;
  scoreTeamB: number;
  winnerId?: number;
  notes?: string;
}

const matchesService = {
  // === ENDPOINTS PÚBLICOS ===

  // Obtener todos los partidos
  getAll: async (): Promise<Match[]> => {
    try {
      const response = await api.get("/matches/public");
      return response.data;
    } catch (error) {
      console.error("Error al obtener partidos:", error);
      throw error;
    }
  },

  // Obtener partido por ID
  getById: async (id: number): Promise<Match> => {
    try {
      const response = await api.get(`/matches/public/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener partido:", error);
      throw error;
    }
  },

  // Obtener partidos por torneo
  getByTournament: async (tournamentId: number): Promise<Match[]> => {
    try {
      const response = await api.get(`/matches/public?tournamentId=${tournamentId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener partidos del torneo:", error);
      return [];
    }
  },

  // === ENDPOINTS PROTEGIDOS ===

  // Crear partido (Referee/Admin)
  create: async (data: MatchCreateDTO): Promise<Match> => {
    try {
      const response = await api.post("/matches", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear partido:", error);
      throw error;
    }
  },

  // Eliminar partido (Referee/Admin)
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/matches/${id}`);
    } catch (error) {
      console.error("Error al eliminar partido:", error);
      throw error;
    }
  },

  // Registrar resultado
  registerResult: async (data: MatchResult): Promise<any> => {
    try {
      const response = await api.post("/match-results", data);
      return response.data;
    } catch (error) {
      console.error("Error al registrar resultado:", error);
      throw error;
    }
  },

  // Obtener resultado de un partido
  getResult: async (matchId: number): Promise<any> => {
    try {
      const response = await api.get(`/match-results/${matchId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener resultado:", error);
      return null;
    }
  },

  // Actualizar resultado
  updateResult: async (data: MatchResult): Promise<any> => {
    try {
      const response = await api.put("/match-results", data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar resultado:", error);
      throw error;
    }
  },

  // Eliminar resultado
  deleteResult: async (matchId: number): Promise<void> => {
    try {
      await api.delete(`/match-results/${matchId}`);
    } catch (error) {
      console.error("Error al eliminar resultado:", error);
      throw error;
    }
  }
};

export default matchesService;