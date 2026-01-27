import { Timestamp } from 'firebase-admin/firestore';
import { IBaseEntity } from '../../../core/database';

/**
 * Payout Status
 */
export enum PayoutStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  TRANSFERRED = 'TRANSFERRED',
}

/**
 * Bank Info
 */
export interface BankInfo {
  bankName: string;
  accountNumber: string;
  accountName: string;
}

/**
 * Admin Payout Entity - Entity cho admin quản lý payouts
 *
 * NOTE: Đây là entity dùng trong Admin module.
 * Khi implement EPIC 10 (Wallet), entity đầy đủ sẽ ở modules/wallets/
 *
 * Collection: payoutRequests
 */
export interface AdminPayoutEntity extends IBaseEntity {
  /** User ID requesting payout */
  userId: string;

  /** Wallet ID */
  walletId: string;
  
  /** Wallet type */
  walletType?: string;

  /** Amount to payout (VND) */
  amount: number;

  /** Bank code (e.g., ICB, MB, VCB) */
  bankCode: string;
  
  /** Account number */
  accountNumber: string;
  
  /** Account name */
  accountName: string;

  /** Bank info (legacy - for backward compatibility) */
  bankInfo?: BankInfo;

  /** Payout status */
  status: PayoutStatus;

  /** Request note from user */
  note?: string;

  /** Approved info */
  approvedAt?: Timestamp;
  approvedBy?: string;

  /** Rejected info */
  rejectedAt?: Timestamp;
  rejectedBy?: string;
  rejectedReason?: string;

  /** Transferred info */
  transferredAt?: Timestamp;
  transferredBy?: string;
  transferNote?: string;
}
