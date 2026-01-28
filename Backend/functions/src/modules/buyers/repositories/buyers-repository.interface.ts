import { ShopBuyerEntity, BuyerTier } from '../entities/shop-buyer.entity';
import { BuyerSortBy } from '../dto';

/**
 * Filters for listing buyers
 */
export interface BuyerListFilters {
  page: number;
  limit: number;
  tier?: 'ALL' | BuyerTier;
  search?: string;
  sort: BuyerSortBy;
}

/**
 * Buyers Repository Interface (DI)
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
   * Find buyer by customerId in shop
   */
  findById(shopId: string, customerId: string): Promise<ShopBuyerEntity | null>;

  /**
   * Create or update buyer stats (called from Orders service)
   */
  createOrUpdate(shopId: string, data: Partial<ShopBuyerEntity>): Promise<ShopBuyerEntity>;
}
