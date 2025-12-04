/**
 * Auth User Entity (Domain Model)
 * 
 * Represents an authenticated user in the system.
 * This is a domain model, independent of any infrastructure concerns.
 */
export class AuthUser {
  id: string;
  email: string;
  passwordHash: string;
  roles: string[];
  createdAt: Date;
  updatedAt: Date;

  constructor(partial: Partial<AuthUser>) {
    Object.assign(this, partial);
  }

  /**
   * Check if user has a specific role
   */
  hasRole(role: string): boolean {
    return this.roles.includes(role);
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles: string[]): boolean {
    return roles.some((role) => this.roles.includes(role));
  }
}
