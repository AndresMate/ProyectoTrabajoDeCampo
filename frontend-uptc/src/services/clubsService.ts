// src/services/clubsService.ts
import api from "./api";
import axios from "axios";

export interface Club {
  id: number;
  name: string;
  description?: string;
  isActive?: boolean;
}

const clubsService = {
  getAll: async (): Promise<Club[]> => {
    try {
      const res = await api.get<Club[]>("/clubs/active");
      // 🔹 Aseguramos que solo devuelva id y name
      return (res.data ?? []).map((c) => ({
        id: c.id,
        name: c.name,
      }));
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener clubes:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener clubes:", error);
      }
      return [];
    }
  },
    async getActive() {
        try {
      const res = await api.get<Club[]>("/clubs/active");
      // 🔹 Aseguramos que solo devuelva id y name
      return (res.data ?? []).map((c) => ({
        id: c.id,
        name: c.name,
      }));
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("Error al obtener clubes:", error.response?.data ?? error.message);
      } else {
        console.error("Error al obtener clubes:", error);
      }
      return [];
    }
    }
};

export default clubsService;
