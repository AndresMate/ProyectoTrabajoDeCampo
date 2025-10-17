import api from "./api";

export interface Club {
  id: number;
  name: string;
  description?: string;
  foundedYear?: number;
  contactEmail?: string;
  contactPhone?: string;
  isActive: boolean;
  createdAt?: string;
}

export interface ClubCreateDTO {
  name: string;
  description?: string;
  foundedYear?: number;
  contactEmail?: string;
  contactPhone?: string;
}

const clubsService = {
  // Obtener todos los clubes
  getAll: async (page = 0, size = 50): Promise<any> => {
    try {
      const response = await api.get(`/clubs?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener clubes:", error);
      throw error;
    }
  },

  // Obtener clubes activos
  getActive: async (): Promise<Club[]> => {
    try {
      const response = await api.get("/clubs/active");
      return response.data;
    } catch (error) {
      console.error("Error al obtener clubes activos:", error);
      throw error;
    }
  },

  // Obtener club por ID
  getById: async (id: number): Promise<Club> => {
    try {
      const response = await api.get(`/clubs/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener club:", error);
      throw error;
    }
  },

  // Crear club
  create: async (data: ClubCreateDTO): Promise<Club> => {
    try {
      const response = await api.post("/clubs", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear club:", error);
      throw error;
    }
  },

  // Actualizar club
  update: async (id: number, data: Partial<ClubCreateDTO>): Promise<Club> => {
    try {
      const response = await api.put(`/clubs/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar club:", error);
      throw error;
    }
  },

  // Desactivar club
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/clubs/${id}`);
    } catch (error) {
      console.error("Error al desactivar club:", error);
      throw error;
    }
  }
};

export default clubsService;