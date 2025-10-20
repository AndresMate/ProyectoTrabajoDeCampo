'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/authService';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await authService.login({ email, password });

      if (response.forcePasswordChange) {
        router.push('/change-password');
      } else if (response.role === 'SUPER_ADMIN' || response.role === 'ADMIN') {
        router.push('/admin/torneos');
      } else if (response.role === 'REFEREE') {
        router.push('/referee/partidos');
      } else {
        router.push('/');
      }
    } catch (err: any) {
      if (err.response) {
        const status = err.response.status;
        const message = err.response.data?.message || err.response.data?.error;

        if (status === 401 || status === 403) {
          setError('Correo o contraseña incorrectos');
        } else if (status === 500) {
          setError('Error en el servidor. Intenta de nuevo más tarde.');
        } else {
          setError(message || 'Error al iniciar sesión');
        }
      } else if (err.request) {
        setError('No se pudo conectar con el servidor. Verifica tu conexión.');
      } else {
        setError('Error inesperado. Intenta de nuevo.');
      }

      console.error('Error de login:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-uptc-black via-gray-900 to-uptc-black relative overflow-hidden">
      {/* Decoración de fondo */}
      <div className="absolute inset-0 opacity-10">
        <div className="absolute top-20 left-20 w-96 h-96 bg-uptc-yellow rounded-full blur-3xl"></div>
        <div className="absolute bottom-20 right-20 w-96 h-96 bg-uptc-yellow rounded-full blur-3xl"></div>
      </div>

      {/* Contenedor del formulario */}
      <div className="relative z-10 w-full max-w-md mx-4">
        <div className="bg-white rounded-2xl shadow-2xl overflow-hidden">
          {/* Header con colores UPTC */}
          <div className="bg-uptc-black p-8 text-center border-b-4 border-uptc-yellow">
            <div className="flex justify-center mb-4">
              <div className="w-20 h-20 bg-uptc-yellow rounded-full flex items-center justify-center shadow-xl">
                <span className="text-uptc-black font-bold text-3xl">U</span>
              </div>
            </div>
            <h1 className="text-2xl font-bold text-uptc-yellow mb-2">
              Sistema de Torneos UPTC
            </h1>
            <p className="text-gray-400 text-sm">
              Universidad Pedagógica y Tecnológica de Colombia
            </p>
          </div>

          {/* Formulario */}
          <div className="p-8">
            <h2 className="text-xl font-semibold text-uptc-black mb-6 text-center">
              Iniciar Sesión
            </h2>

            {error && (
              <div className="bg-red-50 border-l-4 border-red-500 text-red-700 px-4 py-3 rounded mb-6">
                <p className="text-sm">{error}</p>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Email */}
              <div>
                <label className="block text-uptc-black font-semibold mb-2 text-sm">
                  Correo Institucional
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:border-uptc-yellow focus:ring-4 focus:ring-uptc-yellow focus:ring-opacity-20 transition-all"
                  placeholder="ejemplo@uptc.edu.co"
                />
              </div>

              {/* Password */}
              <div>
                <label className="block text-uptc-black font-semibold mb-2 text-sm">
                  Contraseña
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    className="w-full px-4 py-3 pr-12 border-2 border-gray-300 rounded-lg focus:border-uptc-yellow focus:ring-4 focus:ring-uptc-yellow focus:ring-opacity-20 transition-all"
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-uptc-black transition-colors"
                  >
                    {showPassword ? '👁️' : '👁️‍🗨️'}
                  </button>
                </div>
              </div>

              {/* Botón de submit */}
              <button
                type="submit"
                disabled={loading}
                className="w-full btn-uptc-primary py-3 text-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <span className="animate-spin">⏳</span>
                    Iniciando sesión...
                  </span>
                ) : (
                  'Iniciar Sesión'
                )}
              </button>
            </form>

            {/* Links adicionales */}
            <div className="mt-6 space-y-3 text-center text-sm">
              <a href="#" className="block text-uptc-black hover:text-uptc-yellow-dark transition-colors font-medium">
                ¿Olvidaste tu contraseña?
              </a>
              <div className="border-t border-gray-200 pt-4">
                <a href="/" className="text-gray-600 hover:text-uptc-black transition-colors">
                  ← Volver al inicio
                </a>
              </div>
            </div>
          </div>

          {/* Footer del formulario */}
          <div className="bg-gray-50 px-8 py-4 border-t border-gray-200">
            <div className="flex items-center justify-center gap-2 text-xs text-gray-600">
              <span className="bg-uptc-yellow text-uptc-black px-2 py-1 rounded font-semibold">
                Vigilada Mineducación
              </span>
            </div>
          </div>
        </div>

        {/* Texto informativo */}
        <div className="mt-6 text-center">
          <p className="text-white text-sm">
            Sistema exclusivo para miembros de la comunidad UPTC
          </p>
        </div>
      </div>
    </div>
  );
}