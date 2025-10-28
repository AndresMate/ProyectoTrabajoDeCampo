import axios from 'axios';
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
    membersPerTeam: number;
  };
  playerCount: number;
}

const inscriptionPlayerService = {
  getInscriptionWithLimit: async (inscriptionId: number): Promise<InscriptionWithLimit> => {
    try {
      const response = await api.get(`/inscriptions/${inscriptionId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener inscripción:", error);
      throw error;
    }
  },

  getByInscription: async (inscriptionId: number): Promise<PlayerSummary[]> => {
    try {
      const response = await api.get(`/inscriptions/${inscriptionId}/players`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener jugadores de inscripción:", error);
      throw error;
    }
  },

  addPlayer: async (inscriptionId: number, playerId: number): Promise<InscriptionPlayerDTO> => {
    try {
      const response = await api.post(`/inscriptions/${inscriptionId}/players`, { playerId });
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status;
        const message = error.response?.data?.message ?? error.message;
        if (status === 400) {
          throw new Error(message || 'Se alcanzó el límite de jugadores');
        }
        // rethrow the original axios error to preserve details
        throw error;
      }

      if (error instanceof Error) {
        throw error;
      }

      throw new Error('Error inesperado al agregar jugador');
    }
  },

  removePlayer: async (inscriptionId: number, playerId: number): Promise<void> => {
    try {
      await api.delete(`/inscriptions/${inscriptionId}/players/${playerId}`);
    } catch (error) {
      console.error("Error al eliminar jugador:", error);
      throw error;
    }
  },

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