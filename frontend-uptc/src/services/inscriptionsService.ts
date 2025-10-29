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
  availabilities: TeamAvailabilityDTO[];
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
  availabilities: TeamAvailabilityDTO[];
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
        } catch (error: any) {
            console.error("❌ Error al crear inscripción:", error.response?.data || error);
            throw error;
        }
    },

    // Obtener inscripción por ID
    getById: async (id: number): Promise<InscriptionResponseDTO> => {
        try {
            const response = await api.get(`/inscriptions/${id}`);
            return response.data;
        } catch (error: any) {
            console.error("❌ Error al obtener inscripción:", error);
            throw error;
        }
    },

    // ========================
    // VALIDACIONES
    // ========================

    // Validar si un nombre de equipo está disponible (no repetido)
    checkTeamName: async (tournamentId: number, teamName: string): Promise<boolean> => {
        try {
            const response = await api.get(`/inscriptions/check-team-name`, {
                params: {tournamentId, teamName}
            });
            return response.data?.isAvailable ?? false;
        } catch (error: any) {
            console.error("❌ Error al verificar nombre de equipo:", error);
            return false;
        }
    },

    // Validar si un club ya está inscrito en el torneo
    checkClub: async (tournamentId: number, clubId: number): Promise<boolean> => {
        try {
            const response = await api.get(`/inscriptions/check-club`, {
                params: {tournamentId, clubId}
            });
            return response.data?.isAvailable ?? false;
        } catch (error: any) {
            console.error("❌ Error al verificar club:", error);
            return false;
        }
    },

    // Validar si un jugador ya está inscrito en otro equipo
    checkPlayer: async (tournamentId: number, documentNumber: string): Promise<boolean> => {
        try {
            const response = await api.get(`/inscriptions/check-player`, {
                params: {tournamentId, documentNumber}
            });
            return response.data?.isAvailable ?? false;
        } catch (error: any) {
            console.error("❌ Error al verificar jugador:", error);
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
        } catch (error: any) {
            console.error("❌ Error al obtener inscripciones:", error);
            throw error;
        }
    },

    // Aprobar inscripción
    approve: async (id: number): Promise<InscriptionResponseDTO> => {
        try {
            const response = await api.post(`/inscriptions/admin/${id}/approve`);
            return response.data;
        } catch (error: any) {
            console.error("❌ Error al aprobar inscripción:", error);
            throw error;
        }
    },

    // Rechazar inscripción
    reject: async (id: number, reason: string): Promise<InscriptionResponseDTO> => {
        try {
            const response = await api.post(`/inscriptions/admin/${id}/reject`, {reason});
            return response.data;
        } catch (error: any) {
            console.error("❌ Error al rechazar inscripción:", error);
            throw error;
        }
    },


    findPlayerByDocument: async (documentNumber: string) => {
        try {
            const response = await api.get(`/players/document/${documentNumber}`);
            return response.data;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 404) {
                return null;
            }
            console.error("Error buscando jugador:", error);
            throw error;
        }
    }
};

export default inscriptionsService;