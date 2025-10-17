// src/services/authService.ts
import api from "./api";

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

export const authService = {
  login: async (credentials: LoginCredentials): Promise<LoginResponse> => {
    try {
      const response = await api.post("/auth/login", credentials);
      const data = response.data;

      if (data.token) {
        localStorage.setItem("token", data.token);
        localStorage.setItem(
          "user",
          JSON.stringify({
            email: data.email,
            fullName: data.fullName,
            role: data.role,
            userId: data.userId, // ✅ ahora sí lo guardamos
            forcePasswordChange: data.forcePasswordChange,
          })
        );
        console.log("✅ Login exitoso:", data);
      }

      return data;
    } catch (error: any) {
      console.error("❌ Error en login:", error.response?.data);
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    window.location.href = "/login";
  },

  getToken: (): string | null => {
    if (typeof window !== "undefined") {
      return localStorage.getItem("token");
    }
    return null;
  },

  getCurrentUser: () => {
    if (typeof window !== "undefined") {
      const userStr = localStorage.getItem("user");
      return userStr ? JSON.parse(userStr) : null;
    }
    return null;
  },

  isAuthenticated: (): boolean => {
    return !!authService.getToken();
  },

  hasRole: (role: string): boolean => {
    const user = authService.getCurrentUser();
    return user?.role === role;
  },
};
