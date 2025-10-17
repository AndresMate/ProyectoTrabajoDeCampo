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

export interface UserCreateDTO {
  fullName: string;
  email: string;
  role: string;
  password?: string;
}

export interface UserUpdateDTO {
  fullName?: string;
  email?: string;
  role?: string;
}

export interface ChangePasswordDTO {
  currentPassword: string;
  newPassword: string;
}

const usersService = {
  // Obtener todos los usuarios (paginado)
  getUsers: async (page = 0, size = 50): Promise<UsersResponse> => {
    try {
      const response = await api.get(`/users?page=${page}&size=${size}`);
      console.log('✅ Users response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('❌ Error en getUsers:', error.response?.data);
      throw error;
    }
  },

  // Obtener usuarios activos
  getActiveUsers: async (): Promise<User[]> => {
    try {
      const response = await api.get('/users/active');
      return response.data;
    } catch (error: any) {
      console.error('❌ Error en getActiveUsers:', error);
      throw error;
    }
  },

  // Obtener usuario por ID
  getUserById: async (id: number): Promise<User> => {
    try {
      const response = await api.get(`/users/${id}`);
      return response.data;
    } catch (error: any) {
      console.error('❌ Error en getUserById:', error);
      throw error;
    }
  },

  // Obtener usuarios por rol
  getUsersByRole: async (role: string): Promise<User[]> => {
    try {
      const response = await api.get(`/users/role/${role}`);
      return response.data;
    } catch (error: any) {
      console.error('❌ Error en getUsersByRole:', error);
      throw error;
    }
  },

  // Crear usuario
  createUser: async (data: UserCreateDTO): Promise<any> => {
    try {
      const response = await api.post('/users', data);
      return response.data;
    } catch (error: any) {
      console.error('❌ Error en createUser:', error);
      throw error;
    }
  },

  // Actualizar usuario
  updateUser: async (id: number, data: UserUpdateDTO): Promise<User> => {
    try {
      const response = await api.put(`/users/${id}`, data);
      return response.data;
    } catch (error: any) {
      console.error('❌ Error en updateUser:', error);
      throw error;
    }
  },

  // Cambiar contraseña
  changePassword: async (id: number, data: ChangePasswordDTO): Promise<void> => {
    try {
      await api.post(`/users/${id}/change-password`, data);
    } catch (error: any) {
      console.error('❌ Error en changePassword:', error);
      throw error;
    }
  },

  // Desactivar usuario
  deactivateUser: async (id: number): Promise<void> => {
    try {
      await api.post(`/users/${id}/deactivate`);
    } catch (error: any) {
      console.error('❌ Error en deactivateUser:', error);
      throw error;
    }
  },

  // Eliminar usuario
  deleteUser: async (id: number): Promise<void> => {
    try {
      await api.delete(`/users/${id}`);
    } catch (error: any) {
      console.error('❌ Error en deleteUser:', error);
      throw error;
    }
  },

  // Resetear contraseña
  resetPassword: async (id: number): Promise<string> => {
    try {
      const response = await api.post(`/users/${id}/reset-password`);
      return response.data.temporaryPassword;
    } catch (error: any) {
      console.error('❌ Error en resetPassword:', error);
      throw error;
    }
  }
};

export default usersService;