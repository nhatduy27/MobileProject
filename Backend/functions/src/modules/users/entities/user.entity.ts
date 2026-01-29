/**
 * User Entity
 *
 * Represents a user in the system with role-specific fields
 */

export enum UserRole {
  CUSTOMER = 'CUSTOMER',
  OWNER = 'OWNER',
  SHIPPER = 'SHIPPER',
  ADMIN = 'ADMIN',
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  BANNED = 'BANNED',
  DELETED = 'DELETED',
}

export interface ShipperInfo {
  shopId: string;
  shopName: string;
  vehicleType: string;
  vehicleNumber: string;
  driverLicenseUrl?: string;
  status: 'ACTIVE' | 'INACTIVE';
  isOnline: boolean;
}

export interface NotificationSettings {
  orderUpdates: boolean;
  promotions: boolean;
  email: boolean;
  push: boolean;
}

export interface UserSettings {
  notifications: NotificationSettings;
  language: 'vi' | 'en';
  currency: string;
}

export interface FcmToken {
  token: string;
  deviceId: string;
  createdAt: Date;
}

export class UserEntity {
  id: string;
  email: string;
  displayName: string;
  phone?: string;
  avatarUrl?: string;
  role: UserRole;
  status: UserStatus;
  emailVerified: boolean;

  // Authentication provider (password, google, etc.)
  provider?: 'password' | 'google';

  // Role-specific fields
  shopId?: string; // For OWNER
  shipperInfo?: ShipperInfo; // For SHIPPER

  // Settings
  settings?: UserSettings;

  // FCM tokens for push notifications
  fcmTokens?: FcmToken[];

  // Ban info
  bannedAt?: Date;
  bannedBy?: string;
  bannedReason?: string;

  // Timestamps
  createdAt: Date;
  updatedAt: Date;

  constructor(partial: Partial<UserEntity>) {
    Object.assign(this, partial);
  }
}
