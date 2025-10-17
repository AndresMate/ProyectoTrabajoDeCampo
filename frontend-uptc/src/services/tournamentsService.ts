import api from "./api";

// ======================
// Interfaces de datos
// ======================

export interface Tournament {
  id: number;
  name: string;
  maxTeams: number;
  startDate: string;
  endDate: string;
  modality: "DIURNO" | "NOCTURNO";
  status: "PLANNING" | "OPEN_FOR_INSCRIPTION" | "IN_PROGRESS" | "FINISHED" | "CANCELLED";
  sport: {
    id: number;
    name: string;
  };
  category: {
    id: number;
    name: string;
  };
  createdBy: {
    id: number;
    fullName: string;
  };
  createdAt?: string;
  updatedAt?: string;
}

export interface TournamentCreateDTO {
  name: string;
  maxTeams: number;
  startDate: string;
  endDate: string;
  modality: "DIURNO" | "NOCTURNO";
  status: "PLANNING" | "OPEN_FOR_INSCRIPTION" | "IN_PROGRESS" | "FINISHED" | "CANCELLED";
  sportId: number;
  categoryId: number;
  createdById: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ======================
// Servicio principal
// ======================

export const tournamentsService = {
  // ======================
  // ENDPOINTS PÚBLICOS
  // ======================

  getAll: async (page = 0, size = 10): Promise<Tournament[]> => {
    try {
      const response = await api.get(`/tournaments/public?page=${page}&size=${size}`);
      return response.data.content || response.data;
    } catch (error) {
      console.error("Error al obtener torneos:", error);
      throw error;
    }
  },

  getAllPaginated: async (page = 0, size = 10): Promise<PageResponse<Tournament>> => {
    try {
      const response = await api.get(`/tournaments/public?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneos paginados:", error);
      throw error;
    }
  },

  getById: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.get(`/tournaments/public/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneo por ID:", error);
      throw error;
    }
  },

  getActive: async (): Promise<Tournament[]> => {
    try {
      const response = await api.get("/tournaments/public/active");
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneos activos:", error);
      throw error;
    }
  },

  // ======================
  // ENDPOINTS PRIVADOS
  // ======================

  create: async (data: TournamentCreateDTO): Promise<Tournament> => {
    try {
      console.log("📤 Enviando torneo al backend:", data);
      const response = await api.post("/tournaments", data);
      return response.data;
    } catch (error: any) {
      console.error("Error al crear torneo:", error.response?.data || error);
      throw error;
    }
  },

  // ⚠️ PATCH para edición parcial
  update: async (id: number | string, data: Partial<TournamentCreateDTO>): Promise<Tournament> => {
    try {
      console.log(`✏️ Actualizando torneo ${id} con datos:`, data);
      const response = await api.patch(`/tournaments/${id}`, data);
      return response.data;
    } catch (error: any) {
      console.error("Error al actualizar torneo:", error.response?.data || error);
      throw error;
    }
  },

  delete: async (id: number | string): Promise<void> => {
    try {
      await api.delete(`/tournaments/${id}`);
    } catch (error) {
      console.error("Error al eliminar torneo:", error);
      throw error;
    }
  },

  // ======================
  // CAMBIOS DE ESTADO
  // ======================

  startTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post(`/tournaments/${id}/start`);
      return response.data;
    } catch (error) {
      console.error("Error al iniciar torneo:", error);
      throw error;
    }
  },

  completeTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post(`/tournaments/${id}/complete`);
      return response.data;
    } catch (error) {
      console.error("Error al completar torneo:", error);
      throw error;
    }
  },

  cancelTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post(`/tournaments/${id}/cancel`);
      return response.data;
    } catch (error) {
      console.error("Error al cancelar torneo:", error);
      throw error;
    }
  },

  // ======================
  // RELACIONES
  // ======================

  getInscriptions: async (tournamentId: number | string): Promise<any[]> => {
    try {
      const response = await api.get(`/inscriptions?tournamentId=${tournamentId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener inscripciones:", error);
      return [];
    }
  },

  getMatches: async (tournamentId: number | string): Promise<any[]> => {
    try {
      const response = await api.get(`/matches/public?tournamentId=${tournamentId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener partidos:", error);
      return [];
    }
  },

  getStats: async (): Promise<any> => {
    try {
      const response = await api.get("/tournaments/stats");
      return response.data;
    } catch (error) {
      console.error("Error al obtener estadísticas:", error);
      throw error;
    }
  },
};

export default tournamentsService;
