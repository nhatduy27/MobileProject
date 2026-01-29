import { ProductEntity } from '../entities';
import { ProductFilterDto } from '../dto';

/**
 * Products Repository Interface (SOLID - Dependency Inversion Principle)
 */
export interface IProductsRepository {
  /**
   * Create a new product
   */
  create(
    shopId: string,
    shopName: string,
    categoryName: string,
    data: {
      name: string;
      description: string;
      price: number;
      categoryId: string;
      imageUrl: string;
      preparationTime: number;
    },
  ): Promise<ProductEntity>;

  /**
   * Find product by ID
   */
  findById(id: string): Promise<ProductEntity | null>;

  /**
   * Find products by shop ID with filters
   */
  findByShopId(
    shopId: string,
    filters: Omit<Partial<ProductFilterDto>, 'isAvailable'> & { isAvailable?: string | boolean },
  ): Promise<{ products: ProductEntity[]; total: number }>;

  /**
   * Global product search (across all shops)
   */
  searchGlobal(filters: ProductFilterDto): Promise<{ products: ProductEntity[]; total: number }>;

  /**
   * Update product
   */
  update(id: string, data: Partial<ProductEntity>): Promise<void>;

  /**
   * Toggle product availability
   */
  toggleAvailability(id: string, isAvailable: boolean): Promise<void>;

  /**
   * Soft delete product
   */
  softDelete(id: string): Promise<void>;

  /**
   * Update product stats
   */
  updateStats(
    id: string,
    stats: {
      rating?: number;
      totalRatings?: number;
      soldCount?: number;
    },
  ): Promise<void>;

  /**
   * Increment soldCount for multiple products atomically
   * Used when order is delivered
   */
  incrementSoldCount(items: Array<{ productId: string; quantity: number }>): Promise<void>;

  /**
   * Decrement soldCount for multiple products atomically
   * Used when delivered order is cancelled (edge case)
   */
  decrementSoldCount(items: Array<{ productId: string; quantity: number }>): Promise<void>;
}
