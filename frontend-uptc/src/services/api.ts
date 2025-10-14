// src/services/api.ts
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api", // 👈 cambia el puerto si tu backend usa otro
  headers: {
    "Content-Type": "application/json",
  },
});

// ✅ Interceptor opcional para manejar errores globales
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error("Error en la petición:", error.response || error.message);
    return Promise.reject(error);
  }
);

export default api;
