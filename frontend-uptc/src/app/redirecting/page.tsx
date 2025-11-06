// src/app/redirecting/page.tsx
'use client';

import { useEffect, useState } from 'react';

export default function RedirectingPage() {
  const [mounted, setMounted] = useState(false);
  const [redirecting, setRedirecting] = useState(false);

  useEffect(() => {
    // Evitar ejecución múltiple
    if (redirecting) return;

    setMounted(true);
    setRedirecting(true);

    console.log('🔄 Página de redirección cargada');

    // Leer directamente del localStorage
    const userStr = localStorage.getItem('user');
    const token = localStorage.getItem('token');

    if (!userStr || !token) {
      console.log('❌ No hay sesión, redirigiendo a login');
      // Usar replace para evitar loop
      window.location.replace('/login');
      return;
    }

    try {
      const user = JSON.parse(userStr);
      console.log('👤 Usuario:', user.email, '- Rol:', user.role);

      // Determinar destino
      let targetUrl = '/';

      if (user.role === 'SUPER_ADMIN' || user.role === 'ADMIN') {
        targetUrl = '/admin/torneos';
      } else if (user.role === 'REFEREE') {
        targetUrl = '/admin/torneos';
      }

      console.log('➡️ Redirigiendo a:', targetUrl);

      // Pequeño delay para evitar loops
      setTimeout(() => {
        // Usar replace para que no pueda volver atrás
        window.location.replace(targetUrl);
      }, 100);

    } catch (error) {
      console.error('❌ Error al procesar sesión:', error);
      window.location.replace('/login');
    }
  }, []); // Solo ejecutar al montar

  // Evitar hydration mismatch
  if (!mounted) {
    return null;
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-900">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-yellow-400 mx-auto mb-4"></div>
        <p className="text-white">Redirigiendo...</p>
      </div>
    </div>
  );
}