import api from "./api";

export const tournamentsService = {
  // 🔹 Obtener todos los torneos (paginados)
  getAll: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`/tournaments/public?page=${page}&size=${size}`);
      return response.data.content || response.data;
    } catch (error) {
      console.error("Error al obtener torneos:", error);
      throw error;
    }
  },

  // 🔹 Obtener torneo por ID
  getById: async (id: number | string) => {
    try {
      const response = await api.get(`/tournaments/public/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneo por ID:", error);
      throw error;
    }
  },

  // 🔹 Obtener torneos activos
  getActive: async () => {
    try {
      const response = await api.get("/tournaments/public/active");
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneos activos:", error);
      throw error;
    }
  },

  // 🔹 Crear nuevo torneo (Admin)
  create: async (data: any) => {
    try {
      const response = await api.post("/tournaments", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear torneo:", error);
      throw error;
    }
  },

  // 🔹 Actualizar torneo (Admin)
  update: async (id: number | string, data: any) => {
    try {
      const response = await api.put(`/tournaments/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar torneo:", error);
      throw error;
    }
  },

  // 🔹 Eliminar torneo (Admin)
  delete: async (id: number | string) => {
    try {
      await api.delete(`/tournaments/${id}`);
    } catch (error) {
      console.error("Error al eliminar torneo:", error);
      throw error;
    }
  },

  // 🔹 Cambiar estado del torneo
  updateStatus: async (id: number | string, status: string) => {
    try {
      const response = await api.patch(`/tournaments/${id}/status`, { status });
      return response.data;
    } catch (error) {
      console.error("Error al cambiar estado:", error);
      throw error;
    }
  },

  // 🔹 Obtener inscripciones de un torneo
  getInscriptions: async (tournamentId: number | string) => {
    try {
      const response = await api.get(`/tournaments/${tournamentId}/inscriptions`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener inscripciones:", error);
      throw error;
    }
  },

  // 🔹 Obtener partidos de un torneo
  getMatches: async (tournamentId: number | string) => {
    try {
      const response = await api.get(`/tournaments/${tournamentId}/matches`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener partidos:", error);
      throw error;
    }
  }
};