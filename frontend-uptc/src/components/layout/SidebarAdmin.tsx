// frontend-uptc/src/components/layout/SidebarAdmin.tsx
'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import PermissionGate from '@/components/PermissionGate';
import { authService } from '@/services/authService';

export default function SidebarAdmin() {
  const pathname = usePathname();
  const [userRole, setUserRole] = useState<string | null>(null);

  useEffect(() => {
    const user = authService.getCurrentUser();
    setUserRole(user?.role || null);
  }, []);

  const links = [
    {
      href: '/admin/torneos',
      label: 'Torneos',
      icon: '🏆',
      permission: 'tournaments.view' as const,
      roles: null // Todos los roles con permiso pueden verlo
    },
    {
      href: '/admin/deportes',
      label: 'Deportes',
      icon: '⚽',
      permission: 'tournaments.view' as const,
      roles: ['ADMIN', 'SUPER_ADMIN'] // Solo ADMIN y SUPER_ADMIN
    },
    {
      href: '/admin/categorias',
      label: 'Categorías',
      icon: '🏅',
      permission: 'tournaments.view' as const,
      roles: ['ADMIN', 'SUPER_ADMIN'] // Solo ADMIN y SUPER_ADMIN
    },
    {
      href: '/admin/usuarios',
      label: 'Usuarios',
      icon: '👥',
      permission: 'users.view' as const,
      roles: null // Todos los roles con permiso pueden verlo
    },
    {
      href: '/admin/reportes',
      label: 'Reportes',
      icon: '📊',
      permission: 'reports.view' as const,
      roles: null // Todos los roles con permiso pueden verlo
    },
  ];

  return (
    <aside className="w-64 h-screen bg-uptc-black fixed left-0 top-0 overflow-y-auto shadow-2xl border-r-4 border-uptc-yellow">
      {/* Header del Sidebar */}
      <div className="p-6 border-b-4 border-uptc-yellow">
        <div className="flex items-center gap-3 mb-2">
          <div className="w-10 h-10 bg-uptc-yellow rounded-full flex items-center justify-center shadow-lg">
            <span className="text-uptc-black font-bold text-lg">U</span>
          </div>
          <div>
            <h2 className="text-uptc-yellow font-bold text-lg">Panel Admin</h2>
            <p className="text-white text-xs font-medium">Gestión de Torneos</p>
          </div>
        </div>
      </div>

      {/* Navegación con control de permisos */}
      <nav className="p-4">
        <div className="space-y-2">
          {links.map(({ href, label, icon, permission, roles }) => {
            const isActive = pathname.startsWith(href);
            
            // Verificar si el link debe mostrarse según el rol
            const shouldShow = roles === null || (userRole && roles.includes(userRole));

            if (!shouldShow) {
              return null;
            }

            return (
              <PermissionGate key={href} permission={permission}>
                <Link
                  href={href}
                  className={`
                    flex items-center gap-3 px-4 py-3 rounded-lg
                    transition-all duration-200 font-bold text-base
                    ${
                      isActive
                        ? 'bg-uptc-yellow text-uptc-black shadow-xl scale-105'
                        : 'text-uptc-yellow bg-gray-900 hover:bg-uptc-yellow hover:text-uptc-black hover:scale-105 shadow-md'
                    }
                  `}
                >
                  <span className="text-2xl">{icon}</span>
                  <span>{label}</span>
                </Link>
              </PermissionGate>
            );
          })}
        </div>
      </nav>

      {/* Footer del Sidebar */}
      <div className="absolute bottom-0 left-0 right-0 p-4 border-t-4 border-uptc-yellow bg-gray-900">
        <div className="text-center">
          <p className="text-xs text-white font-semibold">
            Sistema de Torneos UPTC
          </p>
          <p className="text-xs text-uptc-yellow mt-1 font-bold">
            Versión 1.0 • 2025
          </p>
        </div>
      </div>
    </aside>
  );
}