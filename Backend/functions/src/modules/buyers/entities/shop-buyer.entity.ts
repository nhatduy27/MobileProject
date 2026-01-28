/**
 * Buyer Tier Enum
 * Auto-assigned based on totalSpent
 */
export enum BuyerTier {
  NEW = 'NEW', // < 500k
  NORMAL = 'NORMAL', // 500k - 2M
  VIP = 'VIP', // >= 2M
}

/**
 * ShopBuyer Entity
 * Stored in: shops/{shopId}/shopBuyers/{customerId}
 * Primary source of truth for buyer stats (cached/denormalized)
 */
export class ShopBuyerEntity {
  customerId: string;
  shopId: string;

  // Denormalized from users
  displayName: string;
  phone?: string;
  avatar?: string;
  email?: string;

  // Auto-assigned tier
  tier: BuyerTier;
  tierLastUpdated: Date;

  // Cached stats from DELIVERED orders
  totalOrders: number;
  totalSpent: number;
  avgOrderValue: number;
  firstOrderDate?: Date;
  lastOrderDate?: Date;
  joinedDate: Date; // = firstOrderDate

  // Metadata
  createdAt: Date;
  updatedAt: Date;

  constructor(partial: Partial<ShopBuyerEntity>) {
    Object.assign(this, partial);
  }
}

/**
 * Calculate buyer tier based on total spending
 * MVP: Hardcoded thresholds
 */
export function calculateBuyerTier(totalSpent: number): BuyerTier {
  if (totalSpent >= 2_000_000) return BuyerTier.VIP;
  if (totalSpent >= 500_000) return BuyerTier.NORMAL;
  return BuyerTier.NEW;
}
