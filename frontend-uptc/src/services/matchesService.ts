// frontend-uptc/src/services/matchesService.ts - VERSIÓN FINAL
import api from "./api";


export interface Match {
  id: number;
  startsAt: string | null;
  status: string;
  tournament: {
    id: number;
    name: string;
  };
  category: {
    id: number;
    name: string;
  };
  scenario?: {
    id: number;
    name: string;
    venue?: {
      id: number;
      name: string;
    };
  } | null;
  homeTeam: {
    id: number;
    name: string;
    club?: {
      id: number;
      name: string;
    };
  };
  awayTeam: {
    id: number;
    name: string;
    club?: {
      id: number;
      name: string;
    };
  };
  referee?: {
    id: number;
    fullName: string;
  } | null;
  result?: {
    homeScore: number;
    awayScore: number;
    notes?: string;
  } | null;
}

// DTO de creación/actualización (MatchDTO del backend)
export interface MatchCreateDTO {
  tournamentId: number;
  categoryId: number;
  scenarioId?: number | null;
  startsAt?: string | null; // LocalDateTime en ISO: "2025-01-15T14:30:00"
  homeTeamId: number;
  awayTeamId: number;
  status?: string;
  refereeId?: number | null;
}

// DTO para resultado de partido
export interface MatchResult {
  matchId: number;
  homeScore: number;
  awayScore: number;
  winnerId?: number;
  notes?: string;
}

// ============================================
// SERVICIO
// ============================================

const matchesService = {
  // === ENDPOINTS PÚBLICOS ===

  /**
   * Obtener todos los partidos con filtros opcionales
   */
  getAll: async (tournamentId?: number, categoryId?: number): Promise<Match[]> => {
    try {
      let url = "/matches/public";
      const params = new URLSearchParams();

      if (tournamentId) params.append("tournamentId", tournamentId.toString());
      if (categoryId) params.append("categoryId", categoryId.toString());

      if (params.toString()) url += `?${params.toString()}`;

      const response = await api.get(url);
      return response.data as Match[];
    } catch (error) {
      console.error("Error al obtener partidos:", error);
      throw error;
    }
  },

  /**
   * Obtener partido por ID
   */
  getById: async (id: number): Promise<Match> => {
    try {
      const response = await api.get(`/matches/public/${id}`);
      return response.data as Match;
    } catch (error) {
      console.error("Error al obtener partido:", error);
      throw error;
    }
  },

  /**
   * Obtener partidos por torneo (endpoint dedicado)
   */
  getByTournament: async (tournamentId: number): Promise<Match[]> => {
    try {
      const response = await api.get(`/matches/tournament/${tournamentId}`);
      return response.data as Match[];
    } catch (error) {
      console.error("Error al obtener partidos del torneo:", error);
      return [];
    }
  },

  /**
   * Obtener partidos por torneo Y categoría
   */
  getByTournamentAndCategory: async (tournamentId: number, categoryId: number): Promise<Match[]> => {
    try {
      const response = await api.get(`/matches/tournament/${tournamentId}/category/${categoryId}`);
      return response.data as Match[];
    } catch (error) {
      console.error("Error al obtener partidos:", error);
      return [];
    }
  },

  // === ENDPOINTS PROTEGIDOS (Admin/Referee) ===

  /**
   * Crear nuevo partido
   * Requiere rol ADMIN, SUPER_ADMIN o REFEREE
   */
  create: async (data: MatchCreateDTO): Promise<Match> => {
    try {
      console.log('📤 Enviando partido al backend:', data);
      const response = await api.post("/matches", data);
      console.log('✅ Partido creado:', response.data);
      return response.data as Match;
    } catch (error) {
      console.error("❌ Error al crear partido:", error);
      throw error;
    }
  },

  /**
   * Actualizar partido existente
   * Requiere rol ADMIN, SUPER_ADMIN o REFEREE
   */
  update: async (id: number, data: MatchCreateDTO): Promise<Match> => {
    try {
      console.log(`📤 Actualizando partido ${id}:`, data);
      const response = await api.put(`/matches/${id}`, data);
      console.log('✅ Partido actualizado:', response.data);
      return response.data as Match;
    } catch (error) {
      console.error("❌ Error al actualizar partido:", error);
      throw error;
    }
  },

  /**
   * Eliminar partido
   * Requiere rol ADMIN o SUPER_ADMIN
   */
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/matches/${id}`);
    } catch (error) {
      console.error("Error al eliminar partido:", error);
      throw error;
    }
  },

  /**
   * Iniciar partido (cambiar estado a IN_PROGRESS)
   * Requiere rol REFEREE, ADMIN o SUPER_ADMIN
   */
  startMatch: async (id: number): Promise<Match> => {
    try {
      const response = await api.post(`/matches/${id}/start`);
      return response.data as Match;
    } catch (error) {
      console.error("Error al iniciar partido:", error);
      throw error;
    }
  },

  /**
   * Finalizar partido (cambiar estado a FINISHED)
   * Requiere rol REFEREE, ADMIN o SUPER_ADMIN
   */
  finishMatch: async (id: number): Promise<Match> => {
    try {
      const response = await api.post(`/matches/${id}/finish`);
      return response.data as Match;
    } catch (error) {
      console.error("Error al finalizar partido:", error);
      throw error;
    }
  },

  // === GESTIÓN DE RESULTADOS ===

  /**
   * Registrar resultado de partido
   */
  registerResult: async (data: MatchResult): Promise<MatchResult> => {
    try {
      const response = await api.post("/match-results", data);
      return response.data as MatchResult;
    } catch (error) {
      console.error("Error al registrar resultado:", error);
      throw error;
    }
  },

  /**
   * Obtener resultado de un partido
   */
  getResult: async (matchId: number): Promise<MatchResult | null> => {
    try {
      const response = await api.get(`/match-results/${matchId}`);
      return response.data ? (response.data as MatchResult) : null;
    } catch (error) {
      console.error("Error al obtener resultado:", error);
      return null;
    }
  },

  /**
   * Actualizar resultado existente
   */
  updateResult: async (data: MatchResult): Promise<MatchResult> => {
    try {
      const response = await api.put("/match-results", data);
      return response.data as MatchResult;
    } catch (error) {
      console.error("Error al actualizar resultado:", error);
      throw error;
    }
  },

  /**
   * Eliminar resultado
   */
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