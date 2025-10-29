import api from "./api";

export interface Player {
  id: number;
  fullName: string;
  documentType: string;
  documentNumber: string;
  birthDate: string;
  email?: string;
  phone?: string;
  studentCode?: string;
  academicProgram?: string;
  address?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  bloodType?: string;
  isActive: boolean;
  createdAt?: string;
}

export interface PlayerCreateDTO {
  fullName: string;
  documentType: string;
  documentNumber: string;
  birthDate: string;
  email?: string;
  phone?: string;
  studentCode?: string;
  academicProgram?: string;
  address?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  bloodType?: string;
}

export interface PlayerFilterDTO {
  fullName?: string;
  documentNumber?: string;
  studentCode?: string;
  academicProgram?: string;
  isActive?: boolean;
}

const playersService = {
  // Obtener todos los jugadores
  getAll: async (page = 0, size = 50): Promise<any> => {
    try {
      const response = await api.get(`/players?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener jugadores:", error);
      throw error;
    }
  },

  // ✅ ALTERNATIVA CORRECTA usando el endpoint real
  getAvailablePlayers: async (): Promise<any[]> => {
  try {
    const response = await api.post("/players/search?page=0&size=100", {
      isActive: true
    });
    return response.data.content || response.data;
  } catch (error) {
    console.error("Error al obtener jugadores disponibles:", error);
    return [];
  }
  },

  // Buscar jugadores con filtros
  search: async (filters: PlayerFilterDTO, page = 0, size = 50): Promise<any> => {
    try {
      const response = await api.post(`/players/search?page=${page}&size=${size}`, filters);
      return response.data;
    } catch (error) {
      console.error("Error al buscar jugadores:", error);
      throw error;
    }
  },

  // Obtener jugador por ID
  getById: async (id: number): Promise<any> => {
    try {
      const response = await api.get(`/players/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener jugador:", error);
      throw error;
    }
  },

  // Crear jugador
  create: async (data: PlayerCreateDTO): Promise<Player> => {
    try {
      const response = await api.post("/players", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear jugador:", error);
      throw error;
    }
  },

  // Actualizar jugador
  update: async (id: number, data: Partial<PlayerCreateDTO>): Promise<Player> => {
    try {
      const response = await api.put(`/players/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar jugador:", error);
      throw error;
    }
  },

  // Desactivar jugador
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/players/${id}`);
    } catch (error) {
      console.error("Error al desactivar jugador:", error);
      throw error;
    }
  }
};

export default playersService;