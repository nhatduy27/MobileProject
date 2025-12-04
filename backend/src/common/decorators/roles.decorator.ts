import { SetMetadata } from '@nestjs/common';

/**
 * Roles Decorator
 * 
 * Use this decorator to specify which roles can access a route.
 * Example: @Roles('admin', 'seller')
 */
export const ROLES_KEY = 'roles';
export const Roles = (...roles: string[]) => SetMetadata(ROLES_KEY, roles);
