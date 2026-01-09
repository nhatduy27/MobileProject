import { Timestamp } from 'firebase-admin/firestore';

/**
 * Shop Subscription Status
 */
export enum SubscriptionStatus {
  TRIAL = 'TRIAL', // 7 days free trial
  ACTIVE = 'ACTIVE', // Paid and active
  EXPIRED = 'EXPIRED', // Subscription expired
  SUSPENDED = 'SUSPENDED', // Admin suspended
}

/**
 * Shop Status
 */
export enum ShopStatus {
  OPEN = 'OPEN', // Shop is operating
  CLOSED = 'CLOSED', // Shop temporarily closed
  SUSPENDED = 'SUSPENDED', // Admin suspended
}

/**
 * Subscription Information
 */
export interface ShopSubscription {
  status: SubscriptionStatus;
  startDate: Timestamp;
  trialEndDate: Timestamp | null;
  currentPeriodEnd: Timestamp | null;
  nextBillingDate: Timestamp | null;
  autoRenew: boolean;
}

/**
 * Shop Entity
 * Collection: shops
 */
export class ShopEntity {
  id: string;
  ownerId: string;
  ownerName: string;
  name: string;
  description: string;
  address: string;
  phone: string;

  // Images
  coverImageUrl?: string;
  logoUrl?: string;

  // Operating hours
  openTime: string; // "07:00"
  closeTime: string; // "21:00"

  // Pricing
  shipFeePerOrder: number; // Min 3000đ
  minOrderAmount: number; // Min order amount to place order

  // Status
  isOpen: boolean; // Owner toggle
  status: ShopStatus; // OPEN, CLOSED, SUSPENDED

  // Stats
  rating: number;
  totalRatings: number;
  totalOrders: number;
  totalRevenue: number;

  // Subscription
  subscription: ShopSubscription;

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
