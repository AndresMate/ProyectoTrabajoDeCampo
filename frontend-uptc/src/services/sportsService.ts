import api from "./api";

export interface Sport {
  id: number;
  name: string;
  description?: string;
  isActive: boolean;
  createdAt?: string;
}

export interface SportCreateDTO {
  name: string;
  description?: string;
  isActive?: boolean;
}

const sportsService = {
  // === ENDPOINTS PÚBLICOS ===

  // Obtener deportes activos
  getActive: async (): Promise<Sport[]> => {
    try {
      const response = await api.get("/sports/public/active");
      return response.data;
    } catch (error) {
      console.error("Error al obtener deportes activos:", error);
      throw error;
    }
  },

  // === ENDPOINTS PROTEGIDOS (Admin) ===

  // Obtener todos los deportes
  getAll: async (page = 0, size = 50): Promise<any> => {
    try {
      const response = await api.get(`/sports?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener deportes:", error);
      throw error;
    }
  },

  // Obtener deporte por ID
  getById: async (id: number): Promise<Sport> => {
    try {
      const response = await api.get(`/sports/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener deporte:", error);
      throw error;
    }
  },

  // Crear deporte
  create: async (data: SportCreateDTO): Promise<Sport> => {
    try {
      const response = await api.post("/sports", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear deporte:", error);
      throw error;
    }
  },

  // Actualizar deporte
  update: async (id: number, data: Partial<SportCreateDTO>): Promise<Sport> => {
    try {
      const response = await api.put(`/sports/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar deporte:", error);
      throw error;
    }
  },

  // Desactivar deporte
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/sports/${id}`);
    } catch (error) {
      console.error("Error al desactivar deporte:", error);
      throw error;
    }
  },

  // Reactivar deporte
  reactivate: async (id: number): Promise<Sport> => {
    try {
      const sport = await sportsService.getById(id);
      const response = await api.put(`/sports/${id}`, {
        name: sport.name,
        description: sport.description,
        isActive: true
      });
      return response.data;
    } catch (error) {
      console.error("Error al reactivar deporte:", error);
      throw error;
    }
  }
};

export default sportsService;