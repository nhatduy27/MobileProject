import { IBaseEntity } from '../../../core/database/interfaces';

/**
 * User Entity
 *
 * Represents a user in the system.
 * Stored in Firestore collection: users
 */
export interface UserEntity extends IBaseEntity {
  email: string;
  displayName: string;
  phone?: string;
  photoUrl?: string;

  // Role-based access control
  role: UserRole;

  // Account status
  status: UserStatus;
  emailVerified: boolean;

  // Authentication provider (password, google, etc.)
  provider?: 'password' | 'google';

  // Ban information (if status === BANNED)
  bannedAt?: Date;
  bannedBy?: string; // Admin UID
  bannedReason?: string;

  // Unban information (if previously banned)
  unbannedAt?: Date;
  unbannedBy?: string; // Admin UID

  // Push notifications
  fcmTokens?: string[]; // Array of FCM tokens for multiple devices

  // Additional metadata
  lastLoginAt?: Date;
  loginCount?: number;
}

/**
 * User Role Enum
 */
export enum UserRole {
  CUSTOMER = 'CUSTOMER',
  OWNER = 'OWNER',
  SHIPPER = 'SHIPPER',
  ADMIN = 'ADMIN',
}

/**
 * User Status Enum
 */
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  BANNED = 'BANNED',
  DELETED = 'DELETED',
}
