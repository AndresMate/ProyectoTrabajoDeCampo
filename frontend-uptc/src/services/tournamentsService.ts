import api from "./api";
import axios from "axios";

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
    membersPerTeam: number;
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

// Tipos adicionales para evitar `any`
export interface InscriptionSummary {
  id: number;
  teamName: string;
  clubName?: string;
  playerCount?: number;
}

export interface MatchDTO {
  id: number;
  homeTeam: string;
  awayTeam: string;
  date?: string;
  round?: string;
}

export type StatsDTO = Record<string, unknown>;

// ======================
// Servicio principal
// ======================

export const tournamentsService = {
  // ======================
  // ENDPOINTS PÚBLICOS
  // ======================

  getAll: async (page = 0, size = 10): Promise<Tournament[]> => {
    try {
      const response = await api.get<PageResponse<Tournament> | Tournament[]>(`/tournaments/public?page=${page}&size=${size}`);
      const data = response.data;
      if (Array.isArray(data)) return data;
      return data.content ?? [];
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener torneos:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener torneos:", error);
      }
      throw error;
    }
  },

  getAllPaginated: async (page = 0, size = 10): Promise<PageResponse<Tournament>> => {
    try {
      const response = await api.get<PageResponse<Tournament>>(`/tournaments/public?page=${page}&size=${size}`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener torneos paginados:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener torneos paginados:", error);
      }
      throw error;
    }
  },

  getById: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.get<Tournament>(`/tournaments/public/${id}`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener torneo por ID:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener torneo por ID:", error);
      }
      throw error;
    }
  },

  getActive: async (): Promise<Tournament[]> => {
    try {
      const response = await api.get<Tournament[]>("/tournaments/public/active");
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener torneos activos:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener torneos activos:", error);
      }
      throw error;
    }
  },

  // ======================
  // ENDPOINTS PRIVADOS
  // ======================

  create: async (data: TournamentCreateDTO): Promise<Tournament> => {
    try {
      console.log("📤 Enviando torneo al backend:", data);
      const response = await api.post<Tournament>("/tournaments", data);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al crear torneo:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("Error al crear torneo:", error);
      throw new Error(String(error));
    }
  },

  // ⚠️ PATCH para edición parcial
  update: async (id: number | string, data: Partial<TournamentCreateDTO>): Promise<Tournament> => {
    try {
      console.log(`✏️ Actualizando torneo ${id} con datos:`, data);
      const response = await api.patch<Tournament>(`/tournaments/${id}`, data);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al actualizar torneo:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("Error al actualizar torneo:", error);
      throw new Error(String(error));
    }
  },

  delete: async (id: number | string): Promise<void> => {
    try {
      await api.delete(`/tournaments/${id}`);
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al eliminar torneo:", error.response?.data ?? error.message);
      } else {
        console.error("Error al eliminar torneo:", error);
      }
      throw error;
    }
  },

  // ======================
  // CAMBIOS DE ESTADO
  // ======================

  startTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post<Tournament>(`/tournaments/${id}/start`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al iniciar torneo:", error.response?.data ?? error.message);
      } else {
        console.error("Error al iniciar torneo:", error);
      }
      throw error;
    }
  },

  completeTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post<Tournament>(`/tournaments/${id}/complete`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al completar torneo:", error.response?.data ?? error.message);
      } else {
        console.error("Error al completar torneo:", error);
      }
      throw error;
    }
  },

  cancelTournament: async (id: number | string): Promise<Tournament> => {
    try {
      const response = await api.post<Tournament>(`/tournaments/${id}/cancel`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al cancelar torneo:", error.response?.data ?? error.message);
      } else {
        console.error("Error al cancelar torneo:", error);
      }
      throw error;
    }
  },

  // ======================
  // RELACIONES
  // ======================

  async getInscriptions(tournamentId: number | string): Promise<InscriptionSummary[]> {
    try {
      const response = await api.get<InscriptionSummary[]>(`/inscriptions/tournament/${tournamentId}/approved`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener inscripciones:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener inscripciones:", error);
      }
      return [];
    }
  },

  getMatches: async (tournamentId: number | string): Promise<MatchDTO[]> => {
    try {
      const response = await api.get<MatchDTO[]>(`/matches/public?tournamentId=${tournamentId}`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener partidos:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener partidos:", error);
      }
      return [];
    }
  },

  getStats: async (): Promise<StatsDTO> => {
    try {
      const response = await api.get<StatsDTO>("/tournaments/stats");
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener estadísticas:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener estadísticas:", error);
      }
      throw error;
    }
  },
};

export default tournamentsService;
