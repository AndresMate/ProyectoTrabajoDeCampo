'use client';

import { useEffect, useState } from 'react';
import usersService from '@/services/usersService';
import { authService } from '@/services/authService';
import { useRouter } from 'next/navigation';
import Modal from '@/components/Modal';
import UserForm from '@/components/forms/UserForm';

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
        alert('Sesión expirada. Serás redirigido al login.');
        authService.logout();
      } else {
        alert(`Error: ${error.response?.data?.message || error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDeactivate = async (id: number) => {
    if (!confirm('¿Estás seguro de desactivar este usuario?')) return;

    try {
      await usersService.deactivateUser(id);
      alert('Usuario desactivado exitosamente');
      fetchUsers();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Error al desactivar usuario');
    }
  };

  const handleResetPassword = async (id: number) => {
    if (!confirm('¿Resetear la contraseña de este usuario? Se generará una contraseña temporal.')) return;

    try {
      const tempPassword = await usersService.resetPassword(id);
      alert(`Contraseña reseteada exitosamente.\n\nContraseña temporal: ${tempPassword}\n\nAsegúrate de compartirla con el usuario de forma segura.`);
    } catch (error: any) {
      alert(error.response?.data?.message || 'Error al resetear contraseña');
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
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-900"></div>
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
          className="bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800 transition"
        >
          + Nuevo Usuario
        </button>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Total</div>
          <div className="text-2xl font-bold text-gray-900">{users.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Activos</div>
          <div className="text-2xl font-bold text-green-600">
            {users.filter(u => u.isActive).length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Administradores</div>
          <div className="text-2xl font-bold text-blue-600">
            {users.filter(u => u.role === 'ADMIN' || u.role === 'SUPER_ADMIN').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Árbitros</div>
          <div className="text-2xl font-bold text-green-600">
            {users.filter(u => u.role === 'REFEREE').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-gray-500 text-sm">Usuarios</div>
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
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <select
            value={filterRole}
            onChange={(e) => setFilterRole(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
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
        <table className="min-w-full table-auto">
          <thead className="bg-gray-100 text-gray-700">
            <tr>
              <th className="px-4 py-2 text-left">Nombre</th>
              <th className="px-4 py-2 text-left">Correo</th>
              <th className="px-4 py-2 text-left">Rol</th>
              <th className="px-4 py-2 text-center">Estado</th>
              <th className="px-4 py-2 text-center">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.length > 0 ? (
              filteredUsers.map(user => (
                <tr key={user.id} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-2">{user.fullName}</td>
                  <td className="px-4 py-2">{user.email}</td>
                  <td className="px-4 py-2">
                    <span className={`px-3 py-1 text-sm rounded-full ${getRoleBadge(user.role)}`}>
                      {getRoleText(user.role)}
                    </span>
                  </td>
                  <td className="px-4 py-2 text-center">
                    {user.isActive ? (
                      <span className="text-green-600 font-medium">Activo</span>
                    ) : (
                      <span className="text-red-600 font-medium">Inactivo</span>
                    )}
                  </td>
                  <td className="px-4 py-2 text-center space-x-2">
                    <button
                      onClick={() => {
                        setSelectedUser(user.id);
                        setShowModal(true);
                      }}
                      className="text-blue-600 hover:underline"
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => handleDeactivate(user.id)}
                      className="text-yellow-600 hover:underline"
                    >
                      Desactivar
                    </button>
                    <button
                      onClick={() => handleResetPassword(user.id)}
                      className="text-red-600 hover:underline"
                    >
                      Resetear contraseña
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-gray-500">
                  No se encontraron usuarios.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {showModal && (
        <Modal onClose={() => setShowModal(false)} title={selectedUser ? 'Editar Usuario' : 'Nuevo Usuario'}>
          <UserForm
            userId={selectedUser}
            onSuccess={() => {
              setShowModal(false);
              fetchUsers();
            }}
          />
        </Modal>
      )}
    </div>
  );
}
