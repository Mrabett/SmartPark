import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRole = (route.data?.['role'] as string || '').toLowerCase().trim();
  const userRole = (authService.getUser()?.role || '').toLowerCase().trim();

  // Normalize: 'admin' or 'administrateur' → 'admin'; 'user' → 'client'
  const currentRole = (userRole === 'admin' || userRole === 'role_admin') ? 'admin' : 'client';

  console.log(`[Security] → ${state.url} | Required: "${requiredRole}" | User role: "${userRole}" → resolved: "${currentRole}"`);

  if (!userRole) {
    console.warn('[Security] No user found, redirecting to login');
    return router.parseUrl('/login');
  }

  // Admin can always access admin marketplace
  if (currentRole === 'admin' && requiredRole === 'admin') {
    console.log('[Security] ✅ Admin access granted');
    return true;
  }

  // Admin trying to access client marketplace → redirect to admin marketplace
  if (currentRole === 'admin' && requiredRole === 'client') {
    console.warn('[Security] Admin redirected to admin marketplace');
    return router.parseUrl('/marketplace/admin');
  }

  // Client can access client marketplace
  if (currentRole === 'client' && requiredRole === 'client') {
    return true;
  }

  // Client trying to access admin marketplace → redirect to client marketplace
  if (currentRole === 'client' && requiredRole === 'admin') {
    console.error('[Security] ❌ Client cannot access admin marketplace');
    return router.parseUrl('/marketplace/client');
  }

  return true;
};
