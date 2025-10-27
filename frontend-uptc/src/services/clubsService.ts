// src/services/clubsService.ts
import api from "./api";

export interface Club {
  id: number;
  name: string;
  description?: string;
  isActive?: boolean;
}

const clubsService = {
  // Obtener todos los clubes activos
  getAll: async (): Promise<Club[]> => {
    try {
      const res = await api.get("/clubs/active");
      // 🔹 Aseguramos que solo devuelva id y name
      return (res.data || []).map((c: any) => ({
        id: c.id,
        name: c.name,
      }));
    } catch (e) {
      console.error("Error al obtener clubes:", e);
      return [];
    }
  },
};

export default clubsService;
