// frontend-uptc/src/services/inscriptionPlayerService.ts
import api from "./api";

export interface InscriptionPlayerDTO {
  inscriptionId: number;
  playerId: number;
}

export interface PlayerSummary {
  id: number;
  fullName: string;
  documentNumber: string;
  studentCode?: string;
  institutionalEmail?: string;
}

export interface InscriptionWithLimit {
  id: number;
  teamName: string;
  category: {
    id: number;
    name: string;
    membersPerTeam: number; // 🔥 LÍMITE DE JUGADORES
  };
  playerCount: number; // 🔥 JUGADORES ACTUALES
}

const inscriptionPlayerService = {
  // Obtener inscripción con información de límite
  getInscriptionWithLimit: async (inscriptionId: number): Promise<InscriptionWithLimit> => {
    try {
      const response = await api.get(`/inscriptions/${inscriptionId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener inscripción:", error);
      throw error;
    }
  },

  // Obtener jugadores de una inscripción
  getByInscription: async (inscriptionId: number): Promise<PlayerSummary[]> => {
    try {
      const response = await api.get(`/inscriptions/${inscriptionId}/players`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener jugadores de inscripción:", error);
      throw error;
    }
  },

  // Añadir jugador a inscripción CON VALIDACIÓN
  addPlayer: async (inscriptionId: number, playerId: number): Promise<InscriptionPlayerDTO> => {
    try {
      const response = await api.post(`/inscriptions/${inscriptionId}/players`, {
        playerId
      });
      return response.data;
    } catch (error: any) {
      // El backend ya valida el límite, pero aquí manejamos el error
      if (error.response?.status === 400) {
        throw new Error(error.response.data.message || 'Se alcanzó el límite de jugadores');
      }
      throw error;
    }
  },

  // Eliminar jugador de inscripción
  removePlayer: async (inscriptionId: number, playerId: number): Promise<void> => {
    try {
      await api.delete(`/inscriptions/${inscriptionId}/players/${playerId}`);
    } catch (error) {
      console.error("Error al eliminar jugador:", error);
      throw error;
    }
  },

  // Buscar jugadores disponibles
  searchPlayers: async (query: string): Promise<PlayerSummary[]> => {
    try {
      const response = await api.post('/players/search', {
        fullName: query,
        isActive: true
      });
      return response.data.content || response.data;
    } catch (error) {
      console.error("Error al buscar jugadores:", error);
      return [];
    }
  }
};

export default inscriptionPlayerService;