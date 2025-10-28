import api from "./api";

const teamsService = {
  // 🔹 Obtener todos los equipos (usa paginación del backend)
  getAll: async () => {
    const response = await api.get("/teams");
    // Si la API usa paginación, devolver el contenido directamente
    return response.data.content || response.data;
  },

  // 🔹 Obtener un equipo por ID (incluye detalles y estadísticas)
  getById: async (teamId: number) => {
    const response = await api.get(`/teams/${teamId}`);
    return response.data;
  },

  // 🔹 Obtener todos los equipos sin paginación (opcional)
  getAllList: async () => {
    const response = await api.get("/teams/all");
    return response.data;
  },

  // 🔹 Crear un nuevo equipo
  create: async (teamData: any) => {
    const response = await api.post("/teams", teamData);
    return response.data;
  },

  // 🔹 Actualizar equipo existente
  update: async (id: number, teamData: any) => {
    const response = await api.put(`/teams/${id}`, teamData);
    return response.data;
  },

  // 🔹 Eliminar o desactivar un equipo (soft delete)
  delete: async (id: number) => {
    await api.delete(`/teams/${id}`);
  },

  // 🔹 Obtener el roster (jugadores del equipo)
  getRoster: async (teamId: number) => {
    try {
      const response = await api.get(`/teams/${teamId}/roster`);
      return response.data;
    } catch (error: any) {
      console.error("❌ Error al obtener roster:", error.response?.data || error);
      throw error;
    }
  },

  // 🔹 Asignar capitán
  setCaptain: async (teamId: number, playerId: number) => {
    const response = await api.post(`/teams/${teamId}/set-captain`, { playerId });
    return response.data;
  },

  // 🔹 Eliminar jugador del equipo
  removePlayer: async (teamId: number, playerId: number) => {
    const response = await api.delete(`/teams/${teamId}/players/${playerId}`);
    return response.data;
  },
};

export default teamsService;
