// services/api.ts - VERSIÓN CORREGIDA
import axios from "axios";

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

// Interceptor de response SOLO para logging
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
    return Promise.reject(error);
  }
);

export default api;