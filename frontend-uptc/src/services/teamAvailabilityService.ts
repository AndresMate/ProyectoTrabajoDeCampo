// frontend-uptc/src/services/teamAvailabilityService.ts
import api from "./api";

const teamAvailabilityService = {
  getByTeam: async (teamId: number) => {
    const response = await api.get(`/team-availability/team/${teamId}`);
    return response.data;
  },

  save: async (teamId: number, availabilities: any[], isNocturno: boolean) => {
    const response = await api.post(`/team-availability/team/${teamId}`, availabilities, {
      params: { isNocturno }
    });
    return response.data;
  }
};

export default teamAvailabilityService;