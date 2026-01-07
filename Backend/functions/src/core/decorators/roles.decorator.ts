import { SetMetadata } from '@nestjs/common';
import { UserRole } from '../interfaces/user.interface';

export const ROLES_KEY = 'roles';

/**
 * Roles Decorator
 *
 * Specifies which roles can access the route.
 * Used with RolesGuard.
 *
 * Usage:
 *   @Roles(UserRole.ADMIN)
 *   @Roles(UserRole.SELLER, UserRole.ADMIN)
 */
export const Roles = (...roles: UserRole[]) => SetMetadata(ROLES_KEY, roles);
