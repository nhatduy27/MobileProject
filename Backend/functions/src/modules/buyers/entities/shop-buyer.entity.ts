/**
 * Buyer Tier - Auto-assigned based on totalSpent
 */
export enum BuyerTier {
  NEW = 'NEW', // totalSpent < 500k
  NORMAL = 'NORMAL', // totalSpent >= 500k && < 2M
  VIP = 'VIP', // totalSpent >= 2M
}

/**
 * Shop Buyer Entity
 * Collection: shops/{shopId}/shopBuyers
 *
 * Cached/denormalized buyer stats for fast queries
 * Updated on order delivery
 */
export class ShopBuyerEntity {
  customerId: string; // FK to users
  shopId: string;

  // Denormalized from users
  displayName: string;
  phone?: string;
  avatar?: string;
  email?: string;

  // Auto-assigned tier (based on totalSpent)
  tier: BuyerTier;
  tierLastUpdated: Date;

  // Cached stats from orders (DELIVERED only)
  totalOrders: number; // Count of DELIVERED orders
  totalSpent: number; // Sum of order totals
  avgOrderValue: number; // totalSpent / totalOrders
  firstOrderDate?: Date;
  lastOrderDate?: Date;
  joinedDate: Date; // First order date

  // Metadata
  createdAt: Date;
  updatedAt: Date;

  constructor(partial: Partial<ShopBuyerEntity>) {
    Object.assign(this, partial);
  }
}

/**
 * Tier Calculation Rules (MVP - Hardcoded)
 */
export const TIER_THRESHOLDS = {
  NEW: { min: 0, max: 499999 }, // < 500k
  NORMAL: { min: 500000, max: 1999999 }, // 500k - 2M
  VIP: { min: 2000000, max: Infinity }, // >= 2M
};

/**
 * Calculate tier based on totalSpent
 */
export function calculateBuyerTier(totalSpent: number): BuyerTier {
  if (totalSpent >= TIER_THRESHOLDS.VIP.min) {
    return BuyerTier.VIP;
  }
  if (totalSpent >= TIER_THRESHOLDS.NORMAL.min) {
    return BuyerTier.NORMAL;
  }
  return BuyerTier.NEW;
}
