// frontend-uptc/src/app/admin/usuarios/page.tsx - VERSIÓN CORREGIDA
'use client';

import { useEffect, useState } from 'react';
import usersService from '@/services/usersService';
import { authService } from '@/services/authService';
import { useRouter } from 'next/navigation';
import Modal from '@/components/Modal';
import UserForm from '@/components/forms/UserForm';
import { toastError, toastSuccess, toastPromise } from '@/utils/toast';

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
  const [showModal, setShowModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState<number | undefined>();
  const [filterRole, setFilterRole] = useState<string>('ALL');
  const router = useRouter();

  useEffect(() => {
    checkAuthentication();
  }, []);

  const checkAuthentication = () => {
    const user = authService.getCurrentUser();
    const token = authService.getToken();

    if (!token || !user) {
      router.push('/login');
      return;
    }

    if (user.role !== 'SUPER_ADMIN') {
      router.push('/');
      return;
    }

    fetchUsers();
  };

  const fetchUsers = async () => {
    try {
      const data = await usersService.getUsers();
      setUsers(data.content || data);
    } catch (error: any) {
      console.error('Error en fetchUsers:', error);
      if (error.response?.status === 401) {
        toastError('Sesión expirada. Serás redirigido al login.');
        authService.logout();
      } else {
        // El error ya se muestra en el interceptor de axios
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDeactivate = async (id: number) => {
    try {
      await toastPromise(
        usersService.deactivateUser(id),
        {
          loading: 'Desactivando usuario...',
          success: 'Usuario desactivado exitosamente',
          error: (error: any) => error.response?.data?.message || 'Error al desactivar usuario'
        }
      );
      fetchUsers();
    } catch (error: any) {
      // El error ya se muestra en el toastPromise
    }
  };

  const handleResetPassword = async (id: number) => {
    try {
      const tempPassword = await toastPromise(
        usersService.resetPassword(id),
        {
          loading: 'Reseteando contraseña...',
          success: (pwd: string) => `Contraseña reseteada exitosamente.\n\nContraseña temporal: ${pwd}\n\nAsegúrate de compartirla con el usuario de forma segura.`,
          error: (error: any) => error.response?.data?.message || 'Error al resetear contraseña'
        }
      );
    } catch (error: any) {
      // El error ya se muestra en el toastPromise
    }
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

  const filteredUsers = users.filter(user => {
    const matchesSearch =
      user.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesRole = filterRole === 'ALL' || user.role === filterRole;

    return matchesSearch && matchesRole;
  });

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-uptc-yellow"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-800">Gestión de Usuarios</h1>
        <button
          onClick={() => {
            setSelectedUser(undefined);
            setShowModal(true);
          }}
          className="bg-uptc-black text-uptc-yellow px-6 py-2 rounded-lg hover:bg-gray-800 transition font-semibold"
        >
          + Nuevo Usuario
        </button>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Total</div>
          <div className="text-2xl font-bold text-gray-900">{users.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Activos</div>
          <div className="text-2xl font-bold text-green-600">
            {users.filter(u => u.isActive).length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Administradores</div>
          <div className="text-2xl font-bold text-blue-600">
            {users.filter(u => u.role === 'ADMIN' || u.role === 'SUPER_ADMIN').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Árbitros</div>
          <div className="text-2xl font-bold text-green-600">
            {users.filter(u => u.role === 'REFEREE').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-600 text-sm font-semibold">Usuarios</div>
          <div className="text-2xl font-bold text-gray-600">
            {users.filter(u => u.role === 'USER').length}
          </div>
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <input
            type="text"
            placeholder="Buscar por nombre o correo..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="flex-1 px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow focus:border-uptc-yellow bg-white text-gray-900 font-medium"
          />
          <select
            value={filterRole}
            onChange={(e) => setFilterRole(e.target.value)}
            className="px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-uptc-yellow bg-white text-gray-900 font-medium"
          >
            <option value="ALL">Todos los roles</option>
            <option value="SUPER_ADMIN">Super Admin</option>
            <option value="ADMIN">Administrador</option>
            <option value="REFEREE">Árbitro</option>
            <option value="USER">Usuario</option>
          </select>
        </div>
      </div>

      {/* Tabla de usuarios */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-uptc-black">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Usuario
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Rol
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Estado
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Fecha de registro
              </th>
              <th className="px-6 py-3 text-right text-xs font-semibold text-uptc-yellow uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredUsers.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div className="font-semibold text-gray-900">{user.fullName}</div>
                    <div className="text-sm text-gray-600">{user.email}</div>
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
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700 font-medium">
                  {new Date(user.createdAt).toLocaleDateString('es-ES')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-semibold">
                  <button
                    onClick={() => {
                      setSelectedUser(user.id);
                      setShowModal(true);
                    }}
                    className="text-indigo-600 hover:text-indigo-900 mr-3 transition-colors"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => handleResetPassword(user.id)}
                    className="text-blue-600 hover:text-blue-800 mr-3 transition-colors"
                  >
                    Resetear
                  </button>
                  {user.isActive && (
                    <button
                      onClick={() => handleDeactivate(user.id)}
                      className="text-red-600 hover:text-red-800 transition-colors"
                    >
                      Desactivar
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredUsers.length === 0 && (
        <div className="bg-white rounded-lg shadow p-8 text-center mt-6">
          <p className="text-gray-500 text-lg font-medium">No se encontraron usuarios</p>
          <p className="text-gray-400 text-sm mt-2">
            {users.length === 0 ? 'No hay usuarios en el sistema' : 'No hay coincidencias con tu búsqueda'}
          </p>
        </div>
      )}

      {/* Modal para crear/editar usuario */}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setSelectedUser(undefined);
        }}
        title={selectedUser ? 'Editar Usuario' : 'Crear Nuevo Usuario'}
        size="md"
      >
        <UserForm
          userId={selectedUser}
          onSuccess={() => {
            setShowModal(false);
            setSelectedUser(undefined);
            fetchUsers();
          }}
          onCancel={() => {
            setShowModal(false);
            setSelectedUser(undefined);
          }}
        />
      </Modal>
    </div>
  );
}