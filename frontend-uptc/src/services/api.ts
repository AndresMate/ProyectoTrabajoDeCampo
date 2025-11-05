// services/api.ts - VERSIÓN CORREGIDA
import axios from "axios";
import { toastAxiosError } from "@/utils/toast";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor para AGREGAR token a TODAS las requests
api.interceptors.request.use(
  (config) => {
    // Solo ejecutar en el cliente (navegador)
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("token");
      console.log('🔐 Interceptor - Token:', token ? 'PRESENTE' : 'AUSENTE');

      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        console.log('🔐 Interceptor - Authorization header agregado');
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor de response para logging y manejo de errores
api.interceptors.response.use(
  (response) => {
    console.log('✅ Response recibida:', response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error('❌ Error en response:', {
      status: error.response?.status,
      url: error.config?.url,
      headers: error.config?.headers
    });

    // Solo mostrar toast para errores que no sean de autenticación (401)
    // Los errores 401 se manejan en el componente de login
    if (error.response?.status !== 401) {
      // Verificar si el error no tiene skipToast flag (para errores que se manejan manualmente)
      if (!error.config?.skipToast) {
        toastAxiosError(error);
      }
    }

    return Promise.reject(error);
  }
);

export default api;