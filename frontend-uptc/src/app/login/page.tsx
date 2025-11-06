// src/app/login/page.tsx
'use client';

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { authService } from '@/services/authService';
import Link from "next/link";

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [isChecking, setIsChecking] = useState(true);

  // Verificar autenticación solo una vez al montar
  useEffect(() => {
    let isMounted = true;

    const checkAuth = () => {
      try {
        const token = authService.getToken();
        const user = authService.getCurrentUser();

        if (token && user && isMounted) {
          console.log('👤 Usuario ya autenticado, redirigiendo...');
          // Usar replace en lugar de href para evitar loop
          window.location.replace('/redirecting');
        }
      } catch (error) {
        console.error('Error checking auth:', error);
      } finally {
        if (isMounted) {
          setIsChecking(false);
        }
      }
    };

    // Pequeño delay para evitar loops
    const timer = setTimeout(checkAuth, 100);

    return () => {
      isMounted = false;
      clearTimeout(timer);
    };
  }, []); // Solo ejecutar al montar

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    console.log('🔐 Iniciando login...');

    try {
      const response = await authService.login({ email, password });
      console.log('✅ Login exitoso:', response);

      const savedToken = authService.getToken();
      const savedUser = authService.getCurrentUser();

      console.log('💾 Token guardado:', savedToken ? 'Sí' : 'No');
      console.log('💾 Usuario guardado:', savedUser);

      if (!savedToken || !savedUser) {
        throw new Error('Error al guardar datos de sesión');
      }

      console.log('➡️ Redirigiendo a /redirecting');
      // Usar replace para evitar que el usuario pueda volver atrás
      window.location.replace('/redirecting');

    } catch (err: unknown) {
      console.error('❌ Error en login:', err);

      if (axios.isAxiosError(err)) {
        const status = err.response?.status;
        const message = err.response?.data?.message ?? err.response?.data?.error ?? err.message;

        if (status === 401 || status === 403) {
          setError('Credenciales inválidas. Verifica tu correo y contraseña.');
        } else if (status === 429) {
          setError('Demasiados intentos. Intenta nuevamente más tarde.');
        } else if (status === 500) {
          setError('Error en el servidor. Por favor contacta al administrador.');
        } else {
          setError(typeof message === 'string' && message.length ? message : 'Error inesperado.');
        }
      } else {
        setError('Error de conexión. Verifica tu red.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Mostrar loading mientras verifica autenticación
  if (isChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-yellow-400 mx-auto mb-4"></div>
          <p className="text-white">Verificando sesión...</p>
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

          <div className="p-8">
            <h2 className="text-xl font-semibold text-uptc-black mb-6 text-center">
              Iniciar Sesión
            </h2>

            {error && (
              <div className="bg-red-50 border-l-4 border-red-500 text-red-700 px-4 py-3 rounded mb-6">
                <p className="text-sm">{error}</p>
              </div>
            )}

            {loading && (
              <div className="bg-blue-50 border-l-4 border-blue-500 text-blue-700 px-4 py-3 rounded mb-6 flex items-center gap-3">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-700"></div>
                <p className="text-sm">Autenticando...</p>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-uptc-black font-semibold mb-2 text-sm">
                  Correo electrónico
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  disabled={loading}
                  className="w-full px-4 py-3 border rounded-md focus:outline-none focus:ring-2 focus:ring-uptc-yellow text-gray-900 disabled:bg-gray-100"
                  placeholder="tu@uptc.edu.co"
                  autoComplete="email"
                />
              </div>

              <div>
                <label className="block text-uptc-black font-semibold mb-2 text-sm">
                  Contraseña
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    disabled={loading}
                    className="w-full px-4 py-3 border rounded-md focus:outline-none focus:ring-2 focus:ring-uptc-yellow text-gray-900 disabled:bg-gray-100"
                    placeholder="********"
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(s => !s)}
                    disabled={loading}
                    className="absolute right-3 top-3 text-sm text-gray-600 hover:text-uptc-yellow"
                  >
                    {showPassword ? '👁️' : '👁️‍🗨️'}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full btn-uptc-primary py-3 text-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? '⏳ Ingresando...' : 'Iniciar Sesión'}
              </button>
            </form>

            {process.env.NODE_ENV === 'development' && (
              <div className="mt-4 p-4 bg-gray-50 rounded text-xs border border-gray-200">
                <p className="font-semibold text-gray-700 mb-2">🔧 Debug:</p>
                <div className="space-y-1 text-gray-600">
                  <p>Token: {authService.getToken() ? '✅' : '❌'}</p>
                  <p>Usuario: {authService.getCurrentUser()?.email || '❌'}</p>
                  <p>Rol: {authService.getCurrentUser()?.role || 'N/A'}</p>
                  <p>Checking: {isChecking ? 'Sí' : 'No'}</p>
                </div>
              </div>
            )}

            <div className="mt-6 space-y-3 text-center text-sm">
              <a href="#" className="block text-uptc-black hover:text-uptc-yellow-dark font-medium">
                ¿Olvidaste tu contraseña?
              </a>
              <div className="border-t border-gray-200 pt-4">
                <Link href="/" className="text-gray-600 hover:text-uptc-black">
                  ← Volver al inicio
                </Link>
              </div>
            </div>
          </div>

          <div className="bg-gray-50 px-8 py-4 border-t border-gray-200">
            <div className="flex items-center justify-center gap-2 text-xs text-gray-600">
              <span className="bg-uptc-yellow text-uptc-black px-2 py-1 rounded font-semibold">
                Vigilada Mineducación
              </span>
            </div>
          </div>
        </div>

        <div className="mt-6 text-center">
          <p className="text-white text-sm">
            Sistema exclusivo para miembros de la comunidad UPTC
          </p>
        </div>
      </div>
    </div>
  );
}