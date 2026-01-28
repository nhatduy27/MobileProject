import { IBaseRepository } from '../../../core/database/interfaces';
import { UserEntity, UserStatus } from '../entities';

/**
 * Users Repository Interface
 *
 * Contract for user data access operations.
 * Implementations: FirestoreUsersRepository
 */
export interface IUsersRepository extends IBaseRepository<UserEntity> {
  /**
   * Find user by email
   */
  findByEmail(email: string): Promise<UserEntity | null>;

  /**
   * Find user by phone number
   */
  findByPhone(phone: string): Promise<UserEntity | null>;

  /**
   * Update user's email verification status
   */
  markEmailVerified(userId: string): Promise<void>;

  /**
   * Update user's last login timestamp and increment login count
   */
  updateLastLogin(userId: string): Promise<void>;

  /**
   * Add FCM token to user's device list
   */
  addFcmToken(userId: string, fcmToken: string): Promise<void>;

  /**
   * Remove FCM token from user's device list
   */
  removeFcmToken(userId: string, fcmToken: string): Promise<void>;

  /**
   * Update user status (for admin ban/unban)
   */
  updateStatus(
    userId: string,
    status: UserStatus,
    adminId: string,
    reason?: string,
  ): Promise<UserEntity>;

  /**
   * Find user by email, excluding soft-deleted accounts
   * Returns users with status in [ACTIVE, BANNED] only.
   * Soft-deleted users (status=DELETED) are excluded from registration uniqueness checks.
   */
  findActiveByEmail(email: string): Promise<UserEntity | null>;

  /**
   * Find user by phone, excluding soft-deleted accounts
   * Returns users with status in [ACTIVE, BANNED] only.
   * Soft-deleted users (status=DELETED) are excluded from registration uniqueness checks.
   */
  findActiveByPhone(phone: string): Promise<UserEntity | null>;

  /**
   * Check if email already exists
   */
  emailExists(email: string): Promise<boolean>;
}

/**
 * Dependency Injection Token
 */
export const USERS_REPOSITORY_TOKEN = 'IUsersRepository';
