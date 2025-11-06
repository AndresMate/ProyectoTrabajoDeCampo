// src/services/authService.ts
import api from "./api";
import axios from "axios";

interface LoginCredentials {
  email: string;
  password: string;
}

interface LoginResponse {
  token: string;
  email: string;
  fullName: string;
  role: string;
  forcePasswordChange: boolean;
  userId: number;
}

interface CurrentUser {
  email: string;
  fullName: string;
  role: string;
  userId: number;
  forcePasswordChange: boolean;
}

interface PasswordChangeRequest {
  newPassword: string;
}

export const authService = {
  login: async (credentials: LoginCredentials): Promise<LoginResponse> => {
    try {
      console.log('🔐 Enviando credenciales al backend...');
      const response = await api.post("/auth/login", credentials);
      const data: LoginResponse = response.data;

      console.log('📦 Respuesta del servidor:', data);

      if (data.token) {
        localStorage.setItem("token", data.token);
        localStorage.setItem(
          "user",
          JSON.stringify({
            email: data.email,
            fullName: data.fullName,
            role: data.role,
            userId: data.userId,
            forcePasswordChange: data.forcePasswordChange,
          })
        );
        console.log("✅ Login exitoso. Token y datos guardados");
      } else {
        console.warn('⚠️ Respuesta sin token');
      }

      return data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error en login (Axios):", {
          status: error.response?.status,
          data: error.response?.data,
          message: error.message
        });
        throw error;
      }
      console.error("❌ Error en login (Desconocido):", error);
      throw new Error(String(error));
    }
  },

  forcePasswordChange: async (userId: number, newPassword: string): Promise<void> => {
    try {
      console.log(`🔑 Cambiando contraseña para usuario ${userId}`);
      await api.post(`/auth/force-password-change/${userId}`, {
        newPassword,
      });

      console.log("✅ Contraseña cambiada exitosamente");

      // Actualizar el estado del usuario en localStorage
      const user = authService.getCurrentUser();
      if (user) {
        const updatedUser = { ...user, forcePasswordChange: false };
        localStorage.setItem("user", JSON.stringify(updatedUser));
      }
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error al cambiar contraseña:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("❌ Error al cambiar contraseña:", error);
      throw new Error(String(error));
    }
  },

  logout: () => {
    console.log('👋 Cerrando sesión...');
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    window.location.href = "/login";
  },

  getToken: (): string | null => {
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("token");
      console.log('🎫 Token recuperado:', token ? 'Existe' : 'No existe');
      return token;
    }
    return null;
  },

  getCurrentUser: (): CurrentUser | null => {
    if (typeof window === "undefined") {
      console.log('⚠️ getCurrentUser llamado en servidor (SSR)');
      return null;
    }

    const userStr = localStorage.getItem("user");
    if (!userStr) {
      console.log('⚠️ No hay usuario en localStorage');
      return null;
    }

    try {
      const user = JSON.parse(userStr) as CurrentUser;
      console.log('👤 Usuario actual:', user.email, '- Rol:', user.role);
      return user;
    } catch (e) {
      console.error("❌ Error parseando usuario desde localStorage:", e);
      return null;
    }
  },

  isAuthenticated: (): boolean => {
    const authenticated = !!authService.getToken();
    console.log('🔒 Usuario autenticado:', authenticated);
    return authenticated;
  },

  hasRole: (role: string): boolean => {
    const user = authService.getCurrentUser();
    const hasRole = user?.role === role;
    console.log(`🎭 ¿Usuario tiene rol ${role}?:`, hasRole);
    return hasRole;
  },

  hasAnyRole: (roles: string[]): boolean => {
    const user = authService.getCurrentUser();
    const hasAny = roles.includes(user?.role ?? '');
    console.log(`🎭 ¿Usuario tiene alguno de los roles ${roles.join(', ')}?:`, hasAny);
    return hasAny;
  },
};