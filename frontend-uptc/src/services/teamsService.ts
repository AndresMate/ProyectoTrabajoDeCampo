import api from "./api";

export interface Team {
  id: number;
  name: string;
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
  isActive: boolean;
  createdAt?: string;
}

export interface TeamCreateDTO {
  name: string;
  tournamentId: number;
  categoryId: number;
  clubId?: number;
}

export interface TeamPlayer {
  id: number;
  player: {
    id: number;
    fullName: string;
    documentNumber: string;
  };
  jerseyNumber: number;
  position?: string;
  isCaptain: boolean;
  isActive: boolean;
}

const teamsService = {
  // Obtener todos los equipos
  getAll: async (page = 0, size = 50): Promise<any> => {
    try {
      const response = await api.get(`/teams?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener equipos:", error);
      throw error;
    }
  },

  // Obtener equipos por torneo
  getByTournament: async (tournamentId: number): Promise<Team[]> => {
    try {
      const response = await api.get(`/teams/tournament/${tournamentId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener equipos por torneo:", error);
      throw error;
    }
  },

  // Obtener equipos por torneo y categoría
  getByTournamentAndCategory: async (tournamentId: number, categoryId: number): Promise<Team[]> => {
    try {
      const response = await api.get(`/teams/tournament/${tournamentId}/category/${categoryId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener equipos por torneo y categoría:", error);
      throw error;
    }
  },

  // Obtener equipo por ID
  getById: async (id: number): Promise<any> => {
    try {
      const response = await api.get(`/teams/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener equipo:", error);
      throw error;
    }
  },

  // Crear equipo
  create: async (data: TeamCreateDTO): Promise<Team> => {
    try {
      const response = await api.post("/teams", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear equipo:", error);
      throw error;
    }
  },

  // Actualizar equipo
  update: async (id: number, data: Partial<TeamCreateDTO>): Promise<Team> => {
    try {
      const response = await api.put(`/teams/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar equipo:", error);
      throw error;
    }
  },

  // Desactivar equipo
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/teams/${id}`);
    } catch (error) {
      console.error("Error al desactivar equipo:", error);
      throw error;
    }
  },

  // === GESTIÓN DE ROSTER ===

  // Obtener roster del equipo
  getRoster: async (teamId: number): Promise<TeamPlayer[]> => {
    try {
      const response = await api.get(`/teams/${teamId}/roster`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener roster:", error);
      throw error;
    }
  },

  // Añadir jugador al equipo
  addPlayer: async (teamId: number, playerId: number, jerseyNumber: number, position?: string): Promise<TeamPlayer> => {
    try {
      const response = await api.post(`/teams/${teamId}/roster`, {
        playerId,
        jerseyNumber,
        position
      });
      return response.data;
    } catch (error) {
      console.error("Error al añadir jugador:", error);
      throw error;
    }
  },

  // Eliminar jugador del equipo
  removePlayer: async (teamId: number, playerId: number): Promise<void> => {
    try {
      await api.delete(`/teams/${teamId}/roster/player/${playerId}`);
    } catch (error) {
      console.error("Error al eliminar jugador:", error);
      throw error;
    }
  },

  // Asignar capitán
  setCaptain: async (teamId: number, playerId: number): Promise<TeamPlayer> => {
    try {
      const response = await api.post(`/teams/${teamId}/roster/captain/${playerId}`);
      return response.data;
    } catch (error) {
      console.error("Error al asignar capitán:", error);
      throw error;
    }
  }
};

export default teamsService;