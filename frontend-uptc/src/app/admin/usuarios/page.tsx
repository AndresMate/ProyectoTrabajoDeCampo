'use client';

import { useEffect, useState } from 'react';
import usersService from '@/services/usersService';
import { authService } from '@/services/authService';
import { useRouter } from 'next/navigation';

interface User {
  id: number;
  fullName: string;
  email: string;
  role: string;
  isActive: boolean;
  createdAt: string;
}

export default function AdminUsuariosPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const router = useRouter();

  useEffect(() => {
    checkAuthentication();
  }, [router]);

  const checkAuthentication = () => {
    const user = authService.getCurrentUser();
    const token = authService.getToken();

    console.log('🔐 DEBUG Auth Check:', {
      user,
      token: token ? `PRESENTE (${token.substring(0, 20)}...)` : 'AUSENTE',
      path: window.location.pathname
    });

    if (!token || !user) {
      console.log('❌ No autenticado - redirigiendo a login');
      router.push('/login');
      return;
    }

    if (user.role !== 'SUPER_ADMIN') {
      console.log('❌ Sin permisos - redirigiendo a home');
      router.push('/');
      return;
    }

    console.log('✅ Autenticación OK - cargando usuarios');
    fetchUsers();
  };

  const fetchUsers = async () => {
    try {
      console.log('📋 Iniciando carga de usuarios...');
      const data = await usersService.getUsers();
      console.log('✅ Usuarios cargados:', data.content?.length || 0, 'usuarios');
      setUsers(data.content || data);
    } catch (error: any) {
      console.error('❌ Error en fetchUsers:', {
        status: error.response?.status,
        message: error.response?.data?.message,
        data: error.response?.data
      });

      if (error.response?.status === 401) {
        console.log('🔐 401 detectado - mostrando alerta');
        alert('Sesión expirada. Serás redirigido al login.');
        authService.logout();
      } else {
        alert(`Error: ${error.response?.data?.message || error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    authService.logout();
  };

  const handleCreateUser = () => {
    // Aquí irá la lógica para crear usuario
    alert('Funcionalidad de crear usuario próximamente');
  };

  const getRoleBadge = (role: string) => {
    const colors = {
      SUPER_ADMIN: 'bg-red-100 text-red-700',
      ADMIN: 'bg-blue-100 text-blue-700',
      REFEREE: 'bg-green-100 text-green-700',
      USER: 'bg-gray-100 text-gray-700'
    };
    return colors[role as keyof typeof colors] || 'bg-gray-100 text-gray-700';
  };

  const getRoleText = (role: string) => {
    const texts = {
      SUPER_ADMIN: 'Super Admin',
      ADMIN: 'Administrador',
      REFEREE: 'Árbitro',
      USER: 'Usuario'
    };
    return texts[role as keyof typeof texts] || role;
  };

  const filteredUsers = users.filter(
    (user) =>
      user.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-900"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Usuarios</h1>
        <div className="flex gap-2">
          <button
            onClick={handleLogout}
            className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-600 transition"
          >
            Cerrar Sesión
          </button>
          <button
            onClick={handleCreateUser}
            className="bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800 transition"
          >
            + Nuevo Usuario
          </button>
        </div>
      </div>

      {/* Barra de búsqueda */}
      <div className="mb-6">
        <input
          type="text"
          placeholder="Buscar por nombre o correo..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* Tabla de usuarios */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Usuario
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Rol
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Estado
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fecha de registro
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredUsers.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div className="font-medium text-gray-900">{user.fullName}</div>
                    <div className="text-sm text-gray-500">{user.email}</div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${getRoleBadge(
                      user.role
                    )}`}
                  >
                    {getRoleText(user.role)}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full ${
                      user.isActive
                        ? 'bg-green-100 text-green-700'
                        : 'bg-red-100 text-red-700'
                    }`}
                  >
                    {user.isActive ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(user.createdAt).toLocaleDateString('es-ES')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button className="text-blue-900 hover:text-blue-700 mr-3 transition-colors">
                    Editar
                  </button>
                  <button className="text-red-600 hover:text-red-800 transition-colors">
                    Desactivar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredUsers.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center mt-6">
          <p className="text-gray-500 text-lg">No se encontraron usuarios</p>
          <p className="text-gray-400 text-sm mt-2">
            {users.length === 0 ? 'No hay usuarios en el sistema' : 'No hay coincidencias con tu búsqueda'}
          </p>
        </div>
      )}
    </div>
  );
}