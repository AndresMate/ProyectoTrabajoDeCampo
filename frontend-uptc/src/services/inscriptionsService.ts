// frontend-uptc/src/services/inscriptionsService.ts - VERSIÓN ACTUALIZADA
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

export interface PlayerSummaryDTO {
  id: number;
  fullName: string;
  documentNumber: string;
  studentCode: string;
  institutionalEmail: string;
  idCardImageUrl?: string;
}

export interface InscriptionResponseDTO {
  id: number;
  teamName: string;
  delegateName: string;
  delegateEmail: string;
  delegatePhone: string;
  status: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt?: string;
  tournament: {
    id: number;
    name: string;
    status?: string;
  };
  category: {
    id: number;
    name: string;
    membersPerTeam: number;
  };
  club?: {
    id: number;
    name: string;
  };
  players: PlayerSummaryDTO[];
  playerCount: number;
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
      console.log(`📤 Solicitando inscripción ${id}...`);
      const response = await api.get(`/inscriptions/${id}`);
      console.log(`📥 Inscripción recibida:`, response.data);
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
      console.log('📤 Solicitando todas las inscripciones...');
      const response = await api.get("/inscriptions/admin");
      console.log('📥 Respuesta completa:', response);
      console.log('📥 Data recibida:', response.data);
      console.log('📥 Número de inscripciones:', response.data?.length);

      if (response.data && Array.isArray(response.data)) {
        console.log('✅ Primera inscripción:', response.data[0]);
        return response.data;
      }

      console.warn('⚠️ La respuesta no es un array:', response.data);
      return [];
    } catch (error: any) {
      console.error("❌ Error al obtener inscripciones:", error);
      console.error("❌ Error response:", error.response?.data);
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
  getPlayers: async (inscriptionId: number): Promise<PlayerSummaryDTO[]> => {
    try {
      console.log(`📤 Solicitando jugadores para inscripción ${inscriptionId}...`);
      const response = await api.get(`/inscriptions/${inscriptionId}/players`);
      console.log(`📥 Jugadores recibidos:`, response.data);

      if (Array.isArray(response.data)) {
        return response.data;
      }

      console.warn('⚠️ Los jugadores no son un array:', response.data);
      return [];
    } catch (error: any) {
      console.error("❌ Error al obtener jugadores:", error);
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