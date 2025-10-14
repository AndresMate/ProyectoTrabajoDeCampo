'use client';

import '@/app/globals.css';
import PublicNavbar from '@/components/layout/PublicNavbar';
import { usePathname } from 'next/navigation';
import NavbarAdmin from '@/components/layout/NavbarAdmin';
import SidebarAdmin from '@/components/layout/SidebarAdmin';

/**
 * Layout global que detecta si estamos en el área pública o en el panel admin
 * y renderiza la estructura visual adecuada.
 */
export default function LayoutWrapper({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  // Detecta si la ruta pertenece al panel de administración
  const isAdminRoute = pathname.startsWith('/admin');

  if (isAdminRoute) {
    return (
      <div className="flex min-h-screen bg-gray-100">
        <SidebarAdmin />
        <div className="flex-1 ml-64">
          <NavbarAdmin />
          <main className="p-6">{children}</main>
        </div>
      </div>
    );
  }

  // Si no es admin, muestra layout público
  return (
    <div className="min-h-screen bg-gray-100">
      <PublicNavbar />
      <main className="p-6">{children}</main>
    </div>
  );
}
