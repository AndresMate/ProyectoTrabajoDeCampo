import api from "./api";
import axios from "axios";
// ========================
// Interfaces principales
// ========================

export interface PlayerInscriptionDTO {
  fullName: string;
  documentNumber: string;
  studentCode: string;
  institutionalEmail: string;
  idCardPhotoUrl: string;
}

export interface TeamAvailabilityDTO {
  dayOfWeek: string; // "MONDAY"
  startTime: string; // "11:00"
  endTime: string;   // "12:00"
}

export interface InscriptionCreateDTO {
  tournamentId: number;
  categoryId: number;
  clubId?: number;
  teamName: string;
  delegatePhone: string;
  delegateIndex: number; // posición del jugador delegado
  players: PlayerInscriptionDTO[];
  availability: TeamAvailabilityDTO[];
}

export interface PlayerSummaryDTO {
  id: number;
  fullName: string;
  documentNumber: string;
  studentCode: string;
  institutionalEmail: string;
  idCardImageUrl?: string;
}

export interface InscriptionResponseDTO {
  id: number;
  teamName: string;
  delegateName: string;
  delegateEmail: string;
  delegatePhone: string;
  status: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt?: string;
  tournament: {
    id: number;
    name: string;
    status?: string;
  };
  category: {
    id: number;
    name: string;
    membersPerTeam: number;
  };
  club?: {
    id: number;
    name: string;
  };
  players: PlayerSummaryDTO[];
  playerCount: number;
}

// ========================
// Servicio principal
// ========================

const inscriptionsService = {
  // Crear inscripción completa (jugadores + disponibilidad)
  create: async (data: InscriptionCreateDTO): Promise<InscriptionResponseDTO> => {
    try {
      console.log("📤 Enviando inscripción al backend:", data);
      const response = await api.post("/inscriptions", data);
      console.log("📥 Respuesta inscripción:", response.data);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al crear inscripción:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("❌ Error al crear inscripción:", error);
      throw new Error(String(error));
    }
  },

  // Obtener inscripción por ID
  getById: async (id: number): Promise<InscriptionResponseDTO> => {
    try {
      const response = await api.get(`/inscriptions/${id}`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al obtener inscripción:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("❌ Error al obtener inscripción:", error);
      throw new Error(String(error));
    }
  },

  // ========================
  // VALIDACIONES
  // ========================

  // Validar si un nombre de equipo está disponible (no repetido)
  checkTeamName: async (tournamentId: number, teamName: string): Promise<boolean> => {
    try {
      const response = await api.get(`/inscriptions/check-team-name`, {
        params: { tournamentId, teamName }
      });
      return response.data?.isAvailable ?? false;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al verificar nombre de equipo:", error.response?.data ?? error.message);
      } else {
        console.error("❌ Error al verificar nombre de equipo:", error);
      }
      return false;
    }
  },

  // Validar si un club ya está inscrito en el torneo
  checkClub: async (tournamentId: number, clubId: number): Promise<boolean> => {
    try {
      const response = await api.get(`/inscriptions/check-club`, {
        params: { tournamentId, clubId }
      });
      return response.data?.isAvailable ?? false;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al verificar club:", error.response?.data ?? error.message);
      } else {
        console.error("❌ Error al verificar club:", error);
      }
      return false;
    }
  },

  // Validar si un jugador ya está inscrito en otro equipo
  checkPlayer: async (tournamentId: number, documentNumber: string): Promise<boolean> => {
    try {
      const response = await api.get(`/inscriptions/check-player`, {
        params: { tournamentId, documentNumber }
      });
      return response.data?.isAvailable ?? false;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al verificar jugador:", error.response?.data ?? error.message);
      } else {
        console.error("❌ Error al verificar jugador:", error);
      }
      return false;
    }
  },

  // ========================
  // ADMINISTRACIÓN
  // ========================

  // Obtener todas las inscripciones (solo admin)
  getAll: async (): Promise<InscriptionResponseDTO[]> => {
    try {
      const response = await api.get("/inscriptions/admin");
      return Array.isArray(response.data) ? response.data : [];
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al obtener inscripciones:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("❌ Error al obtener inscripciones:", error);
      throw new Error(String(error));
    }
  },

  // Aprobar inscripción
  approve: async (id: number): Promise<InscriptionResponseDTO> => {
    try {
      const response = await api.post(`/inscriptions/admin/${id}/approve`);
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al aprobar inscripción:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("❌ Error al aprobar inscripción:", error);
      throw new Error(String(error));
    }
  },

    // Eliminar inscripción
  delete: async (id: number): Promise<void> => {
      try {
        await api.delete(`/inscriptions/${id}`);
      } catch (error: unknown) {
        if (axios.isAxiosError(error)) {
          console.error("❌ Error al eliminar inscripción:", error.response?.data ?? error.message);
          throw error;
        }
        console.error("❌ Error al eliminar inscripción:", error);
        throw new Error(String(error));
      }
  },

  // Rechazar inscripción
  reject: async (id: number, reason: string): Promise<InscriptionResponseDTO> => {
    try {
      const response = await api.post(`/inscriptions/admin/${id}/reject`, { reason });
      return response.data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al rechazar inscripción:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("❌ Error al rechazar inscripción:", error);
      throw new Error(String(error));
    }
  }
};

export default inscriptionsService;