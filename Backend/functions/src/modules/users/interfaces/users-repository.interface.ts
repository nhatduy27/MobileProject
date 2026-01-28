import { UserEntity, UserSettings } from '../entities';

/**
 * Users Repository Interface
 *
 * Abstraction for user data access (SOLID - Dependency Inversion)
 */
export interface IUsersRepository {
  /**
   * Find user by ID
   */
  findById(userId: string): Promise<UserEntity | null>;

  /**
   * Find user by email
   */
  findByEmail(email: string): Promise<UserEntity | null>;

  /**
   * Update user profile
   */
  updateProfile(userId: string, data: Partial<UserEntity>): Promise<UserEntity>;

  /**
   * Update user settings
   */
  updateSettings(userId: string, settings: UserSettings): Promise<UserSettings>;

  /**
   * Get user settings
   */
  getSettings(userId: string): Promise<UserSettings | null>;

  /**
   * Update avatar URL
   */
  updateAvatarUrl(userId: string, avatarUrl: string): Promise<void>;

  /**
   * Clear avatar URL (set to null)
   */
  clearAvatarUrl(userId: string): Promise<void>;

  /**
   * Add FCM token
   */
  addFcmToken(userId: string, token: string, deviceId: string): Promise<void>;

  /**
   * Remove FCM token
   */
  removeFcmToken(userId: string, token: string): Promise<void>;

  /**
   * Soft delete user
   */
  softDelete(userId: string): Promise<void>;
}

export const USERS_REPOSITORY = 'IUsersRepository';
