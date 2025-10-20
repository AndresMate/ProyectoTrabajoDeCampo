'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function PublicNavbar() {
  const pathname = usePathname();

  return (
    <nav className="w-full bg-uptc-black text-uptc-yellow shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-20">
          {/* Logo y título */}
          <div className="flex items-center gap-4">
            <div className="flex-shrink-0">
              {/* Aquí iría el logosímbolo UPTC */}
              <div className="w-12 h-12 bg-uptc-yellow rounded-full flex items-center justify-center">
                <span className="text-uptc-black font-bold text-xl">U</span>
              </div>
            </div>
            <div className="hidden sm:block">
              <h1 className="text-xl font-bold text-uptc-yellow">
                Sistema de Torneos
              </h1>
              <p className="text-xs text-gray-400">
                Universidad Pedagógica y Tecnológica de Colombia
              </p>
            </div>
          </div>

          {/* Navegación */}
          <div className="flex items-center gap-6">
            <Link
              href="/"
              className={`font-semibold transition-colors duration-200 ${
                pathname === '/' 
                  ? 'text-uptc-yellow' 
                  : 'text-gray-400 hover:text-uptc-yellow'
              }`}
            >
              Inicio
            </Link>
            <Link
              href="/torneos"
              className={`font-semibold transition-colors duration-200 ${
                pathname.startsWith('/torneos') 
                  ? 'text-uptc-yellow' 
                  : 'text-gray-400 hover:text-uptc-yellow'
              }`}
            >
              Torneos
            </Link>
            <Link
              href="/login"
              className="btn-uptc-secondary text-sm"
            >
              Iniciar sesión
            </Link>
          </div>
        </div>
      </div>

      {/* Línea decorativa inferior */}
      <div className="h-1 bg-gradient-to-r from-uptc-yellow via-uptc-yellow to-transparent"></div>
    </nav>
  );
}