import api from "./api";

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: string;
  isActive: boolean;
  createdAt: string;
}

export interface UsersResponse {
  content: User[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const usersService = {
  getUsers: async (page = 0, size = 50): Promise<UsersResponse> => {
    try {
      // Debug: verificar token antes de hacer la request
      const token = localStorage.getItem('token');
      console.log('🔐 Token en usersService:', token ? 'PRESENTE' : 'AUSENTE');

      if (!token) {
        throw new Error('No token found in localStorage');
      }

      const response = await api.get(`/users`);
      console.log('✅ Users response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('❌ Error detallado en usersService:', {
        status: error.response?.status,
        message: error.response?.data?.message,
        url: error.config?.url,
        headers: error.config?.headers
      });
      throw error;
    }
  },
};

export default usersService;