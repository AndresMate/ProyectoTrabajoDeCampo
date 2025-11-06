// src/middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const pathname = request.nextUrl.pathname;

  console.log('🔒 Middleware:', pathname);

  // Rutas públicas que NUNCA requieren autenticación
  const publicPaths = [
    '/',
    '/login',
    '/redirecting',
    '/torneos',
    '/_next',
    '/api',
    '/favicon.ico'
  ];

  // Si es ruta pública, permitir SIEMPRE
  const isPublicPath = publicPaths.some(path =>
    pathname === path || pathname.startsWith(path)
  );

  if (isPublicPath) {
    console.log('✅ Ruta pública, permitida:', pathname);
    return NextResponse.next();
  }

  // Para rutas protegidas, verificar token en localStorage (simulado con cookies)
  const token = request.cookies.get('auth_token')?.value;

  // Si no hay token y NO es ruta pública, redirigir a login
  if (!token && pathname.startsWith('/admin')) {
    console.log('❌ Sin token, redirigiendo a /login desde:', pathname);
    return NextResponse.redirect(new URL('/login', request.url));
  }

  // Si hay token, verificar permisos por rol
  const userDataCookie = request.cookies.get('user')?.value;

  if (userDataCookie) {
    try {
      const userData = JSON.parse(userDataCookie);
      const role = userData.role;

      console.log('👤 Usuario rol:', role, '- Ruta:', pathname);

      // SUPER_ADMIN puede acceder a todo
      if (role === 'SUPER_ADMIN') {
        console.log('✅ SUPER_ADMIN: acceso total');
        return NextResponse.next();
      }

      // ADMIN puede acceder a todo excepto /admin/usuarios
      if (role === 'ADMIN') {
        if (pathname.startsWith('/admin/usuarios')) {
          console.log('⚠️ ADMIN: bloqueado /admin/usuarios');
          return NextResponse.redirect(new URL('/admin/torneos', request.url));
        }
        console.log('✅ ADMIN: acceso permitido');
        return NextResponse.next();
      }

      // REFEREE solo puede acceder a partidos
      if (role === 'REFEREE') {
        // Bloquear acceso a deportes y categorías (solo ADMIN y SUPER_ADMIN)
        if (pathname.startsWith('/admin/deportes') || pathname.startsWith('/admin/categorias')) {
          console.log('⚠️ REFEREE: bloqueado acceso a deportes/categorías, redirigiendo a torneos');
          return NextResponse.redirect(new URL('/admin/torneos', request.url));
        }

        if (
          pathname.startsWith('/admin/torneos') &&
          (pathname.includes('/partidos') || pathname.includes('/live'))
        ) {
          console.log('✅ REFEREE: acceso a partidos permitido');
          return NextResponse.next();
        }

        // Si intenta acceder a otras rutas admin, redirigir
        if (pathname.startsWith('/admin')) {
          console.log('⚠️ REFEREE: bloqueado, redirigiendo a torneos');
          return NextResponse.redirect(new URL('/admin/torneos', request.url));
        }
      }

      // USER normal no puede acceder al admin
      if (role === 'USER' && pathname.startsWith('/admin')) {
        console.log('⚠️ USER: bloqueado admin');
        return NextResponse.redirect(new URL('/', request.url));
      }

    } catch (e) {
      console.error('❌ Error parsing user data:', e);
    }
  }

  console.log('✅ Middleware: permitido por defecto');
  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
};