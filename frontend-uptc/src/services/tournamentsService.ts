// src/services/tournamentsService.ts
import api from "./api";

export const tournamentsService = {
  // 🔹 Obtener todos los torneos (paginados o no)
  getAll: async () => {
    try {
      const response = await api.get("/tournaments/public");
      // Si tu backend devuelve un objeto tipo { content: [...], totalPages: ... }
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

  // 🔹 Obtener torneos activos o en inscripción (para el carrusel)
  getActive: async () => {
    try {
      const response = await api.get("/tournaments/public/active");
      return response.data;
    } catch (error) {
      console.error("Error al obtener torneos activos:", error);
      throw error;
    }
  },
};
