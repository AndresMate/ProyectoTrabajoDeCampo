// src/app/change-password/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/authService';
import { toastSuccess, toastPromise } from '@/utils/toast';

export default function ChangePasswordPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [user, setUser] = useState<ReturnType<typeof authService.getCurrentUser>>(null);

  useEffect(() => {
    // Verificar autenticación
    if (!authService.isAuthenticated()) {
      router.push('/login');
      return;
    }

    const currentUser = authService.getCurrentUser();
    setUser(currentUser);

    // Si NO debe cambiar contraseña, redirigir según rol
    if (!currentUser?.forcePasswordChange) {
      if (currentUser?.role === 'SUPER_ADMIN' || currentUser?.role === 'ADMIN') {
        router.push('/admin/torneos');
      } else if (currentUser?.role === 'REFEREE') {
        router.push('/admin/torneos');
      } else {
        router.push('/');
      }
    }
  }, [router]);

  const validatePassword = (password: string): string[] => {
    const errors: string[] = [];

    if (password.length < 8) {
      errors.push('Mínimo 8 caracteres');
    }
    if (!/[A-Z]/.test(password)) {
      errors.push('Una mayúscula');
    }
    if (!/[a-z]/.test(password)) {
      errors.push('Una minúscula');
    }
    if (!/[0-9]/.test(password)) {
      errors.push('Un número');
    }
    if (!/[!@#$%^&*()_+\-=\[\]{}|;:,.<>?]/.test(password)) {
      errors.push('Un carácter especial');
    }

    return errors;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    const validationErrors: Record<string, string> = {};

    if (!formData.newPassword) {
      validationErrors.newPassword = 'La contraseña es requerida';
    } else {
      const passwordErrors = validatePassword(formData.newPassword);
      if (passwordErrors.length > 0) {
        validationErrors.newPassword = 'Falta: ' + passwordErrors.join(', ');
      }
    }

    if (!formData.confirmPassword) {
      validationErrors.confirmPassword = 'Confirma tu contraseña';
    } else if (formData.newPassword !== formData.confirmPassword) {
      validationErrors.confirmPassword = 'Las contraseñas no coinciden';
    }

    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    if (!user?.userId) {
      setErrors({ submit: 'Error: Usuario no identificado' });
      return;
    }

    setIsLoading(true);

    try {
      await toastPromise(
        authService.forcePasswordChange(user.userId, formData.newPassword),
        {
          loading: 'Cambiando contraseña...',
          success: '✅ Contraseña cambiada exitosamente',
          error: (error) => error.message || 'Error al cambiar la contraseña'
        }
      );

      // Redirigir según rol
      if (user.role === 'SUPER_ADMIN' || user.role === 'ADMIN') {
        router.push('/admin/torneos');
      } else if (user.role === 'REFEREE') {
        router.push('/admin/torneos');
      } else {
        router.push('/');
      }

    } catch (error: any) {
      setErrors({
        submit: error.message || 'Error al cambiar la contraseña'
      });
    } finally {
      setIsLoading(false);
    }
  };

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-uptc-black">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-uptc-yellow border-t-transparent rounded-full animate-spin mx-auto"></div>
          <p className="mt-4 text-white">Cargando...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-uptc-black via-gray-900 to-uptc-black relative overflow-hidden">
      <div className="absolute inset-0 opacity-10">
        <div className="absolute top-20 left-20 w-96 h-96 bg-uptc-yellow rounded-full blur-3xl"></div>
        <div className="absolute bottom-20 right-20 w-96 h-96 bg-uptc-yellow rounded-full blur-3xl"></div>
      </div>

      <div className="relative z-10 w-full max-w-md mx-4">
        <div className="bg-white rounded-2xl shadow-2xl overflow-hidden">
          {/* Header */}
          <div className="bg-uptc-black p-8 text-center border-b-4 border-uptc-yellow">
            <div className="flex justify-center mb-4">
              <div className="w-20 h-20 bg-yellow-100 rounded-full flex items-center justify-center shadow-xl">
                <svg className="w-10 h-10 text-uptc-yellow" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
            </div>
            <h1 className="text-2xl font-bold text-uptc-yellow mb-2">
              Cambio de Contraseña Obligatorio
            </h1>
            <p className="text-gray-400">
              Hola, <strong>{user.fullName}</strong>
            </p>
          </div>

          {/* Form */}
          <div className="p-8">
            <div className="bg-yellow-50 border-l-4 border-uptc-yellow p-4 mb-6">
              <p className="text-sm text-gray-700">
                Por seguridad, debes cambiar tu contraseña antes de continuar.
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Nueva Contraseña */}
              <div>
                <label className="block text-uptc-black font-semibold mb-2 text-sm">
                  Nueva Contraseña *
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={formData.newPassword}
                    onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                    className={`w-full px-4 py-3 border rounded-md focus:outline-none focus:ring-2 focus:ring-uptc-yellow ${
                      errors.newPassword ? 'border-red-500' : ''
                    }`}
                    placeholder="Nueva contraseña"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-gray-600 hover:text-uptc-black"
                  >
                    {showPassword ? 'Ocultar' : 'Mostrar'}
                  </button>
                </div>
                {errors.newPassword && (
                  <p className="mt-1 text-sm text-red-600">{errors.newPassword}</p>
                )}
              </div>

              {/* Confirmar Contraseña */}
              <div>
                <label className="block text-uptc-black font-semibold mb-2 text-sm">
                  Confirmar Contraseña *
                </label>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={formData.confirmPassword}
                  onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                  className={`w-full px-4 py-3 border rounded-md focus:outline-none focus:ring-2 focus:ring-uptc-yellow ${
                    errors.confirmPassword ? 'border-red-500' : ''
                  }`}
                  placeholder="Confirmar contraseña"
                />
                {errors.confirmPassword && (
                  <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>
                )}
              </div>

              {/* Requisitos */}
              <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <p className="text-sm font-semibold text-uptc-black mb-2">
                  Requisitos:
                </p>
                <ul className="text-xs text-gray-700 space-y-1">
                  <li>• Mínimo 8 caracteres</li>
                  <li>• Una mayúscula (A-Z)</li>
                  <li>• Una minúscula (a-z)</li>
                  <li>• Un número (0-9)</li>
                  <li>• Un carácter especial (!@#$%...)</li>
                </ul>
              </div>

              {/* Error General */}
              {errors.submit && (
                <div className="bg-red-50 border-l-4 border-red-500 p-4">
                  <p className="text-sm text-red-800">{errors.submit}</p>
                </div>
              )}

              {/* Botones */}
              <div className="space-y-3">
                <button
                  type="submit"
                  disabled={isLoading}
                  className="w-full btn-uptc-primary py-3 text-lg font-bold disabled:opacity-50"
                >
                  {isLoading ? 'Cambiando contraseña...' : 'Cambiar Contraseña'}
                </button>

                <button
                  type="button"
                  onClick={() => authService.logout()}
                  className="w-full bg-gray-100 text-gray-700 py-3 rounded-md font-semibold hover:bg-gray-200 transition-colors"
                >
                  Cerrar Sesión
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}