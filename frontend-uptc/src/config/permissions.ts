export type Permission =
  | 'tournaments.view'
  | 'tournaments.create'
  | 'tournaments.edit'
  | 'tournaments.delete'
  | 'tournaments.change_status'
  | 'matches.view'
  | 'matches.create'
  | 'matches.edit'
  | 'matches.delete'
  | 'matches.manage_results'
  | 'matches.start_finish'
  | 'teams.view'
  | 'teams.create'
  | 'teams.edit'
  | 'teams.delete'
  | 'inscriptions.view'
  | 'inscriptions.approve'
  | 'inscriptions.reject'
  | 'reports.view'
  | 'reports.generate'
  | 'users.view'
  | 'users.create'
  | 'users.edit'
  | 'users.delete'
  | 'users.reset_password';

export type Role = 'SUPER_ADMIN' | 'ADMIN' | 'REFEREE' | 'USER';

// Definición de permisos por rol
export const ROLE_PERMISSIONS: Record<Role, Permission[]> = {
  SUPER_ADMIN: [
    // Tiene TODOS los permisos
    'tournaments.view',
    'tournaments.create',
    'tournaments.edit',
    'tournaments.delete',
    'tournaments.change_status',
    'matches.view',
    'matches.create',
    'matches.edit',
    'matches.delete',
    'matches.manage_results',
    'matches.start_finish',
    'teams.view',
    'teams.create',
    'teams.edit',
    'teams.delete',
    'inscriptions.view',
    'inscriptions.approve',
    'inscriptions.reject',
    'reports.view',
    'reports.generate',
    'users.view',
    'users.create',
    'users.edit',
    'users.delete',
    'users.reset_password',
  ],

  ADMIN: [
    // Gestión completa de torneos
    'tournaments.view',
    'tournaments.create',
    'tournaments.edit',
    'tournaments.delete',
    'tournaments.change_status',
    // Gestión completa de partidos
    'matches.view',
    'matches.create',
    'matches.edit',
    'matches.delete',
    'matches.manage_results',
    'matches.start_finish',
    // Gestión de equipos
    'teams.view',
    'teams.create',
    'teams.edit',
    'teams.delete',
    // Gestión de inscripciones
    'inscriptions.view',
    'inscriptions.approve',
    'inscriptions.reject',
    // Reportes
    'reports.view',
    'reports.generate',
    // NO tiene permisos de usuarios
  ],

  REFEREE: [
    // Solo puede ver torneos y partidos
    'tournaments.view',
    'matches.view',
    'matches.manage_results', // ✅ CLAVE: Solo gestionar resultados
    'teams.view',
    // NO puede crear, editar o eliminar nada
    // NO tiene acceso a inscripciones
    // NO tiene acceso a reportes
    // NO tiene acceso a usuarios
  ],

  USER: [
    // Solo visualización básica
    'tournaments.view',
    'matches.view',
    'teams.view',
  ],
};

// Función helper para verificar permisos
export function hasPermission(userRole: Role | undefined, permission: Permission): boolean {
  if (!userRole) return false;
  return ROLE_PERMISSIONS[userRole]?.includes(permission) ?? false;
}

// Función helper para verificar múltiples permisos (AND)
export function hasAllPermissions(userRole: Role | undefined, permissions: Permission[]): boolean {
  if (!userRole) return false;
  return permissions.every(p => hasPermission(userRole, p));
}

// Función helper para verificar múltiples permisos (OR)
export function hasAnyPermission(userRole: Role | undefined, permissions: Permission[]): boolean {
  if (!userRole) return false;
  return permissions.some(p => hasPermission(userRole, p));
}