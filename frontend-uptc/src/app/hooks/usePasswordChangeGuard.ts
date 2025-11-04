// src/hooks/usePasswordChangeGuard.ts
'use client';

import { useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { authService } from '@/services/authService';

export function usePasswordChangeGuard() {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    // Verificar autenticación
    if (!authService.isAuthenticated()) {
      if (pathname !== '/login') {
        console.log('🔒 Usuario no autenticado, redirigiendo a login');
        router.push('/login');
      }
      return;
    }

    const user = authService.getCurrentUser();
    if (!user) {
      router.push('/login');
      return;
    }

    // Si debe cambiar contraseña y NO está en /change-password
    if (user.forcePasswordChange && pathname !== '/change-password') {
      console.log('🔐 Usuario debe cambiar contraseña, redirigiendo...');
      router.push('/change-password');
      return;
    }

    // Si NO debe cambiar contraseña y está en /change-password
    if (!user.forcePasswordChange && pathname === '/change-password') {
      console.log('✅ Contraseña ya cambiada, redirigiendo a dashboard');
      router.push('/dashboard');
    }
  }, [router, pathname]);
}