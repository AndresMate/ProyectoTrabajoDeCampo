// frontend-uptc/src/components/layout/NavbarAdmin.tsx - VERSIÓN CORREGIDA
'use client';

import { useEffect, useState } from 'react';
import { authService } from '@/services/authService';
import { useRouter } from 'next/navigation';

export default function NavbarAdmin() {
  const router = useRouter();
  const [user, setUser] = useState<any>(null);
  const [showMenu, setShowMenu] = useState(false);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (!currentUser) {
      router.push('/login');
    } else {
      setUser(currentUser);
    }
  }, [router]);

  const handleLogout = () => {
    if (confirm('¿Estás seguro de que deseas cerrar sesión?')) {
      authService.logout();
    }
  };

  const getRoleLabel = (role: string) => {
    const roles: any = {
      SUPER_ADMIN: 'Super Administrador',
      ADMIN: 'Administrador',
      REFEREE: 'Árbitro',
      USER: 'Usuario'
    };
    return roles[role] || role;
  };

  if (!user) return null;

  return (
    <header className="w-full h-20 bg-uptc-black shadow-lg border-b-4 border-uptc-yellow">
      <div className="h-full px-6 flex items-center justify-between">
        {/* Lado izquierdo: Logo y título */}
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 bg-uptc-yellow rounded-full flex items-center justify-center shadow-lg">
            <span className="text-uptc-black font-bold text-xl">U</span>
          </div>
          <div>
            <h1 className="text-lg font-bold text-uptc-yellow">Panel de Administración</h1>
            <p className="text-xs text-white">Sistema de Gestión de Torneos</p>
          </div>
        </div>

        {/* Lado derecho: Usuario y opciones */}
        <div className="flex items-center gap-4">
          {/* Info del usuario */}
          <div className="hidden md:block text-right">
            <p className="text-sm font-semibold text-white">{user.fullName}</p>
            <p className="text-xs text-uptc-yellow">{getRoleLabel(user.role)}</p>
          </div>

          {/* Avatar con menú */}
          <div className="relative">
            <button
              onClick={() => setShowMenu(!showMenu)}
              className="w-10 h-10 bg-uptc-yellow rounded-full flex items-center justify-center font-bold text-uptc-black hover:scale-110 transition-transform"
            >
              {user.fullName.charAt(0).toUpperCase()}
            </button>

            {/* Menú desplegable */}
            {showMenu && (
              <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-2xl border-2 border-uptc-yellow overflow-hidden z-50">
                <div className="bg-uptc-black p-4 border-b-2 border-uptc-yellow">
                  <p className="text-sm font-semibold text-uptc-yellow">{user.fullName}</p>
                  <p className="text-xs text-white">{user.email}</p>
                </div>

                <div className="py-2">
                  <button
                    onClick={() => {
                      setShowMenu(false);
                      router.push('/profile');
                    }}
                    className="w-full px-4 py-2 text-left text-sm text-uptc-black hover:bg-gray-100 transition-colors font-medium"
                  >
                    👤 Mi Perfil
                  </button>
                  <button
                    onClick={() => {
                      setShowMenu(false);
                      router.push('/settings');
                    }}
                    className="w-full px-4 py-2 text-left text-sm text-uptc-black hover:bg-gray-100 transition-colors font-medium"
                  >
                    ⚙️ Configuración
                  </button>
                  <hr className="my-2 border-gray-200" />
                  <button
                    onClick={handleLogout}
                    className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 transition-colors font-semibold"
                  >
                    🚪 Cerrar Sesión
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}