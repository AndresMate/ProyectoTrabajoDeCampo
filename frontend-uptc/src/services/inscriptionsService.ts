// frontend-uptc/src/services/inscriptionsService.ts
import api from "./api";

export interface InscriptionDTO {
  id?: number;
  tournamentId: number;
  categoryId: number;
  clubId?: number;
  teamName: string;
  delegateName: string;
  delegateEmail: string;
  delegatePhone: string;
  status?: string;
  rejectionReason?: string;
  createdAt?: string;
}

export interface InscriptionResponseDTO {
  id: number;
  tournament: {
    id: number;
    name: string;
  };
  category: {
    id: number;
    name: string;
  };
  club?: {
    id: number;
    name: string;
  };
  teamName: string;
  delegateName: string;
  delegateEmail: string;
  delegatePhone: string;
  status: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt?: string;
}

const inscriptionsService = {
  // === ENDPOINTS PÚBLICOS ===

  // Crear inscripción
  create: async (data: InscriptionDTO): Promise<InscriptionResponseDTO> => {
    try {
      const response = await api.post("/inscriptions", data);
      return response.data;
    } catch (error: any) {
      console.error("Error al crear inscripción:", error);
      throw error;
    }
  },

  // Obtener inscripción por ID
  getById: async (id: number): Promise<InscriptionResponseDTO> => {
    try {
      const response = await api.get(`/inscriptions/${id}`);
      return response.data;
    } catch (error: any) {
      console.error("Error al obtener inscripción:", error);
      throw error;
    }
  },

  // Verificar disponibilidad de nombre de equipo
  checkTeamName: async (tournamentId: number, teamName: string): Promise<{ isAvailable: boolean }> => {
    try {
      const response = await api.get(`/inscriptions/check-team-name`, {
        params: { tournamentId, teamName }
      });
      return response.data;
    } catch (error: any) {
      console.error("Error al verificar nombre de equipo:", error);
      return { isAvailable: false };
    }
  },

  // Eliminar inscripción
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/inscriptions/${id}`);
    } catch (error: any) {
      console.error("Error al eliminar inscripción:", error);
      throw error;
    }
  },

  // === ENDPOINTS DE ADMINISTRACIÓN ===

  // Obtener todas las inscripciones (Admin)
  getAll: async (): Promise<InscriptionResponseDTO[]> => {
    try {
      const response = await api.get("/inscriptions/admin");
      return response.data;
    } catch (error: any) {
      console.error("Error al obtener inscripciones:", error);
      throw error;
    }
  },

  // Aprobar inscripción (Admin)
  approve: async (id: number): Promise<InscriptionResponseDTO> => {
    try {
      const response = await api.post(`/inscriptions/admin/${id}/approve`);
      return response.data;
    } catch (error: any) {
      console.error("Error al aprobar inscripción:", error);
      throw error;
    }
  },

  // Rechazar inscripción (Admin)
  reject: async (id: number, reason: string): Promise<InscriptionResponseDTO> => {
    try {
      const response = await api.post(`/inscriptions/admin/${id}/reject`, { reason });
      return response.data;
    } catch (error: any) {
      console.error("Error al rechazar inscripción:", error);
      throw error;
    }
  },

  // === GESTIÓN DE JUGADORES ===

  // Obtener jugadores de una inscripción
  getPlayers: async (inscriptionId: number): Promise<any[]> => {
    try {
      const response = await api.get(`/inscriptions/${inscriptionId}/players`);
      return response.data;
    } catch (error: any) {
      console.error("Error al obtener jugadores:", error);
      throw error;
    }
  },

  // Añadir jugador a inscripción
  addPlayer: async (inscriptionId: number, playerId: number): Promise<any> => {
    try {
      const response = await api.post(`/inscriptions/${inscriptionId}/players`, { playerId });
      return response.data;
    } catch (error: any) {
      console.error("Error al añadir jugador:", error);
      throw error;
    }
  },

  // Eliminar jugador de inscripción
  removePlayer: async (inscriptionId: number, playerId: number): Promise<void> => {
    try {
      await api.delete(`/inscriptions/${inscriptionId}/players/${playerId}`);
    } catch (error: any) {
      console.error("Error al eliminar jugador:", error);
      throw error;
    }
  }
};

export default inscriptionsService;