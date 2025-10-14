'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const Sidebar = () => {
  const pathname = usePathname();

  const links = [
    { href: '/usuarios', label: 'Usuarios' },
    { href: '/equipos', label: 'Equipos' },
    { href: '/torneos', label: 'Torneos' },
    { href: '/partidos', label: 'Partidos' },
    { href: '/inscripciones', label: 'Inscripciones' },
  ];

  return (
    <aside className="w-64 h-screen bg-slate-900 text-white p-4 fixed">
      <h2 className="text-xl font-bold mb-6">Panel UPTC</h2>
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
};

export default Sidebar;
