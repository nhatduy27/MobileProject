import { AuthUser } from './auth-user.entity';

/**
 * Type for creating a new user (without generated fields)
 */
export type CreateAuthUserDto = {
  email: string;
  passwordHash: string;
  roles: string[];
};

/**
 * Auth Repository Port (Abstraction)
 * 
 * This abstract class defines the contract for authentication data access.
 * Implementations can use Firebase Auth, PostgreSQL, MongoDB, etc.
 */
export abstract class AuthRepository {
  /**
   * Find a user by email address
   */
  abstract findByEmail(email: string): Promise<AuthUser | null>;

  /**
   * Find a user by ID
   */
  abstract findById(id: string): Promise<AuthUser | null>;

  /**
   * Create a new user
   */
  abstract create(user: CreateAuthUserDto): Promise<AuthUser>;

  /**
   * Update an existing user
   */
  abstract update(id: string, user: Partial<AuthUser>): Promise<AuthUser>;

  /**
   * Delete a user by ID
   */
  abstract delete(id: string): Promise<void>;
}
