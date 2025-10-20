// frontend-uptc/src/services/sanctionsService.ts
import api from "./api";

const sanctionsService = {
  getByPlayer: async (playerId: number) => {
    const response = await api.get(`/sanctions/player/${playerId}`);
    return response.data;
  },

  getByTeam: async (teamId: number) => {
    const response = await api.get(`/sanctions/team/${teamId}`);
    return response.data;
  },

  create: async (data: any) => {
    const response = await api.post('/sanctions', data);
    return response.data;
  },

  delete: async (id: number) => {
    await api.delete(`/sanctions/${id}`);
  }
};

export default sanctionsService;