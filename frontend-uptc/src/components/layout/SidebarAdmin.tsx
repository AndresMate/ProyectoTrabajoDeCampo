'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function SidebarAdmin() {
  const pathname = usePathname();

  const links = [
    { href: '/admin/torneos', label: 'Torneos', icon: '🏆' },
    { href: '/admin/equipos', label: 'Equipos', icon: '⚽' },
    { href: '/admin/partidos', label: 'Partidos', icon: '📅' },
    { href: '/admin/inscripciones', label: 'Inscripciones', icon: '📋' },
    { href: '/admin/usuarios', label: 'Usuarios', icon: '👥' },
    { href: '/admin/reportes', label: 'Reportes', icon: '📊' },
  ];

  return (
    <aside className="w-64 h-screen bg-uptc-black fixed left-0 top-0 overflow-y-auto shadow-2xl border-r-4 border-uptc-yellow">
      {/* Header del Sidebar */}
      <div className="p-6 border-b-2 border-uptc-yellow">
        <div className="flex items-center gap-3 mb-2">
          {/* Logo placeholder */}
          <div className="w-10 h-10 bg-uptc-yellow rounded-full flex items-center justify-center">
            <span className="text-uptc-black font-bold text-lg">U</span>
          </div>
          <div>
            <h2 className="text-uptc-yellow font-bold text-lg">Panel Admin</h2>
            <p className="text-gray-400 text-xs">Gestión de Torneos</p>
          </div>
        </div>
      </div>

      {/* Navegación */}
      <nav className="p-4">
        <div className="space-y-2">
          {links.map(({ href, label, icon }) => {
            const isActive = pathname === href;
            return (
              <Link
                key={href}
                href={href}
                className={`
                  flex items-center gap-3 px-4 py-3 rounded-lg
                  transition-all duration-200 font-semibold
                  ${
                    isActive
                      ? 'bg-uptc-yellow text-uptc-black shadow-lg'
                      : 'text-gray-400 hover:bg-gray-800 hover:text-uptc-yellow'
                  }
                `}
              >
                <span className="text-xl">{icon}</span>
                <span>{label}</span>
              </Link>
            );
          })}
        </div>
      </nav>

      {/* Footer del Sidebar */}
      <div className="absolute bottom-0 left-0 right-0 p-4 border-t-2 border-gray-800">
        <div className="text-center">
          <p className="text-xs text-gray-500">
            Sistema de Torneos UPTC
          </p>
          <p className="text-xs text-gray-600 mt-1">
            Versión 1.0 • 2025
          </p>
        </div>
      </div>
    </aside>
  );
}