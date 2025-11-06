// frontend-uptc/src/hooks/usePermissions.ts
'use client';

import { useEffect, useState } from 'react';
import { authService } from '@/services/authService';
import { hasPermission, hasAllPermissions, hasAnyPermission, Permission, Role } from '@/config/permissions';

export function usePermissions() {
  const [userRole, setUserRole] = useState<Role | undefined>();

  useEffect(() => {
    const user = authService.getCurrentUser();
    setUserRole(user?.role as Role);
  }, []);

  return {
    userRole,
    can: (permission: Permission) => hasPermission(userRole, permission),
    canAll: (permissions: Permission[]) => hasAllPermissions(userRole, permissions),
    canAny: (permissions: Permission[]) => hasAnyPermission(userRole, permissions),
  };
}