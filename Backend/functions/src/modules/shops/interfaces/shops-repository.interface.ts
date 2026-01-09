import { ShopEntity } from '../entities/shop.entity';
import { CreateShopDto, UpdateShopDto } from '../dto';

/**
 * Shop Repository Interface
 * Following SOLID principles (DIP - Dependency Inversion Principle)
 */
export interface IShopsRepository {
  /**
   * Create a new shop
   */
  create(ownerId: string, ownerName: string, data: CreateShopDto): Promise<ShopEntity>;

  /**
   * Find shop by ID
   */
  findById(shopId: string): Promise<ShopEntity | null>;

  /**
   * Find shop by owner ID
   */
  findByOwnerId(ownerId: string): Promise<ShopEntity | null>;

  /**
   * Update shop
   */
  update(shopId: string, data: Partial<UpdateShopDto>): Promise<ShopEntity>;

  /**
   * Toggle shop open/close status
   */
  toggleStatus(shopId: string, isOpen: boolean): Promise<void>;

  /**
   * Get all shops with pagination
   */
  findAll(params: {
    page: number;
    limit: number;
    status?: string;
    search?: string;
  }): Promise<{ shops: ShopEntity[]; total: number }>;

  /**
   * Update shop statistics
   */
  updateStats(shopId: string, stats: {
    totalOrders?: number;
    totalRevenue?: number;
    rating?: number;
    totalRatings?: number;
  }): Promise<void>;
}

export const SHOPS_REPOSITORY = 'SHOPS_REPOSITORY';
