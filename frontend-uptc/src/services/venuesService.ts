import api from "./api";

export interface Venue {
  id: number;
  name: string;
  address?: string;
  city?: string;
  capacity?: number;
  hasParking: boolean;
  description?: string;
  createdAt?: string;
}

export interface Scenario {
  id: number;
  name: string;
  venue: {
    id: number;
    name: string;
  };
  sportType?: string;
  surfaceType?: string;
  hasLighting: boolean;
  capacity?: number;
  description?: string;
}

export interface VenueCreateDTO {
  name: string;
  address?: string;
  city?: string;
  capacity?: number;
  hasParking?: boolean;
  description?: string;
}

export interface ScenarioCreateDTO {
  name: string;
  venueId: number;
  sportType?: string;
  surfaceType?: string;
  hasLighting?: boolean;
  capacity?: number;
  description?: string;
}

const venuesService = {
  // === VENUES (SEDES) ===

  // Buscar sedes por nombre - Público
  searchByName: async (name: string): Promise<Venue[]> => {
    try {
      const response = await api.get(`/venues/public/search?name=${name}`);
      return response.data;
    } catch (error) {
      console.error("Error al buscar sedes:", error);
      throw error;
    }
  },

  // Obtener todas las sedes
  getAll: async (page = 0, size = 50): Promise<any> => {
    try {
      const response = await api.get(`/venues?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener sedes:", error);
      throw error;
    }
  },

  // Obtener sede por ID
  getById: async (id: number): Promise<Venue> => {
    try {
      const response = await api.get(`/venues/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener sede:", error);
      throw error;
    }
  },

  // Crear sede
  create: async (data: VenueCreateDTO): Promise<Venue> => {
    try {
      const response = await api.post("/venues", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear sede:", error);
      throw error;
    }
  },

  // Actualizar sede
  update: async (id: number, data: Partial<VenueCreateDTO>): Promise<Venue> => {
    try {
      const response = await api.put(`/venues/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar sede:", error);
      throw error;
    }
  },

  // Eliminar sede
  delete: async (id: number): Promise<void> => {
    try {
      await api.delete(`/venues/${id}`);
    } catch (error) {
      console.error("Error al eliminar sede:", error);
      throw error;
    }
  },

  // === SCENARIOS (ESCENARIOS) ===

  // Obtener todos los escenarios
  getAllScenarios: async (page = 0, size = 50): Promise<any> => {
    try {
      const response = await api.get(`/scenarios?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener escenarios:", error);
      throw error;
    }
  },

  // Obtener escenarios por sede
  getScenariosByVenue: async (venueId: number): Promise<Scenario[]> => {
    try {
      const response = await api.get(`/scenarios/venue/${venueId}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener escenarios por sede:", error);
      throw error;
    }
  },

  // Obtener escenarios para partidos nocturnos
  getScenariosForNightGames: async (): Promise<Scenario[]> => {
    try {
      const response = await api.get("/scenarios/night-games");
      return response.data;
    } catch (error) {
      console.error("Error al obtener escenarios nocturnos:", error);
      throw error;
    }
  },

  // Obtener escenario por ID
  getScenarioById: async (id: number): Promise<Scenario> => {
    try {
      const response = await api.get(`/scenarios/${id}`);
      return response.data;
    } catch (error) {
      console.error("Error al obtener escenario:", error);
      throw error;
    }
  },

  // Crear escenario
  createScenario: async (data: ScenarioCreateDTO): Promise<Scenario> => {
    try {
      const response = await api.post("/scenarios", data);
      return response.data;
    } catch (error) {
      console.error("Error al crear escenario:", error);
      throw error;
    }
  },

  // Actualizar escenario
  updateScenario: async (id: number, data: Partial<ScenarioCreateDTO>): Promise<Scenario> => {
    try {
      const response = await api.put(`/scenarios/${id}`, data);
      return response.data;
    } catch (error) {
      console.error("Error al actualizar escenario:", error);
      throw error;
    }
  },

  // Eliminar escenario
  deleteScenario: async (id: number): Promise<void> => {
    try {
      await api.delete(`/scenarios/${id}`);
    } catch (error) {
      console.error("Error al eliminar escenario:", error);
      throw error;
    }
  }
};

export default venuesService;