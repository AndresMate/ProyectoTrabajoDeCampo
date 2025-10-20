// frontend-uptc/src/components/layout/PublicNavbar.tsx - VERSIÓN COMPLETAMENTE CORREGIDA
'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function PublicNavbar() {
  const pathname = usePathname();

  return (
    <nav className="w-full bg-uptc-black shadow-lg border-b-4 border-uptc-yellow">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-20">
          {/* Logo y título */}
          <Link href="/" className="flex items-center gap-4 hover:opacity-90 transition-opacity">
            <div className="flex-shrink-0">
              <div className="w-12 h-12 bg-uptc-yellow rounded-full flex items-center justify-center shadow-lg">
                <span className="text-uptc-black font-bold text-xl">U</span>
              </div>
            </div>
            <div className="hidden sm:block">
              <h1 className="text-xl font-bold text-uptc-yellow">
                Sistema de Torneos
              </h1>
              <p className="text-xs text-gray-300">
                Universidad Pedagógica y Tecnológica de Colombia
              </p>
            </div>
          </Link>

          {/* Navegación - COMPLETAMENTE VISIBLE */}
          <div className="flex items-center gap-8">
            <Link
              href="/"
              className={`font-bold transition-all duration-200 text-lg px-4 py-2 rounded-lg ${
                pathname === '/' 
                  ? 'text-uptc-black bg-uptc-yellow shadow-lg' 
                  : 'text-uptc-yellow hover:bg-uptc-yellow hover:text-uptc-black'
              }`}
            >
              Inicio
            </Link>

            <Link
              href="/torneos"
              className={`font-bold transition-all duration-200 text-lg px-4 py-2 rounded-lg ${
                pathname.startsWith('/torneos') 
                  ? 'text-uptc-black bg-uptc-yellow shadow-lg' 
                  : 'text-uptc-yellow hover:bg-uptc-yellow hover:text-uptc-black'
              }`}
            >
              Torneos
            </Link>

            <Link
              href="/login"
              className="bg-uptc-yellow text-uptc-black font-bold px-6 py-2.5 rounded-lg hover:bg-yellow-400 transition-all shadow-lg hover:shadow-xl hover:scale-105"
            >
              Iniciar sesión
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
}