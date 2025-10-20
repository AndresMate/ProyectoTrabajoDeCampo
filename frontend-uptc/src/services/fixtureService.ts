// frontend-uptc/src/services/fixtureService.ts
import api from "./api";

const fixtureService = {
  generate: async (tournamentId: number, categoryId: number, mode: string) => {
    const response = await api.post('/fixtures/generate', null, {
      params: { tournamentId, categoryId, mode }
    });
    return response.data;
  },

  delete: async (tournamentId: number, categoryId: number) => {
    const response = await api.delete('/fixtures', {
      params: { tournamentId, categoryId }
    });
    return response.data;
  }
};

export default fixtureService;