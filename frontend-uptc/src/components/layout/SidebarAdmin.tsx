'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function SidebarAdmin() {
  const pathname = usePathname();

  const links = [
    { href: '/admin/torneos', label: 'Torneos' },
    { href: '/admin/equipos', label: 'Equipos' },
    { href: '/admin/partidos', label: 'Partidos' },
    { href: '/admin/inscripciones', label: 'Inscripciones' },
    { href: '/admin/usuarios', label: 'Usuarios' },
    { href: '/admin/reportes', label: 'Reportes' },
  ];

  return (
    <aside className="w-64 h-screen bg-slate-900 text-white p-4 fixed">
      <h2 className="text-xl font-bold mb-6">Panel Admin</h2>
      <nav className="flex flex-col gap-2">
        {links.map(({ href, label }) => (
          <Link
            key={href}
            href={href}
            className={`p-2 rounded hover:bg-slate-700 transition ${
              pathname === href ? 'bg-slate-700' : ''
            }`}
          >
            {label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
