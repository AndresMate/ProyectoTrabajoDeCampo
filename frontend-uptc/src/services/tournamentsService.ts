import api from "./api";

export interface Tournament {
  id: number;
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  registrationDeadline?: string;
  status: string;
  sport: {
    id: number;
    name: string;
  };
  category?: {
    id: number;
    name: string;
  };
  createdBy?: {
    id: number;
    fullName: string;
  };
}

export interface TournamentCreateDTO {
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  registrationDeadline?: string;
  sportId: number;
  categoryIds?: number[];
  rules?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const tournamentsService = {
  // Obtener todos los torneos (paginados) - Público
  getAll: async (page = 0, size = 10): Promise<Tournament[]> => {
    try {
      const response = await api.get(`/tournaments/public?page=${page}&size=${size}`);
      return response.data.content || response.data;
    } catch (error) {
      console.error("Error al obtener torneos:", error);
      throw error;
    }
  },

  // Obtener torneos con paginación completa
  getAllPaginated: async (page = 0, size = 10): Promise<PageResponse<Tournament>> => {
    try {
      const response = await api.get(`/tournaments/public?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneos paginados:", error);
      throw error;
    }
  },

  // Obtener torneo por ID - Público
  getById: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.get(`/tournaments/public/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneo por ID:", error);
      throw error;
    }
  },

  // Obtener torneos activos - Público
  getActive: async (): Promise<Tournament[]> => {
    try {
      const response = await api.get("/tournaments/public/active");
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneos activos:", error);
      throw error;
    }
  },

  // Buscar torneos con filtros - Público
  search: async (filters: any, page = 0, size = 10): Promise<PageResponse<Tournament>> => {
    try {
      const response = await api.post(`/tournaments/public/search?page=${page}&size=${size}`, filters);
      return response.data;
    } catch (error) {
      console.error("Error al buscar torneos:", error);
      throw error;
    }
  },

  // === ENDPOINTS PROTEGIDOS (Requieren autenticación) ===

  // Crear nuevo torneo (Admin)
  create: async (data: TournamentCreateDTO): Promise<Tournament> => {
    try {
      const response = await api.post("/tournaments", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear torneo:", error);
      throw error;
    }
  },

  // Actualizar torneo (Admin)
  update: async (id: number | string, data: Partial<TournamentCreateDTO>): Promise<Tournament> => {
    try {
      const response = await api.put(`/tournaments/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar torneo:", error);
      throw error;
    }
  },

  // Eliminar torneo (Admin)
  delete: async (id: number | string): Promise<void> => {
    try {
      await api.delete(`/tournaments/${id}`);
    } catch (error) {
      console.error("Error al eliminar torneo:", error);
      throw error;
    }
  },

  // Iniciar torneo (Admin)
  startTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post(`/tournaments/${id}/start`);
      return response.data;
    } catch (error) {
      console.error("Error al iniciar torneo:", error);
      throw error;
    }
  },

  // Completar torneo (Admin)
  completeTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post(`/tournaments/${id}/complete`);
      return response.data;
    } catch (error) {
      console.error("Error al completar torneo:", error);
      throw error;
    }
  },

  // Cancelar torneo (Admin)
  cancelTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post(`/tournaments/${id}/cancel`);
      return response.data;
    } catch (error) {
      console.error("Error al cancelar torneo:", error);
      throw error;
    }
  },

  // Obtener inscripciones de un torneo
  getInscriptions: async (tournamentId: number | string): Promise<any[]> => {
    try {
      const response = await api.get(`/inscriptions?tournamentId=${tournamentId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener inscripciones:", error);
      return [];
    }
  },

  // Obtener partidos de un torneo
  getMatches: async (tournamentId: number | string): Promise<any[]> => {
    try {
      const response = await api.get(`/matches/public?tournamentId=${tournamentId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener partidos:", error);
      return [];
    }
  },

  // Obtener estadísticas de torneos (Admin)
  getStats: async (): Promise<any> => {
    try {
      const response = await api.get("/tournaments/stats");
      return response.data;
    } catch (error) {
      console.error("Error al obtener estadísticas:", error);
      throw error;
    }
  }
};export default tournamentsService;