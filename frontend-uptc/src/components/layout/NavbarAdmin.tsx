'use client';

import { useEffect, useState } from 'react';
import { authService } from '@/services/authService';
import { useRouter } from 'next/navigation';

export default function NavbarAdmin() {
  const router = useRouter();
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (!currentUser) {
      router.push('/login');
    } else {
      setUser(currentUser);
    }
  }, [router]);

  const handleLogout = () => {
    if (confirm('¿Estás seguro de que deseas cerrar sesión?')) {
      authService.logout();
    }
  };

  if (!user) return null;

  return (
    <header className="w-full h-16 bg-slate-800 text-white flex items-center justify-between px-6 shadow-md">
      <h1 className="text-lg font-semibold">Panel de Administración</h1>
      <div className="flex items-center gap-4">
        <div className="text-right">
          <p className="text-sm font-medium">{user.fullName}</p>
          <p className="text-xs text-gray-400">{user.role}</p>
        </div>
        <button
          onClick={handleLogout}
          className="bg-red-500 px-4 py-2 rounded hover:bg-red-400 transition text-sm font-medium"
        >
          Cerrar sesión
        </button>
      </div>
    </header>
  );
}