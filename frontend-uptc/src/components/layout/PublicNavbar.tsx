'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function PublicNavbar() {
  const pathname = usePathname();

  return (
    <nav className="w-full bg-slate-900 text-white flex justify-between items-center px-6 py-3 shadow-md">
      <h1 className="text-lg font-bold">Torneos UPTC</h1>
      <div className="flex gap-4">
        <Link
          href="/torneos"
          className={`hover:text-yellow-400 transition ${
            pathname.startsWith('/torneos') ? 'text-yellow-400' : ''
          }`}
        >
          Torneos
        </Link>
        <Link
          href="/login"
          className="bg-yellow-500 px-3 py-1 rounded hover:bg-yellow-400 transition"
        >
          Iniciar sesión
        </Link>
      </div>
    </nav>
  );
}
