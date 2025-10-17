import api from "./api";

export interface Team {
  id: number;
  name: string;
  clubId: number;
  clubName?: string;
  tournamentId: number;
  tournamentName?: string;
  categoryId: number;
  categoryName?: string;
  delegateName: string;
  delegatePhone: string;
  delegateEmail: string;
  isActive: boolean;
}

export const teamsService = {
  getAll: async (page = 0, size = 50) => {
    const response = await api.get(`/teams?page=${page}&size=${size}`);
    return response.data;
  },

  getById: async (id: number) => {
    const response = await api.get(`/teams/${id}`);
    return response.data;
  },

  getByTournament: async (tournamentId: number) => {
    const response = await api.get(`/teams/tournament/${tournamentId}`);
    return response.data;
  },

  getByTournamentAndCategory: async (tournamentId: number, categoryId: number) => {
    const response = await api.get(`/teams/tournament/${tournamentId}/category/${categoryId}`);
    return response.data;
  },

  create: async (data: Partial<Team>) => {
    const response = await api.post("/teams", data);
    return response.data;
  },

  update: async (id: number, data: Partial<Team>) => {
    const response = await api.put(`/teams/${id}`, data);
    return response.data;
  },

  delete: async (id: number) => {
    await api.delete(`/teams/${id}`);
  },

  getRoster: async (teamId: number) => {
    const response = await api.get(`/teams/${teamId}/roster`);
    return response.data;
  },

  addPlayer: async (teamId: number, data: { playerId: number; jerseyNumber: number; isCaptain?: boolean }) => {
    const response = await api.post(`/teams/${teamId}/roster`, data);
    return response.data;
  },

  removePlayer: async (teamId: number, playerId: number) => {
    await api.delete(`/teams/${teamId}/roster/player/${playerId}`);
  },

  setCaptain: async (teamId: number, playerId: number) => {
    const response = await api.post(`/teams/${teamId}/roster/captain/${playerId}`);
    return response.data;
  }
};

export default teamsService;