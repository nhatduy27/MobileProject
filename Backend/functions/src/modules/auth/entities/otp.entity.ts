import { IBaseEntity } from '../../../core/database/interfaces';

/**
 * OTP Entity
 * 
 * Represents a one-time password for email verification or password reset.
 * Stored in Firestore collection: otps
 * 
 * TTL: 5 minutes
 */
export interface OTPEntity extends IBaseEntity {
  email: string;
  code: string; // 6-digit code
  type: OTPType;
  expiresAt: Date;
  verified: boolean;
  attempts: number; // Number of verification attempts
}

/**
 * OTP Type Enum
 */
export enum OTPType {
  EMAIL_VERIFICATION = 'EMAIL_VERIFICATION',
  PASSWORD_RESET = 'PASSWORD_RESET',
}

/**
 * OTP Configuration
 */
export const OTP_CONFIG = {
  CODE_LENGTH: 6,
  EXPIRY_MINUTES: 5,
  MAX_ATTEMPTS: 3,
  RATE_LIMIT_SECONDS: 60, // Can't request new OTP within 60 seconds
};
