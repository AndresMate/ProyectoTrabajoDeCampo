import api from "./api";

const teamsService = {
  getAll: async () => {
    const response = await api.get("/teams");
    return response.data.content || response.data;
  },

  getById: async (teamId: number) => {
    const response = await api.get(`/teams/${teamId}`);
    return response.data;
  },

  getAllList: async () => {
    const response = await api.get("/teams/all");
    return response.data;
  },

  create: async (teamData: any) => {
    const response = await api.post("/teams", teamData);
    return response.data;
  },

  update: async (id: number, teamData: any) => {
    const response = await api.put(`/teams/${id}`, teamData);
    return response.data;
  },

  delete: async (id: number) => {
    await api.delete(`/teams/${id}`);
  },

  // ✅ CORREGIDO - usar el endpoint correcto del roster
  getRoster: async (teamId: number) => {
    try {
      const response = await api.get(`/teams/${teamId}/roster`);
      return response.data;
    } catch (error: any) {
      console.error("❌ Error al obtener roster:", error.response?.data || error);
      throw error;
    }
  },

  // ✅ CORREGIDO - endpoint correcto
  setCaptain: async (teamId: number, playerId: number) => {
    const response = await api.post(`/teams/${teamId}/roster/captain/${playerId}`);
    return response.data;
  },

  // ✅ CORREGIDO - endpoint correcto
  removePlayer: async (teamId: number, playerId: number) => {
    await api.delete(`/teams/${teamId}/roster/player/${playerId}`);
  },

  // ✅ AGREGAR - endpoint que faltaba
  addPlayerToRoster: async (teamId: number, playerData: any) => {
    const response = await api.post(`/teams/${teamId}/roster`, playerData);
    return response.data;
  },

  getByTournamentAndCategory: async (tournamentId: number, categoryId: number) => {
    const response = await api.get(`/teams/tournament/${tournamentId}/category/${categoryId}`);
    return response.data;
  },
};

export default teamsService;