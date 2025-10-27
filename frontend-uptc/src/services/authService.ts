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

export const authService = {
  login: async (credentials: LoginCredentials): Promise<LoginResponse> => {
    try {
      const response = await api.post("/auth/login", credentials);
      const data: LoginResponse = response.data;

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
        console.log("✅ Login exitoso:", data);
      }

      return data;
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        console.error("❌ Error en login:", error.response?.data ?? error.message);
        throw error;
      }
      console.error("❌ Error en login:", error);
      throw new Error(String(error));
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

  getCurrentUser: (): CurrentUser | null => {
    if (typeof window === "undefined") return null;
    const userStr = localStorage.getItem("user");
    if (!userStr) return null;
    try {
        return JSON.parse(userStr) as CurrentUser;
    } catch (e) {
      console.error("❌ Error parseando usuario desde localStorage:", e);
      return null;
    }
  },

  isAuthenticated: (): boolean => {
    return !!authService.getToken();
  },

  hasRole: (role: string): boolean => {
    const user = authService.getCurrentUser();
    return user?.role === role;
  },
};
