import api from "./api";

export interface Category {
  id: number;
  name: string;
  description?: string;
  sportId: number;
  sportName?: string;
  minAge?: number;
  maxAge?: number;
  gender?: string;
  isActive: boolean;
}

export interface CategoryCreateDTO {
  name: string;
  description?: string;
  sportId: number;
  minAge?: number;
  maxAge?: number;
  gender?: string;
}

const categoriesService = {
  // Obtener todas las categorías
  getAll: async (page = 0, size = 50): Promise<any> => {
    try {
      const response = await api.get(`/categories?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener categorías:", error);
      throw error;
    }
  },

  // Obtener categorías activas por deporte
  getActiveBySport: async (sportId: number): Promise<Category[]> => {
    try {
      const response = await api.get(`/categories/sport/${sportId}/active`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener categorías por deporte:", error);
      throw error;
    }
  },

  // Obtener categoría por ID
  getById: async (id: number): Promise<Category> => {
    try {
      const response = await api.get(`/categories/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener categoría:", error);
      throw error;
    }
  },

  // Crear categoría
  create: async (data: CategoryCreateDTO): Promise<Category> => {
    try {
      const response = await api.post("/categories", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear categoría:", error);
      throw error;
    }
  },

  // Actualizar categoría
  update: async (id: number, data: Partial<CategoryCreateDTO>): Promise<Category> => {
    try {
      const response = await api.put(`/categories/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar categoría:", error);
      throw error;
    }
  },

  // Desactivar categoría
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/categories/${id}`);
    } catch (error) {
      console.error("Error al desactivar categoría:", error);
      throw error;
    }
  }
};

export default categoriesService;