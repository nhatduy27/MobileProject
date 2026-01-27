import { ShopBuyerEntity, BuyerTier } from '../entities/shop-buyer.entity';
import { BuyerSortBy } from '../dto';

/**
 * Buyer List Filters
 */
export interface BuyerListFilters {
  page: number;
  limit: number;
  tier?: 'ALL' | BuyerTier;
  search?: string; // Prefix match on displayName + phone
  sort: BuyerSortBy;
}

/**
 * Buyers Repository Interface
 * Handles subcollection: shops/{shopId}/shopBuyers
 */
export interface IBuyersRepository {
  /**
   * List buyers for a shop with filters and pagination
   */
  listByShop(
    shopId: string,
    filters: BuyerListFilters,
  ): Promise<{ buyers: ShopBuyerEntity[]; total: number }>;

  /**
   * Find a buyer by customerId in shop
   */
  findById(shopId: string, customerId: string): Promise<ShopBuyerEntity | null>;

  /**
   * Create or update buyer stats (for Cloud Function)
   */
  createOrUpdate(shopId: string, data: Partial<ShopBuyerEntity>): Promise<ShopBuyerEntity>;
}
