import { IBaseRepository } from '../../../core/database/interfaces';
import { OTPEntity, OTPType } from '../entities';

/**
 * OTP Repository Interface
 * 
 * Contract for OTP data access operations.
 * Implementations: FirestoreOTPRepository
 */
export interface IOTPRepository extends IBaseRepository<OTPEntity> {
  /**
   * Find the most recent OTP for an email
   */
  findLatestByEmail(email: string, type: OTPType): Promise<OTPEntity | null>;
  
  /**
   * Mark OTP as verified
   */
  markVerified(otpId: string): Promise<void>;
  
  /**
   * Increment verification attempts
   */
  incrementAttempts(otpId: string): Promise<void>;
  
  /**
   * Delete all OTPs for an email (after successful verification)
   */
  deleteByEmail(email: string, type: OTPType): Promise<void>;
  
  /**
   * Delete expired OTPs (cleanup job)
   */
  deleteExpired(): Promise<number>;
  
  /**
   * Check if user has requested OTP recently (rate limiting)
   */
  hasRecentRequest(email: string, type: OTPType, seconds: number): Promise<boolean>;
}

/**
 * Dependency Injection Token
 */
export const OTP_REPOSITORY_TOKEN = 'IOTPRepository';
