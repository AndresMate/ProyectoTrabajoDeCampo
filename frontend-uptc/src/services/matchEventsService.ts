import api from "./api";

export interface MatchEvent {
  id?: number;
  matchId: number;
  playerId: number;
  type: 'GOAL' | 'YELLOW_CARD' | 'RED_CARD' | 'SUBSTITUTION' | 'OWN_GOAL' | 'PENALTY';
  minute: number;
  description?: string;
  teamId?: number;
  player?: {
    id: number;
    fullName: string;
  };
  team?: {
    id: number;
    name: string;
  };
  createdAt?: string;
}

export interface MatchEventCreateDTO {
  matchId: number;
  teamId: number;
  playerId: number;
  type: 'GOAL' | 'YELLOW_CARD' | 'RED_CARD' | 'SUBSTITUTION' | 'OWN_GOAL' | 'PENALTY';
  minute: number;
  description?: string;
}


const matchEventsService = {
  /**
   * Obtener eventos por partido
   */
  getByMatch: async (matchId: number): Promise<MatchEvent[]> => {
    try {
      const response = await api.get(`/match-events/match/${matchId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener eventos del partido:", error);
      throw error;
    }
  },

  /**
   * Crear un nuevo evento en un partido
   */
  create: async (data: MatchEventCreateDTO): Promise<MatchEvent> => {
    try {
      const response = await api.post("/match-events", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear evento:", error);
      throw error;
    }
  },

  /**
   * Eliminar un evento
   */
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/match-events/${id}`);
    } catch (error) {
      console.error("Error al eliminar evento:", error);
      throw error;
    }
  },
};

export default matchEventsService;
